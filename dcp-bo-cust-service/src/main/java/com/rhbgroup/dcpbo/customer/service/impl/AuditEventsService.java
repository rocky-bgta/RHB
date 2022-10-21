package com.rhbgroup.dcpbo.customer.service.impl;

import com.jayway.jsonpath.JsonPath;
import com.rhbgroup.dcpbo.customer.dto.Investigation;
import com.rhbgroup.dcpbo.customer.dto.InvestigationPagination;
import com.rhbgroup.dcpbo.customer.enums.DefaultValue;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AuditEventsService {

    @Autowired
    private DcpAuditFundTransferRepository dcpAuditFundTransferRepository;

    @Autowired
    private DcpAuditProfileRepository dcpAuditProfileRepository;

    @Autowired
    private DcpAuditBillPaymentRepository dcpAuditBillPaymentRepository;

    @Autowired
    private DcpAuditTopupRepository dcpAuditTopupRepository;

    @Autowired
    private DcpAuditMiscRepository dcpAuditMiscRepository;

    @Autowired
    private DcpAuditEventConfigRepository dcpAuditEventConfigRepository;

    @Autowired
    private AuditSummaryConfigRepository auditSummaryConfigRepository;

    private final Logger log = LogManager.getLogger(AuditEventsService.class);

    private final String FORMAT_DATE = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String EMPTY_VALUE = "empty";
    private static final String EMPTY_STRING = "";
    private final String STRING_DATE_REPLACEMENT = "+";
    private final String STRING_DATE_SPACES = "\\s";
    private static final String FIELD_AMOUNT = "amount";
    private static final String FORMAT_AMOUNT = "0.00";

    public HashMap parseInputDate(String frDateStr, String toDateStr) {
        //Parsing inputted Date Format
        frDateStr = frDateStr.replaceAll(STRING_DATE_SPACES, STRING_DATE_REPLACEMENT);
        toDateStr = toDateStr.replaceAll(STRING_DATE_SPACES, STRING_DATE_REPLACEMENT);
        Date prevSix = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat(FORMAT_DATE);
        if (toDateStr.equals(EMPTY_VALUE) || toDateStr.equals(EMPTY_STRING)) {
            toDateStr = dateFormatter.format(new Date());
        }
        if (frDateStr.equals(EMPTY_VALUE) || frDateStr.equals(EMPTY_STRING)) {
            try {
                Date date = dateFormatter.parse(toDateStr);
                java.sql.Date toDateFormatted = new java.sql.Date(date.getTime());

                Calendar cal = Calendar.getInstance();
                cal.setTime(toDateFormatted);
                cal.add(Calendar.MONTH, -6);

                prevSix = cal.getTime();
            } catch (Exception ex) {
                log.info("Issue parsing time ", ex);
            }
            frDateStr = dateFormatter.format(prevSix);
        }

        Timestamp frDate = new Timestamp(new Date().getTime());
        Timestamp toDate = new Timestamp(new Date().getTime());
        try {
            frDate = new Timestamp(dateFormatter.parse(frDateStr).getTime());
            toDate = new Timestamp(dateFormatter.parse(toDateStr).getTime());
        } catch (ParseException ex) {
            log.info("Parse exeption ", ex);
        }

        HashMap formattedDate = new HashMap();

        formattedDate.put("frDate", frDate);
        formattedDate.put("toDate", toDate);

        return formattedDate;
    }

    public String getDescription(Optional<AuditEventConfig> auditEventConfigOptional, Integer auditId, String auditEventCode) {

        String auditDetails = null;
        Boolean isWithDetails = true;
        String description = null;

        List<AuditSummaryConfig> auditSummaryConfigPathList = auditSummaryConfigRepository.findPathsByEventCode(auditEventCode);
        DecimalFormat format = new DecimalFormat(FORMAT_AMOUNT);

        if (auditEventConfigOptional.get().getDetailsTableName().equalsIgnoreCase("DCP_AUDIT_FUND_TRANSFER")) {
            AuditFundTransfer auditFundTransfer = dcpAuditFundTransferRepository.getDetail(auditId);
            if (auditFundTransfer != null)
                auditDetails = auditFundTransfer.getDetails();
        } else if (auditEventConfigOptional.get().getDetailsTableName().equalsIgnoreCase("DCP_AUDIT_PROFILE")) {
            AuditProfile auditProfile = dcpAuditProfileRepository.getDetail(auditId);
            if (auditProfile != null)
                auditDetails = auditProfile.getDetails();
        } else if (auditEventConfigOptional.get().getDetailsTableName().equalsIgnoreCase("DCP_AUDIT_BILL_PAYMENT")) {
            AuditBillPayment auditBillPayment = dcpAuditBillPaymentRepository.getDetail(auditId);
            if (auditBillPayment != null)
                auditDetails = auditBillPayment.getDetails();
        } else if (auditEventConfigOptional.get().getDetailsTableName().equalsIgnoreCase("DCP_AUDIT_TOPUP")) {
            AuditTopup auditTopup = dcpAuditTopupRepository.getDetail(auditId);
            if (auditTopup != null)
                auditDetails = auditTopup.getDetails();
        } else if (auditEventConfigOptional.get().getDetailsTableName().equalsIgnoreCase("DCP_AUDIT_MISC")) {
            AuditMisc auditMisc = dcpAuditMiscRepository.getDetail(auditId);
            if (auditMisc != null)
                auditDetails = auditMisc.getDetails();
        } else {
            isWithDetails = false;
        }

        if (isWithDetails && auditDetails != null) {
            Iterator<AuditSummaryConfig> iter = auditSummaryConfigPathList.iterator();
            StringBuilder sb = new StringBuilder();
            List<String> favEventCodeList = dcpAuditEventConfigRepository.getEventCodesFavourites();
            while (iter.hasNext()) {
            	AuditSummaryConfig auditSummaryConfig = iter.next();
                String fieldName = auditSummaryConfig.getFieldName();
                String path = auditSummaryConfig.getPath();
                if(fieldName == null) {
                	fieldName = StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(path.substring(path.lastIndexOf('.')+1)),' '));
                }
                String fieldValue = null;
                try {
                    fieldValue = JsonPath.parse(auditDetails).read(path, String.class);
                    if (fieldName.toLowerCase().contains(FIELD_AMOUNT.toLowerCase())) {
                        Double fieldValueDouble = Double.parseDouble(fieldValue);
                        fieldValue = format.format(fieldValueDouble);
                        if (favEventCodeList.contains(auditEventCode) && fieldValueDouble.compareTo(Double.parseDouble(DefaultValue.FAVOURITE_EMPTY_AMOUNT.getValue())) == 0) {
                            fieldValue = null;
                        }
                    }
                } catch (NumberFormatException ex) {
                    log.warn(String.format("Invalid number value for amount field [%s] : %s", fieldName, fieldValue), ex);
                } catch (Exception ex) {
                    log.warn(ex.getMessage());
                }

                if (StringUtils.isNotEmpty(fieldValue)) {
                    if (StringUtils.isNotEmpty(fieldName)) {
                        sb.append(fieldName);
                        sb.append(" : ");
                    }
                    sb.append(fieldValue);
                    if (iter.hasNext()) {
                        sb.append("\n");
                    }
                }
            }
            description = sb.toString();
        }
        return description;
    }

    public String getAuditDetails(Optional<AuditEventConfig> auditEventConfigOptional, Integer auditId) {

        String auditDetails = null;

        if (auditEventConfigOptional.get().getDetailsTableName().equalsIgnoreCase("DCP_AUDIT_FUND_TRANSFER")) {
            AuditFundTransfer auditFundTransfer = dcpAuditFundTransferRepository.getDetail(auditId);
            if (auditFundTransfer != null)
                auditDetails = auditFundTransfer.getDetails();
        } else if (auditEventConfigOptional.get().getDetailsTableName().equalsIgnoreCase("DCP_AUDIT_PROFILE")) {
            AuditProfile auditProfile = dcpAuditProfileRepository.getDetail(auditId);
            if (auditProfile != null)
                auditDetails = auditProfile.getDetails();
        } else if (auditEventConfigOptional.get().getDetailsTableName().equalsIgnoreCase("DCP_AUDIT_BILL_PAYMENT")) {
            AuditBillPayment auditBillPayment = dcpAuditBillPaymentRepository.getDetail(auditId);
            if (auditBillPayment != null)
                auditDetails = auditBillPayment.getDetails();
        } else if (auditEventConfigOptional.get().getDetailsTableName().equalsIgnoreCase("DCP_AUDIT_TOPUP")) {
            AuditTopup auditTopup = dcpAuditTopupRepository.getDetail(auditId);
            if (auditTopup != null)
                auditDetails = auditTopup.getDetails();
        } else if (auditEventConfigOptional.get().getDetailsTableName().equalsIgnoreCase("DCP_AUDIT_MISC")) {
            AuditMisc auditMisc = dcpAuditMiscRepository.getDetail(auditId);
            if (auditMisc != null)
                auditDetails = auditMisc.getDetails();
        }

        return auditDetails;
    }
}
