package com.rhbgroup.dcpbo.customer.service.impl;

import com.jayway.jsonpath.JsonPath;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dcpbo.BoAuditDetails;
import com.rhbgroup.dcpbo.customer.dcpbo.BoAuditModule;
import com.rhbgroup.dcpbo.customer.dcpbo.BoAuditSummaryConfig;
import com.rhbgroup.dcpbo.customer.dcpbo.BoConfigFunctionModule;
import com.rhbgroup.dcpbo.customer.dto.BoAuditEvent;
import com.rhbgroup.dcpbo.customer.dto.BoAuditEvents;
import com.rhbgroup.dcpbo.customer.dto.BoAuditPagination;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.BoAuditService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class BoAuditServiceImpl implements BoAuditService {

    private static Logger logger = LogManager.getLogger(BoAuditServiceImpl.class);

    public static final String VALUE = "value";
    public static final String FIELD_NAME = "parameters";
    public static final String CUSTOMER_SIGNON_ID = "username";
    public static final String CUSTOMER_ID = "customerId";
    public static final String DETAILS = "details";
    public static final String ACCESS_STAFF_ACC = "accessStaffAcc";
    public static final String MAKER_ACTIVITY = "makerActivity";
    public static final String APPROVER_ACTIVITY = "approverActivity";
    private static final int SWITCH_ON = 0;
    private static final int SWITCH_OFF = 1;
    public static final int PAGE_SIZE = 25;
    private static final String ERROR_MESSAGE = "Error parsing parameters detail";

    @Autowired
    BoAuditRepository boAuditRepository;

    @Autowired
    BoAuditDetailsRepository boAuditDetailsRepository;

    @Autowired
    BoAuditSummaryConfigRepository boAuditSummaryConfigRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    BoConfigFunctionModuleRepository boConfigFunctionModuleRepository;

    @Override
    public BoData fetchAuditListBy(List<Integer> moduleList, String username, String selectedDate, Integer pageNo) {
        logger.info("fetchAuditListBy... ");
        logger.debug("moduleList: " + moduleList);
        logger.debug("username: " + username);
        logger.debug("selectedDate: " + selectedDate);
        logger.debug("pageNo: " + pageNo);
        Optional<String> usernameOpt = notBlank(username);
        Optional<String> selectedDateOpt = notBlank(selectedDate);

        List<BoConfigFunctionModule> boConfigFunctionModule = boConfigFunctionModuleRepository.getBoConfigFunctionModule(moduleList);
        Map<Integer, String> configMap = boConfigFunctionModule.stream().collect(
                Collectors.toMap(BoConfigFunctionModule::getId, BoConfigFunctionModule::getModuleName));
        List<Integer> functionList = configMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());

        Integer switchFunction = SWITCH_ON;
        if (configMap.containsValue("User Management")) {
            switchFunction = SWITCH_OFF;
        }
        Integer switchUsername = SWITCH_OFF;
        if (usernameOpt.isPresent()) {
            switchUsername = SWITCH_ON;
        }
        Integer switchDate = SWITCH_OFF;
        if (selectedDateOpt.isPresent()) {
            switchDate = SWITCH_ON;
        }

        logger.debug("SWITCH_ON represent as " + SWITCH_ON + ", SWITCH_OFF represent as " + SWITCH_OFF);
        logger.debug("switchFunction: " + switchFunction + " switchUsername: " + switchUsername + " switchDate: " + switchDate);

        int offset = pageNo - 1;

        BoAuditEvents events = new BoAuditEvents();
        PageRequest pageRequest = new PageRequest(offset, PAGE_SIZE);
        logger.debug("pageRequest offset: " + pageRequest.getOffset());
        Page<BoAuditModule> page = boAuditRepository.getBoAudit(
                functionList,
                switchFunction,
                username,
                switchUsername,
                selectedDate,
                switchDate,
                pageRequest);

        String pageIndicator = page.getTotalPages() > pageNo ? "N" : "L";
        BoAuditPagination boAuditPagination = new BoAuditPagination();
        boAuditPagination.setPageNo(pageNo);
        boAuditPagination.setTotalPageNo(page.getTotalPages());
        boAuditPagination.setActivityCount(page.getTotalElements());
        boAuditPagination.setPageIndicator(pageIndicator);
        boAuditPagination.setPageSize(PAGE_SIZE);
        logger.debug(page.getTotalElements());
        if (page.getNumberOfElements() == 0) {
            logger.warn("No audit log found");
        }

        List<BoAuditEvent> boAuditEventList = new ArrayList<>();
        try {
            logger.info("Stream boAuditEventList");
            boAuditEventList = page.getContent().stream().map(boAuditModule -> {
                logger.info("audit_id: " + boAuditModule.getId());
                logger.info("event_id: " + boAuditModule.getEventId());
                BoAuditEvent boAuditEvent = new BoAuditEvent();
                boAuditEvent.setId(boAuditModule.getId());
                boAuditEvent.setModuleName(getModuleName(configMap, boAuditModule.getFunctionId()));
                boAuditEvent.setActivityName(boAuditModule.getActivityName());
                boAuditEvent.setUsername(boAuditModule.getUsername());
                boAuditEvent.setTimestamp(boAuditModule.getTimestamp());

                BoAuditDetails boAuditDetails = getBoAuditDetails(boAuditModule);
                List<BoAuditSummaryConfig> auditSummaryConfig = getSummaryConfig(boAuditModule.getEventId());
                Map<String, String> payload = getPayloadFromDetails(auditSummaryConfig, boAuditDetails.getDetails());

                logger.debug("payload - customerSignOnId : " + payload.get(CUSTOMER_SIGNON_ID));
                logger.debug("payload - event: " + payload.get("event"));
                boAuditEvent.setCustomerSignOnId(payload.get(CUSTOMER_SIGNON_ID));
                boAuditEvent.setEvent(payload.get("event"));
                boAuditEvent.setAccessStaffAccount(payload.get(ACCESS_STAFF_ACC));
                boAuditEvent.setDetails(payload.get(DETAILS));
                if (payload.get(MAKER_ACTIVITY) != null && payload.get(MAKER_ACTIVITY).equals("y")) {
                    logger.info("Maker Activity set.");
                    boAuditEvent.setMakerActivity(boAuditModule.getActivityName());
                }
                boAuditEvent.setApproverActivity(payload.get(APPROVER_ACTIVITY));
                return boAuditEvent;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Caught unexpected error :: ", e);
        }

        logger.debug("boAuditEventList size: " + boAuditEventList.size());
        events.getEvent().addAll(boAuditEventList);
        events.setPagination(boAuditPagination);

        return events;
    }

    private String getModuleName(Map<Integer, String> configMap, Integer functionId) {
        return configMap.get(functionId) != null ? configMap.get(functionId) : "User Management";
    }

    private BoAuditDetails getBoAuditDetails(BoAuditModule boAuditModule) {
        logger.info("getDetails...");
        BoAuditDetails boAuditDetails = null;

        if (boAuditModule.getDetailsTableName().equalsIgnoreCase("TBL_BO_AUDIT_BILLER")) {
            logger.info("getDetails from TBL_BO_AUDIT_BILLER");
            boAuditDetails = boAuditDetailsRepository.getAuditBillerDetails(boAuditModule.getId());
        } else if (boAuditModule.getDetailsTableName().equalsIgnoreCase("TBL_BO_AUDIT_FUND")) {
            logger.info("getDetails from TBL_BO_AUDIT_FUND");
            boAuditDetails = boAuditDetailsRepository.getAuditFundDetails(boAuditModule.getId());
        } else if (boAuditModule.getDetailsTableName().equalsIgnoreCase("TBL_BO_AUDIT_FUNDSETUP")) {
            logger.info("getDetails from TBL_BO_AUDIT_FUNDSETUP");
            boAuditDetails = boAuditDetailsRepository.getAuditFundsetupDetails(boAuditModule.getId());
        } else if (boAuditModule.getDetailsTableName().equalsIgnoreCase("TBL_BO_AUDIT_PROVIDE_ASSIST")) {
            logger.info("getDetails from TBL_BO_AUDIT_PROVIDE_ASSIST");
            boAuditDetails = boAuditDetailsRepository.getAuditProvideAssistanceDetails(boAuditModule.getId());
        } else if (boAuditModule.getDetailsTableName().equalsIgnoreCase("TBL_BO_AUDIT_SM_DOWNTIME")) {
            logger.info("getDetails from TBL_BO_AUDIT_SM_DOWNTIME");
            boAuditDetails = boAuditDetailsRepository.getAuditSmDowntimeDetails(boAuditModule.getId());
        } else if (boAuditModule.getDetailsTableName().equalsIgnoreCase("TBL_BO_AUDIT_USERMGMT")) {
            logger.info("getDetails from TBL_BO_AUDIT_USERMGMT");
            boAuditDetails = boAuditDetailsRepository.getAuditUsermgmtDetails(boAuditModule.getId());
        }
        return boAuditDetails;
    }

    private List<BoAuditSummaryConfig> getSummaryConfig(Integer eventId) {
        return boAuditSummaryConfigRepository.findByEventId(eventId);
    }

    public Map<String, String> getPayloadFromDetails(List<BoAuditSummaryConfig> auditSummaryConfig,
                                                     String auditDetails) {

        // Need to rewrite this section code, messy code due to rush to resolve prod issue.
        Map<String, String> attributes = new LinkedHashMap<>();
        if (auditDetails == null || auditSummaryConfig == null || auditSummaryConfig.isEmpty()) {
            logger.error("boAuditDetail / auditSummaryConfig is empty.");
            return attributes; // Return empty list if no details.
        }

        logger.info("processing detail attributes..");

        for (BoAuditSummaryConfig auditSum : auditSummaryConfig) {
            String fieldName = auditSum.getFieldName();
            String path = auditSum.getPath();
            String type = auditSum.getType();
            logger.debug("fieldName: " + fieldName);
            logger.debug("path: " + path);

            handlingMakerActivity(attributes, fieldName);
            extractPath(auditDetails, attributes, fieldName, path, type);
            extractParameterName(auditDetails, attributes, fieldName, path, type);
            extractParameterFieldName(auditDetails, attributes, fieldName, path, type);
            extractAdditionalData(attributes, auditDetails, fieldName, path);

        }

        logger.info("Retrieve user profile information.");
        UserProfile userProfile = null;
        if (Optional.ofNullable(attributes.get(CUSTOMER_ID)).isPresent()) {
            logger.debug(CUSTOMER_ID + ": " + attributes.get(CUSTOMER_ID));
            userProfile = userProfileRepository.findByCustomerId(Integer.parseInt(attributes.get(CUSTOMER_ID)));
            attributes.put(CUSTOMER_SIGNON_ID, userProfile.getUsername());
        } else if (Optional.ofNullable(attributes.get(CUSTOMER_SIGNON_ID)).isPresent()) {
            logger.debug(CUSTOMER_SIGNON_ID + ": " + attributes.get(CUSTOMER_SIGNON_ID));
            userProfile = userProfileRepository.findByUsername(attributes.get(CUSTOMER_SIGNON_ID));
        }

        if (userProfile != null) {
            attributes.put(ACCESS_STAFF_ACC, userProfile.isStaff() ? "Yes" : "No");
        }

        return attributes;
    }

    private void handlingMakerActivity(Map<String, String> attributes, String fieldName) {
        logger.info("handlingMakerActivity");
        if (!fieldName.equals(MAKER_ACTIVITY)) {
            return;
        }
        attributes.put(fieldName, "y");
    }

    private void extractPath(String auditDetails, Map<String, String> attributes, String fieldName, String path, String type) {
        logger.info("extractPath");
        if (type.equals("path")) {
            if (!notBlank(fieldName).isPresent()) {
                logger.info("generate field name from path.");
                fieldName = StringUtils.capitalize(
                        StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(path.substring(path.lastIndexOf('.') + 1)), ' '));
            }
            try {
                String fieldValue = JsonPath.parse(auditDetails).read(path, String.class);
                logger.debug("fieldValue: " + fieldValue);
                attributes.put(fieldName, fieldValue);
            } catch (Exception e) {
                logger.error("Error parsing path detail::", e);
            }
        }
    }

    public void extractParameterName(String auditDetails, Map<String, String> attributes, String fieldName, String path, String type) {
        logger.info("extractParameterName");
        if (type.equals("parameter") && fieldName.equals(FIELD_NAME)) {
            try {
                List<Map<String, Object>> parameters = JsonPath.parse(auditDetails).read(path);
                for (Map<String, Object> parameter : parameters) {
                    String key = (String) parameter.get("name"); // from json payload parameters
                    logger.debug("key: " + key);
                    if (key == null) {
                        break;
                    }

                    if (Pattern.compile(Pattern.quote(CUSTOMER_ID), Pattern.CASE_INSENSITIVE).matcher(key).find()) {
                        logger.debug("value :" + parameter.get(VALUE));
                        attributes.put(CUSTOMER_ID, getValue(parameter));
                    }
                    getAdditionalDetails(attributes, key, parameter);
                }
            } catch (Exception e) {
                logger.error("Error parsing parameters detail::", e);
            }
        }
    }

    public void extractParameterFieldName(String auditDetails, Map<String, String> attributes, String fieldName, String path, String type) {
        logger.info("extractParameterFieldName");
        if (type.equals("parameter") && fieldName.equals(FIELD_NAME)) {
            try {
                List<Map<String, Object>> parameters = JsonPath.parse(auditDetails).read(path);
                StringBuilder sb = new StringBuilder();
                for (Map<String, Object> parameter : parameters) {
                    if (parameter.get("fieldName") == null) {
                        break;
                    }
                    if (notBlank((String) parameter.get(VALUE)).isPresent()) {
                        sb.append(String.format("%s: %s%n", parameter.get("fieldName"), getValue(parameter)));
                    }
                }
                if (sb.length() > 0) {
                    attributes.put(DETAILS, sb.toString());
                }
            } catch (Exception e) {
                logger.error("Error parsing parameters detail::", e);
            }
        }
    }

    public void getAdditionalDetails(Map<String, String> attributes, String key, Map<String, Object> parameter) {
        if (key.equals("accountNo")) {
            attributes.put(DETAILS, getValue(parameter));
        }
        if (key.equals("sCardId")) {
            attributes.put(DETAILS, getValue(parameter));
        }
        if (key.equals("request")) {
            LinkedHashMap<String, String> value = (LinkedHashMap<String, String>) parameter.get(VALUE);
            attributes.put(DETAILS, value.get("accountNo"));
        }
        if (key.equals("delApprovalRequest")) {
            LinkedHashMap<String, Object> value = (LinkedHashMap<String, Object>) parameter.get(VALUE);
            String details = value.entrySet()
                    .stream()
                    .filter(e -> e.getValue() != null)
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining("\n"));
            attributes.put(DETAILS, details);
        }
        if (key.equals("workflowDeviceRequest")) {
            LinkedHashMap<String, Object> value = (LinkedHashMap<String, Object>) parameter.get(VALUE);
            String reason = value.entrySet()
                    .stream()
                    .filter(e -> e.getValue() != null)
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining("\n"));
            attributes.put(APPROVER_ACTIVITY, reason);
        }
    }

    public void extractAdditionalData(Map<String, String> attributes, String auditDetails, String fieldName, String path) {
        logger.info("extractAdditionalData");
        if (!fieldName.equals("additionalData")) {
            return;
        }
        try {
            LinkedHashMap<String, Object> additionalData = JsonPath.parse(auditDetails).read(path);
            String details = additionalData.entrySet()
                    .stream()
                    .filter(data -> data.getValue() != null)
                    .map(data -> data.getKey() + ": " + data.getValue())
                    .collect(Collectors.joining("\n"));
            attributes.put(DETAILS, details);
        } catch (Exception e) {
            logger.error(ERROR_MESSAGE, e);
        }
    }

    private Optional<String> notBlank(String s) {
        return (s == null || s.chars().allMatch(Character::isWhitespace)) ? Optional.empty() : Optional.of(s);
    }

    public String getValue(Map<String, Object> parameter) {
        return String.valueOf(parameter.get(VALUE) != null ? parameter.get(VALUE) : "");
    }
}
