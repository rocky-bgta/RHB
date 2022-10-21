package com.rhbgroup.dcpbo.customer.service.impl;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.AuditDetails;
import com.rhbgroup.dcpbo.customer.dto.AuditDetailsActivity;
import com.rhbgroup.dcpbo.customer.enums.DefaultValue;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.Audit;
import com.rhbgroup.dcpbo.customer.model.AuditDetailConfig;
import com.rhbgroup.dcpbo.customer.model.AuditDetailsRecord;
import com.rhbgroup.dcpbo.customer.model.AuditEventConfig;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.AuditDetailsService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class AuditDetailsServiceImpl implements AuditDetailsService {
    @Autowired
    AuditRepository auditRepository;

    @Autowired
    DcpAuditEventConfigRepository auditEventConfigRepository;

    @Autowired
    AuditDetailConfigRepo auditDetailConfigRepository;

    @Autowired
    DcpAuditBillPaymentRepository auditBillPaymentRepository;

    @Autowired
    DcpAuditFundTransferRepository auditFundTransferRepository;

    @Autowired
    AuditMiscRepository auditMiscRepository;

    @Autowired
    AuditProfileRepository auditProfileRepository;

    @Autowired
    DcpAuditTopupRepository auditTopupRepository;

    @Autowired
    LookupStatusRepository lookupStatusRepository;

    enum DetailTable {
        DCP_AUDIT_BILL_PAYMENT,
        DCP_AUDIT_FUND_TRANSFER,
        DCP_AUDIT_MISC,
        DCP_AUDIT_PROFILE,
        DCP_AUDIT_TOPUP
    }

    private static final String SUCCESS_STATUS = "Success";
    private static final String FAILED_STATUS = "Failed";
    private static final String FIELD_AMOUNT = "amount";
    private static final String PATH_REF_ID = "$.request.refId";
    private static final String FORMAT_AMOUNT = "0.00";
    private static Logger logger = LogManager.getLogger(AuditDetailsServiceImpl.class);

    /*
     * This is implemented as part of DCPBL-8143 user story
     */
    @Override
    public BoData getAuditDetailsActivity(Integer auditId, String eventCode) {
        DecimalFormat format = new DecimalFormat(FORMAT_AMOUNT);
        logger.debug("getAuditDetailsActivity()");
        logger.info("    auditId: " + auditId);
        logger.info("    eventCode: " + eventCode);

        Audit audit = auditRepository.findOne(auditId);
        logger.info("    audit: " + audit);
        if (audit == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find Audit for auditId: " + auditId);
        if (StringUtils.isNotEmpty(audit.getEventCode()) && !audit.getEventCode().equals(eventCode)) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Invalid Audit entry request for auditId:" + auditId + ";eventCode:" + eventCode);
        }
        Date timestamp = audit.getTimestamp();
        if (timestamp == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Null return value for audit.getTimestamp()");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        String sTimestamp = simpleDateFormat.format(timestamp);
        String deviceId = audit.getDeviceId();
        String channel = audit.getChannel();
        String statusCode = audit.getStatusCode();
        String statusDescription = audit.getStatusDescription();
        logger.info("        sTimestamp: " + sTimestamp);
        logger.info("        deviceId: " + deviceId);
        logger.info("        channel: " + channel);
        logger.info("        statusCode: " + statusCode);
        logger.info("        statusDescription: " + statusDescription);

        List<AuditDetailConfig> auditDetailConfigList = auditDetailConfigRepository.findAllByEventCode(eventCode);
        logger.info("    auditDetailConfigList: " + auditDetailConfigList);
        if (auditDetailConfigList == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find AuditDetailConfigList for eventCode: " + eventCode);
        String detailsTableName = "";
        AuditDetails auditDetails = new AuditDetails();
        AuditEventConfig auditEventConfig = auditEventConfigRepository.findByEventCode(eventCode);
        logger.info("    auditEventConfig: " + auditEventConfig);
        if (auditEventConfig == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find AuditEventConfig for eventCode: " + eventCode);
        else if (!auditEventConfig.getDetailsTableName().isEmpty() && !auditEventConfig.getDetailsTableName().toUpperCase().equals("NULL")) {

            detailsTableName = auditEventConfig.getDetailsTableName();
            AuditDetailsTableRepository auditDetailsTableRepository = getDetailsRepository(detailsTableName);
            AuditDetailsRecord auditDetailsRecord = auditDetailsTableRepository.findByAuditId(auditId);
            if (auditDetailsRecord == null) {
                logger.warn("AuditDetailsRecord not found for auditId: {}", auditId);
            } else {
                String details = auditDetailsRecord.getDetails();
                logger.info("    details: " + details);
                if (details == null)
                    throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Null value for details in auditDetailsRecord: " + auditDetailsRecord);

                DocumentContext documentContext = JsonPath.parse(details);
                if (documentContext == null) {
                    throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Null value for documentContext when parsing details: " + details);
                } else {
                    List<String> favEventCodeList = auditEventConfigRepository.getEventCodesFavourites();

                    if (eventCode.equals("31016") || eventCode.equals("31015")) {
                        try {
                            String refId = documentContext.read(PATH_REF_ID, String.class);
                            if (StringUtils.isNotEmpty(refId)) {
                                auditDetails.addDetails("Ref No", refId);
                            }
                        } catch (Exception e) {
                            logger.warn(e.getMessage());
                        }
                    }

                    //Iterate for each config details
                    auditDetailConfigList.forEach(
                            auditDetailConfig -> {
                                logger.info("        auditDetailConfig: " + auditDetailConfig);
                                String fieldName = auditDetailConfig.getFieldName();
                                String fieldPath = auditDetailConfig.getPath();
                                logger.info("            fieldName: " + fieldName);
                                logger.info("            fieldPath: " + fieldPath);
                                if (StringUtils.isEmpty(fieldName)) {
                                    fieldName = StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(fieldPath.substring(fieldPath.lastIndexOf('.') + 1)), ' '));
                                }
                                String fieldValue = null;
                                try {
                                    fieldValue = documentContext.read(fieldPath).toString();
                                    logger.info("            value: " + fieldValue);
                                    if (fieldName.toLowerCase().contains(FIELD_AMOUNT.toLowerCase()) && fieldValue != null) {
                                        Double fieldValueDouble = Double.parseDouble(fieldValue);
                                        fieldValue = format.format(fieldValueDouble);
                                        if (favEventCodeList.contains(eventCode) && fieldValueDouble.compareTo(Double.parseDouble(DefaultValue.FAVOURITE_EMPTY_AMOUNT.getValue())) == 0) {
                                            fieldValue = "";
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    logger.warn(String.format("Invalid number value for amount field [%s] : %s", fieldName, fieldValue), e);
                                } catch (Exception e) {
                                    logger.warn(e);
                                }
                                auditDetails.addDetails(fieldName, fieldValue);
                            }
                    );
                    //Set refId
                    if (!eventCode.equals("31016") && !eventCode.equals("31015") && !eventCode.equals("31018") && !eventCode.equals("10009")) {
                        try {
                            String refId = documentContext.read(PATH_REF_ID, String.class);
                            if (StringUtils.isNotEmpty(refId)) {
                                auditDetails.addDetails("Ref No", refId);
                            }
                        } catch (Exception e) {
                            logger.warn(e.getMessage());
                        }
                    }
                }
            }
        }

        String eventName = auditEventConfig.getEventName();
        logger.info("        detailsTableName: " + detailsTableName);
        logger.info("        eventName: " + eventName);
        auditDetails.setAuditId(auditId);
        auditDetails.setEventCode(eventCode);
        auditDetails.setEventName(eventName);
        auditDetails.setTimestamp(sTimestamp);
        auditDetails.setDeviceId(deviceId);
        auditDetails.setChannel(channel);
        auditDetails.setStatusCode(statusCode);
        auditDetails.setStatusDescription(statusDescription);
        //DCPBL-13896
        auditDetails.setIp(audit.getIpAddress());
        auditDetails.setUsername(audit.getUsername());
        auditDetails.setCisNo(audit.getCisNo());
        if (StringUtils.isNotEmpty(audit.getStatusCode())) {
            Integer successCount = lookupStatusRepository.getSuccessStatusCount(audit.getStatusCode());
            if (successCount.intValue() > 0) {
                auditDetails.setStatusSummary(SUCCESS_STATUS);
            } else {
                auditDetails.setStatusSummary(FAILED_STATUS);
            }
        }
        logger.debug("    auditDetails: " + auditDetails);

        AuditDetailsActivity auditDetailsActivity = new AuditDetailsActivity();
        auditDetailsActivity.setActivity(auditDetails);

        return auditDetailsActivity;
    }

    private AuditDetailsTableRepository getDetailsRepository(String detailsTableName) {
        if (DetailTable.DCP_AUDIT_BILL_PAYMENT.name().equalsIgnoreCase(detailsTableName))
            return auditBillPaymentRepository;

        if (DetailTable.DCP_AUDIT_FUND_TRANSFER.name().equalsIgnoreCase(detailsTableName))
            return auditFundTransferRepository;

        if (DetailTable.DCP_AUDIT_MISC.name().equalsIgnoreCase(detailsTableName))
            return auditMiscRepository;

        if (DetailTable.DCP_AUDIT_PROFILE.name().equalsIgnoreCase(detailsTableName))
            return auditProfileRepository;

        if (DetailTable.DCP_AUDIT_TOPUP.name().equalsIgnoreCase(detailsTableName))
            return auditTopupRepository;

        throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Invalid value for detailsTableName: " + detailsTableName);
    }
}
