package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.Investigation;
import com.rhbgroup.dcpbo.customer.dto.InvestigationEvent;
import com.rhbgroup.dcpbo.customer.dto.InvestigationPagination;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.Audit;
import com.rhbgroup.dcpbo.customer.model.AuditEventConfig;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.InvestigationAuditListService;
import com.rhbgroup.dcpbo.customer.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


@Service
public class InvestigationAuditListServiceImpl implements InvestigationAuditListService {

    @Autowired
    private DcpAuditCategoryConfigRepository dcpAuditCategoryConfigRepository;

    @Autowired
    private DcpAuditRepository dcpAuditRepository;

    @Autowired
    private DcpAuditEventConfigRepository dcpAuditEventConfigRepository;

    @Autowired
    private AuditSummaryConfigRepository auditSummaryConfigRepository;

    @Autowired
    private DcpAuditFundTransferRepository dcpAuditFundTransferRepository;

    @Autowired
    private DcpAuditBillPaymentRepository dcpAuditBillPaymentRepository;

    @Autowired
    private DcpAuditMiscRepository dcpAuditMiscRepository;

    @Autowired
    private DcpAuditProfileRepository dcpAuditProfileRepository;

    @Autowired
    private DcpAuditTopupRepository dcpAuditTopupRepository;

    @Autowired
    private LookupStatusRepository lookupStatusRepository;

    @Autowired
    private BoRepositoryHelper boRepositoryHelper;

    @Autowired
    private AuditEventsService auditEventsService;

    public static final int PAGE_SIZE = 20;
    private final Logger log = LogManager.getLogger(InvestigationAuditListServiceImpl.class);
    private final String STRING_ALL = "all";
    private final String PAGINATION_LAST = "L";
    private final String PAGINATION_NOT_LAST = "N";
    private final String STATUS_FAIL = "F";
    private final String STATUS_SUCCESS = "S";
    private static final String SUCCESS_STATUS = "Success";
    private static final String FAILED_STATUS = "Failed";
    public static final String PATH_REQ_REF_ID = "$.request.refId";
    public static final String PATH_RES_REF_ID = "$.response.refId";
    public static final String PATH_ADD_REF_ID = "$.additionalData.referenceId";

