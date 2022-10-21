package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.AuditEvent;
import com.rhbgroup.dcpbo.customer.dto.AuditPagination;
import com.rhbgroup.dcpbo.customer.dto.Event;
import com.rhbgroup.dcpbo.customer.dto.EventCategory;
import com.rhbgroup.dcpbo.customer.model.Audit;
import com.rhbgroup.dcpbo.customer.model.AuditEventConfig;
import com.rhbgroup.dcpbo.customer.model.DcpAuditCategoryConfig;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.DcpCustomerAuditService;
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
public class DcpCustomerAuditServiceImpl implements DcpCustomerAuditService {

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

    private static final String SUCCESS_STATUS = "Success";
    private static final String FAILED_STATUS = "Failed";
    public static final int PAGE_SIZE = 15;
    public static final String PATH_REQ_REF_ID = "$.request.refId";
    public static final String PATH_RES_REF_ID = "$.response.refId";
    public static final String PATH_ADD_REF_ID = "$.additionalData.referenceId";
    private final Logger log = LogManager.getLogger(DcpCustomerAuditServiceImpl.class);

    @Override
    public BoData listing(String frDateStr, String toDateStr, int customerId, String auditCategoryIds, Integer pageNo) {

        HashMap formattedDate = auditEventsService.parseInputDate(frDateStr, toDateStr);
        Timestamp frDate = (Timestamp) formattedDate.get("frDate");
        Timestamp toDate = (Timestamp) formattedDate.get("toDate");

        List<Audit> dcpAudits;
        Integer totalPageNum;
        Integer offset;
        AuditPagination auditPagination = new AuditPagination();
        auditPagination.setPageNum(1);

        offset = (pageNo - 1) * PAGE_SIZE;
        auditPagination.setPageNum(pageNo);

        List<Integer> auditCategoriesList = new ArrayList<>();
        if (!auditCategoryIds.equals("all")) {
            auditCategoriesList = boRepositoryHelper.constructAuditConfigCategories(auditCategoryIds);

            dcpAudits = dcpAuditRepository.getCustomerAuditEventsByCategories(customerId, frDate, toDate, offset, PAGE_SIZE, auditCategoriesList);
            totalPageNum = (int) Math.ceil((double) dcpAuditRepository.getCustomerAuditEventsCountByCategories(customerId, frDate, toDate, auditCategoriesList) / PAGE_SIZE);
            if (dcpAudits.size() == 0) {
                log.warn("No audit log found for user with id " + customerId + " from " + frDateStr + " to " + toDateStr);
            }
        } else {
            dcpAudits = dcpAuditRepository.getCustomerAuditEvents(customerId, frDate, toDate, offset, PAGE_SIZE);
            totalPageNum = (int) Math.ceil((double) dcpAuditRepository.getCustomerAuditEventsCount(customerId, frDate, toDate) / PAGE_SIZE);
            if (dcpAudits.size() == 0) {
                log.warn("No audit log found for user with id " + customerId + " from " + frDateStr + " to " + toDateStr);
            }
        }

        AuditEvent auditEvent = new AuditEvent();
        if (dcpAudits.size() != 0) {

            List<String> eventCodesList = boRepositoryHelper.constructEventCodesFromAudits(dcpAudits);
            List<AuditEventConfig> dcpEventConfigs;

            if (auditCategoryIds.equals("all")) {
                dcpEventConfigs = dcpAuditEventConfigRepository.getMappingsAll(eventCodesList);
            } else {
                dcpEventConfigs = dcpAuditEventConfigRepository.getMappings(eventCodesList, auditCategoriesList);
            }

            //Constructing event response
            for (Audit dcpAudit : dcpAudits) {
                Event event = new Event();
                String eventCode = dcpAudit.getEventCode();
                event.setEventId(Integer.toString(dcpAudit.getId()));
                event.setEventCode(eventCode);
                Optional<AuditEventConfig> auditEventConfigOptional = dcpEventConfigs.stream()
                        .filter(adtEvtConfig -> dcpAudit.getEventCode().equalsIgnoreCase(adtEvtConfig.getEventCode()))
                        .findFirst();

                event.setEventName(auditEventConfigOptional.get().getEventName());
                event.setEventCategoryId(String.valueOf(auditEventConfigOptional.get().getEventCategoryId()));
                Integer auditId = dcpAudit.getId();

                String auditDetails = auditEventsService.getAuditDetails(auditEventConfigOptional, auditId);
                log.info("auditDetails : " + auditDetails);

                if (StringUtils.isNotEmpty(dcpAudit.getStatusCode())) {
                    Integer successCount = lookupStatusRepository.getSuccessStatusCount(dcpAudit.getStatusCode());
                    if (successCount.intValue() > 0) {
                        event.setStatus(SUCCESS_STATUS);
                    } else {
                        event.setStatus(FAILED_STATUS);
                    }
                }
                event.setChannel(dcpAudit.getChannel());
                event.setTimestamp(dcpAudit.getTimestamp().toString());


                //Constructing description if it has description
                if (auditEventConfigOptional.isPresent()) {
                    String description = auditEventsService.getDescription(auditEventConfigOptional, auditId, eventCode);
                    event.setDescription(description);
                }
                //Setting ref Id
                if (StringUtils.isNotEmpty(auditDetails)) {
                    if (JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_REQ_REF_ID) != null) {
                        String refId = JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_REQ_REF_ID);
                        event.setRefId(refId);
                        log.info("PATH_REQ_REF_ID : " + PATH_REQ_REF_ID);
                    } else if (JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_RES_REF_ID) != null) {
                        String refId = JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_RES_REF_ID);
                        event.setRefId(refId);
                        log.info("PATH_RES_REF_ID : " + PATH_RES_REF_ID);
                    } else if (JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_ADD_REF_ID) != null) {
                        String refId = JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_ADD_REF_ID);
                        event.setRefId(refId);
                        log.info("PATH_ADD_REF_ID : " + PATH_ADD_REF_ID);
                    }
                    log.info("Ref Id :" + event.getRefId());
                }
                auditEvent.addEvent(event);
            }
        }

        //Constructing pagination
        if (dcpAudits.size() < PAGE_SIZE) {
            auditPagination.setPageIndicator("L");
        } else {
            auditPagination.setPageIndicator("N");
        }
        auditPagination.setActivityCount(dcpAudits.size());
        auditPagination.setPageNum(pageNo);
        auditPagination.setTotalPageNum(totalPageNum);
        auditEvent.setPagination(auditPagination);

        //Constructing event categories list
        List<DcpAuditCategoryConfig> eventCategories = dcpAuditCategoryConfigRepository.findAll();
        for (DcpAuditCategoryConfig dcpAuditCategoryConfig : eventCategories) {
            EventCategory eventCategory = new EventCategory();
            eventCategory.setEventCategoryId(Integer.toString(dcpAuditCategoryConfig.getId()));
            eventCategory.setEventCategoryName(dcpAuditCategoryConfig.getCategoryName());
            auditEvent.addEventCategory(eventCategory);
        }
        return auditEvent;
    }
}
