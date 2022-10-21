package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.*;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.RegistrationToken;
import com.rhbgroup.dcpbo.customer.repository.AuditDetailConfigRepo;
import com.rhbgroup.dcpbo.customer.repository.RegistrationTokenRepo;
import com.rhbgroup.dcpbo.customer.service.AuditRegistrationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuditRegistrationServiceImpl implements AuditRegistrationService {

    @Autowired
    RegistrationTokenRepo registrationTokenRepository;

    @Autowired
    AuditDetailConfigRepo auditDetailConfigRepository;

    private static Logger logger = LogManager.getLogger(AuditRegistrationServiceImpl.class);

    public static final int PAGE_SIZE = 15;
    private static final String FORMAT_DATE = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String STRING_DATE_REPLACEMENT = "+";
    private static final String STRING_DATE_SPACES = "\\s";
    private static final String EMPTY_VALUE = "empty";
    private static final String EMPTY_STRING = "";
    private static final String EVENT_CODE = "10000";


    @Override
    public BoData getAuditRegistrationDetails(String token) {

        logger.debug("getAuditRegistrationDetails()");

        RegistrationToken registrationToken = registrationTokenRepository.findByToken(token);

        if (registrationToken == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE,
                    "Cannot find RegistrationToken for token: " + token);

        String channel = registrationToken.getChannel();
        String auditAdditionalData = registrationToken.getAuditAdditionalData();
        Date updatedTime = registrationToken.getUpdatedTime();
        String deviceId = registrationToken.getDeviceId();
        String ipAddress = registrationToken.getIpAddress();
        String userName = registrationToken.getUsername();
        String cisNo = registrationToken.getCisNo();
        String eventCode = EVENT_CODE;
        String eventName = "User Registration";

        AuditDetails auditDetails = new AuditDetails();

        auditDetails.setAuditId(0);
        auditDetails.setEventCode(eventCode);
        auditDetails.setEventName(eventName);

        if (updatedTime == null)
            throw new CommonException(CommonException.GENERIC_ERROR_CODE,
                    "Null return value for registrationToken.getTimestamp()");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_DATE);
        String sTimestamp = simpleDateFormat.format(updatedTime);
        auditDetails.setTimestamp(sTimestamp);
        auditDetails.setDeviceId(deviceId);
        auditDetails.setChannel(channel);
        auditDetails.setIp(ipAddress);
        auditDetails.setUsername(userName);
        auditDetails.setCisNo(cisNo);

        String statusCode = null;
        String statusDescription = null;
        String statusSummary = null;

        Step step = JsonUtil.jsonToObject(auditAdditionalData, Step.class);

        if (step.getStep3() != null) {
            statusCode = step.getStep3().getStatus().getCode();
            statusDescription = step.getStep3().getStatus().getDescription();
            statusSummary = step.getStep3().getStatus().getDescription();
        } else if (step.getStep2() != null) {
            statusCode = step.getStep2().getStatus().getCode();
            statusDescription = step.getStep2().getStatus().getDescription();
            statusSummary = step.getStep2().getStatus().getDescription();
        } else if (step.getStep1() != null) {
            statusCode = step.getStep1().getStatus().getCode();
            statusDescription = step.getStep1().getStatus().getDescription();
            statusSummary = step.getStep1().getStatus().getDescription();
        }

        auditDetails.setStatusCode(statusCode);
        auditDetails.setStatusDescription(statusDescription);
        auditDetails.setStatusSummary(statusSummary);

        LinkedHashMap<String, String> hashmap = new LinkedHashMap<>();
        if (step.getStep1() != null) {
            hashmap.put("Step1 Description", step.getStep1().getDescription());
            hashmap.put("Step1 Status Code", step.getStep1().getStatus().getCode());
        }

        hashmap.put("Sanction Screening", step.getIsSanctioned());
        hashmap.put("Nationality", step.getNationalityDesc());

        if (step.getStep1b() != null) {
            hashmap.put("Step1b Description", step.getStep1b().getDescription());
            hashmap.put("Step1b Status Code", step.getStep1b().getStatus().getCode());
        }
        if (step.getStep2() != null) {
            hashmap.put("Step2 Description", step.getStep2().getDescription());
            hashmap.put("Step2 Status Code", step.getStep2().getStatus().getCode());
        }
        if (step.getStep3() != null) {
            hashmap.put("Step3 Description", step.getStep3().getDescription());
            hashmap.put("Step3 Status Code", step.getStep3().getStatus().getCode());
        }

        hashmap.put("Credit/Debit/Prepaid/Loan Number", step.getRegCardLoanNo());
        hashmap.put("ID Number", step.getIdNo());
        hashmap.put("ID Type", step.getIdType());
        hashmap.put("Mobile Number", step.getMobileNo());
        hashmap.put("Email Address", step.getEmail());
        hashmap.put("Customer Type", step.getIsPremier());
        hashmap.put("T&C Acceptance", step.getTermsAndConditionsAccepted());
        hashmap.put("T&C Accepted Date & Time", step.getTermsAndConditionsAcceptedDate());
        hashmap.put("T&C Category", step.getTnccategory());
        hashmap.put("PDPA version", step.getPdpaversion());
        hashmap.put("T&C version", step.getTncversion());
        hashmap.put("Cross Selling Accepted Date & Time", step.getCrossSellingAcceptedDate());
        hashmap.put("Cross-Selling Accepted (RHB Banking Group)", step.getCrossSellingRHBGroupAccepted());
        hashmap.put("Cross-Selling Accepted (Strategic Partner/Third Party)", step.getCrossSellingRHBPartnersAccepted());
        hashmap.put("Registered OTP Mobile Number", step.getOtpMobileNo());
        hashmap.put("OTP Registration Date & Time", step.getOtpRegistrationDate());

        for (Map.Entry<String, String> entry : hashmap.entrySet()) {
            auditDetails.addDetails(entry.getKey(), entry.getValue());
        }

        AuditDetailsActivity auditDetailsActivity = new AuditDetailsActivity();
        auditDetailsActivity.setActivity(auditDetails);

        return auditDetailsActivity;
    }

    @Override
    public BoData listing(String cisNo, Integer pageNo, String frDateStr, String toDateStr) {

        Map<String, Timestamp> formattedDate = parseInputDate(frDateStr, toDateStr);
        Timestamp frDate = formattedDate.get("frDate");
        Timestamp toDate = formattedDate.get("toDate");
        String status = " ";
        Integer totalPageNum = 0;
        Integer offset = 0;
        AuditPagination auditPagination = new AuditPagination();
        auditPagination.setPageNum(1);

        offset = (pageNo - 1) * PAGE_SIZE;
        auditPagination.setPageNum(pageNo);

        AuditEvent auditEvent = new AuditEvent();
        List<Object> registrationTokens = registrationTokenRepository.findByCisNo(cisNo, frDate, toDate, offset, PAGE_SIZE);

        totalPageNum = (int) Math.ceil((double) registrationTokenRepository.getCountByCisNo(cisNo, frDate, toDate) / PAGE_SIZE);

        if (registrationTokens.isEmpty()) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE,
                    "Cannot find RegistrationToken for cisNo: " + cisNo);
        }

        for (Object singleRegToken : registrationTokens) {
            Object[] obj = (Object[]) singleRegToken;

            Event event = new Event();
            event.setEventId("0");
            event.setEventCode(EVENT_CODE);
            event.setEventCategoryId("10");
            event.setEventName("User Registration");

            AdditionalDataDescription additionalDataDescription = JsonUtil.jsonToObject((String) obj[0], AdditionalDataDescription.class);

            List<String> details = new ArrayList<String>();
            details.add("Card/Loan No:");
            details.add(additionalDataDescription.getRegCardLoanNo());
            details.add("\n" + "ID No: ");
            details.add(additionalDataDescription.getIdNo());
            details.add("\n" + "Mobile No:");
            details.add(additionalDataDescription.getMobileNo());
            if (additionalDataDescription.getStep1() != null) {
                details.add("\n" + "Step 1:" + additionalDataDescription.getStep1().getDescription());
                details.add("(" + additionalDataDescription.getStep1().getStatus().getDescription() + ")");
            }
            if (additionalDataDescription.getStep2() != null) {
                details.add("\n" + "Step 2:" + additionalDataDescription.getStep2().getDescription());
                details.add("(" + additionalDataDescription.getStep2().getStatus().getDescription() + ")");
            }
            if (additionalDataDescription.getStep3() != null) {
                details.add("\n" + "Step 3:" + additionalDataDescription.getStep3().getDescription());
                details.add("(" + additionalDataDescription.getStep3().getStatus().getDescription() + ")");
            }

            if (additionalDataDescription.getStep3() != null) {
                status = additionalDataDescription.getStep3().getStatus().getCode();
            } else if (additionalDataDescription.getStep2() != null) {
                status = additionalDataDescription.getStep2().getStatus().getCode();
            } else if (additionalDataDescription.getStep1() != null) {
                status = additionalDataDescription.getStep1().getStatus().getCode();
            }

            String detail = details.stream().map(String::valueOf).collect(Collectors.joining(""));

            String eventStatus = getStatus(status);

            event.setStatus(eventStatus);
            event.setDescription(detail);
            event.setChannel((String) obj[1]);
            Timestamp timeStamp = (Timestamp) obj[2];
            String updatedTime = timeStamp.toString();
            event.setTimestamp(updatedTime);
            event.setRefId((String) obj[3]);
            auditEvent.addEvent(event);
        }

        EventCategory eventCategory = new EventCategory();
        eventCategory.setEventCategoryId("10");
        eventCategory.setEventCategoryName("Others");
        auditEvent.addEventCategory(eventCategory);

        //Constructing pagination
        if (registrationTokens.size() < PAGE_SIZE) {
            auditPagination.setPageIndicator("L");
        } else {
            auditPagination.setPageIndicator("N");
        }
        auditPagination.setActivityCount(registrationTokens.size());
        auditPagination.setPageNum(pageNo);
        auditPagination.setTotalPageNum(totalPageNum);
        auditEvent.setPagination(auditPagination);

        return auditEvent;
    }

    private String getStatus(String code) {
        String value = null;
        if (code.equals(EVENT_CODE)) {
            value = "Success";
        } else {
            value = "Failed";
        }
        return value;
    }

    public Map<String, Timestamp> parseInputDate(String frDateStr, String toDateStr) {
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
                logger.info("Issue parsing time ", ex);
            }
            frDateStr = dateFormatter.format(prevSix);
        }

        Timestamp frDate = new Timestamp(new Date().getTime());
        Timestamp toDate = new Timestamp(new Date().getTime());
        try {
            frDate = new Timestamp(dateFormatter.parse(frDateStr).getTime());
            toDate = new Timestamp(dateFormatter.parse(toDateStr).getTime());
        } catch (ParseException ex) {
            logger.info("Parse exeption ", ex);
        }

        HashMap<String, Timestamp> formattedDate = new HashMap<>();

        formattedDate.put("frDate", frDate);
        formattedDate.put("toDate", toDate);

        return formattedDate;
    }

}