    @Override
    public BoData listing(String eventCodes, Integer pageNum, String frDateStr, String toDateStr, String status) {

        HashMap formattedDate = auditEventsService.parseInputDate(frDateStr, toDateStr);
        Timestamp frDate = (Timestamp) formattedDate.get("frDate");
        Timestamp toDate = (Timestamp) formattedDate.get("toDate");

        List<Audit> dcpAudits;
        Integer totalPageNum;
        Integer offset;
        InvestigationPagination investigationPagination = new InvestigationPagination();
        Investigation investigation = new Investigation();
        investigationPagination.setPageNum(1);

        offset = (pageNum - 1) * PAGE_SIZE;
        investigationPagination.setPageNum(pageNum);

        List<Integer> codeList;
        Integer recordCount;
        if (!status.equals(STRING_ALL)) {
            if (status.equalsIgnoreCase(STATUS_FAIL)) {
                codeList = lookupStatusRepository.getEventCodesByFailStatus();
            } else if (status.equalsIgnoreCase(STATUS_SUCCESS)) {
                codeList = lookupStatusRepository.getEventCodesBySuccessStatus();
            } else {
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Invalid status for " + status);
            }
        } else {
            codeList = lookupStatusRepository.getEventCodes();
        }
        if (!eventCodes.equals("0")) {
            List<String> eventCodeList;
            eventCodeList = boRepositoryHelper.constructEventCodeList(eventCodes);
            dcpAudits = dcpAuditRepository.getCustomerAuditEventsByEventCodeAndStatusCode(eventCodeList, frDate, toDate, offset, PAGE_SIZE, codeList);
            recordCount = dcpAuditRepository.getCustomerAuditEventsCountByEventCodeAndStatusCode(eventCodeList, frDate, toDate, codeList);
            totalPageNum = (int) Math.ceil((double) recordCount / PAGE_SIZE);
            if (dcpAudits.size() == 0) {
                log.warn("No audit log found for user with event code " + eventCodes + " from " + frDateStr + " to " + toDateStr);
            }
        } else {
            dcpAudits = dcpAuditRepository.getCustomerAuditEventsByStatusCode(frDate, toDate, offset, PAGE_SIZE, codeList);
            recordCount = dcpAuditRepository.getCustomerAuditEventsCountByStatusCode(frDate, toDate, codeList);
            totalPageNum = (int) Math.ceil((double) recordCount / PAGE_SIZE);
            if (dcpAudits.size() == 0) {
                log.warn("No audit log found from " + frDateStr + " to " + toDateStr);
            }
        }

        if (dcpAudits.size() != 0) {
            List<String> eventCodesList = boRepositoryHelper.constructEventCodesFromAudits(dcpAudits);
            List<AuditEventConfig> dcpEventConfigs;
            dcpEventConfigs = dcpAuditEventConfigRepository.getMappingsAll(eventCodesList);
            if (dcpEventConfigs == null)
                log.warn("Empty event configs for event codes");

            //Constructing event response
            for (Audit dcpAudit : dcpAudits) {
                InvestigationEvent investigationEvent = new InvestigationEvent();
                String auditEventCode = dcpAudit.getEventCode();
                investigationEvent.setEventCode(dcpAudit.getEventCode());
                Optional<AuditEventConfig> auditEventConfigOptional = Optional.ofNullable(dcpEventConfigs.stream()
                        .filter(adtEvtConfig -> dcpAudit.getEventCode().equalsIgnoreCase(adtEvtConfig.getEventCode()))
                        .findFirst().orElse(null));
                String description;
                String auditDetails = null;
                Integer auditId = dcpAudit.getId();

                investigationEvent.setChannel(dcpAudit.getChannel());
                investigationEvent.setTimestamp(dcpAudit.getTimestamp().toString());

                //Constructing description if it has description
                if (auditEventConfigOptional.isPresent()) {
                    investigationEvent.setEventName(String.valueOf(auditEventConfigOptional.get().getEventName()));

                    description = auditEventsService.getDescription(auditEventConfigOptional, auditId, auditEventCode);
                    investigationEvent.setSummaryDescription(description);

                    auditDetails = auditEventsService.getAuditDetails(auditEventConfigOptional, auditId);
                }
                investigationEvent.setAuditId(String.valueOf(dcpAudit.getId()));
                investigationEvent.setUsername(dcpAudit.getUsername());

                if (StringUtils.isNotEmpty(dcpAudit.getStatusCode())) {
                    Integer successCount = lookupStatusRepository.getSuccessStatusCount(dcpAudit.getStatusCode());
                    if (successCount.intValue() > 0) {
                        investigationEvent.setStatusDescription(SUCCESS_STATUS);
                    } else {
                        investigationEvent.setStatusDescription(FAILED_STATUS);
                    }
                }

                if (StringUtils.isNotEmpty(auditDetails)) {
                    if (JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_REQ_REF_ID) != null) {
                        String refId = JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_REQ_REF_ID);
                        investigationEvent.setRefId(refId);
                        log.info("PATH_REQ_REF_ID : " + PATH_REQ_REF_ID);
                    } else if (JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_RES_REF_ID) != null) {
                        String refId = JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_RES_REF_ID);
                        investigationEvent.setRefId(refId);
                        log.info("PATH_RES_REF_ID : " + PATH_RES_REF_ID);
                    } else if (JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_ADD_REF_ID) != null) {
                        String refId = JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_ADD_REF_ID);
                        investigationEvent.setRefId(refId);
                        log.info("PATH_ADD_REF_ID : " + PATH_ADD_REF_ID);
                    }
                    log.info("Ref Id :" + investigationEvent.getRefId());


                }
                investigation.addEvent(investigationEvent);
            }
        }

        //Constructing pagination
        if (dcpAudits.size() < PAGE_SIZE) {
            investigationPagination.setPageIndicator(PAGINATION_LAST);
        } else {
            investigationPagination.setPageIndicator(PAGINATION_NOT_LAST);
        }
        investigationPagination.setRecordCount(recordCount);
        investigationPagination.setPageNum(pageNum);
        investigationPagination.setTotalPageNum(totalPageNum);
        investigation.setPagination(investigationPagination);

        return investigation;
    }
}
