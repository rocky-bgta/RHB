package com.rhbgroup.dcpbo.customer.service.impl;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dcpbo.BoConfigGeneric;
import com.rhbgroup.dcpbo.customer.dto.*;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.SearchOlaCasaException;
import com.rhbgroup.dcpbo.customer.model.*;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.OlaCasaService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OlaCasaServiceImpl implements OlaCasaService {

    public static final String EVENT_CODE = "10009";
    public static final String EMPTY_STRING = "";
    private static Logger logger = LogManager.getLogger(OlaCasaServiceImpl.class);

    private static final Integer PAGE_SIZE = 15;
    private static final String PAGINATION_LAST = "L";
    private static final String FORMAT_DATE = "yyyy-MM-dd HH:mm:ss";

    @Autowired
    OlaTokenRepository olaTokenRepository;

    @Autowired
    OlaTokenGroupRepository olaTokenGroupRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    BoConfigGenericRepository boConfigGenericRepository;

    @Autowired
    DcpAuditEventConfigRepository auditEventConfigRepository;

    @Autowired
    AuditSummaryConfigRepository auditSummaryConfigRepository;

    @Autowired
    AuditEventsService auditEventsService;

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    AuditDetailConfigRepo auditDetailConfigRepository;

    @Override
    public BoData searchOlaCasaValue(String value, Integer pageNo) {

        logger.info("searchOlaCasaValue");
        Pageable pageable = new PageRequest(pageNo - 1, PAGE_SIZE);

        Page<OlaTokenGroup> olaTokenGroupPage = olaTokenGroupRepository.fetchGroupOlaToken(value, pageable);

        if (olaTokenGroupPage.getTotalElements() < 1) {
            throw new SearchOlaCasaException();
        }

        List<OlaTokenUser> olaTokenUserList = olaTokenGroupPage.getContent().stream().map(ot -> {
            OlaToken olaToken = olaTokenRepository.findFirstByIdNoOrderByUpdatedTimeDesc(ot.getIdNo());

            OlaTokenUser olaTokenUser = new OlaTokenUser();
            olaTokenUser.setUsername(ot.getUsername());
            olaTokenUser.setName(ot.getName());
            olaTokenUser.setIdNo(ot.getIdNo());
            olaTokenUser.setIdType(ot.getIdType());

            if(olaToken != null) {
                logger.info("Initialize OlaToken details");
                olaTokenUser.setEmail(olaToken.getEmail());
                olaTokenUser.setMobileNo(olaToken.getMobileNo());
                olaTokenUser.setMobileNo(olaToken.getMobileNo());
                olaTokenUser.setAaoip(olaToken.getToken());
                olaTokenUser.setStatus(olaToken.getStatus());

                UserProfile userProfile = userProfileRepository.findByUsername(olaToken.getUsername());
                if(userProfile != null) {
                    logger.info("Initialize UserProfile details");
                    olaTokenUser.setCustid(userProfile.getId());
                    olaTokenUser.setCisNo(userProfile.getCisNo());
                    olaTokenUser.setIsPremier(getPremierValue(userProfile));
                    olaTokenUser.setLastLogin(userProfile.getLastLogin());
                }
            }

            return olaTokenUser;
        }).collect(Collectors.toList());


        OlaCasaResponse olaCasaResponse = new OlaCasaResponse();
        olaCasaResponse.getCustomer().addAll(olaTokenUserList);
        olaCasaResponse.setPagination(getPagination(olaTokenGroupPage, pageNo));

        return olaCasaResponse;
    }

    @Override
    public BoData listOlaCasaEvents(String idNo, String fromDate, String toDate, Integer pageNo) {

        logger.info("searchOlaCasaValue");
        Pageable pageable = new PageRequest(pageNo - 1, PAGE_SIZE);

        Map<String, Timestamp> formattedDate = auditEventsService.parseInputDate(fromDate, toDate);
        Page<OlaToken> olaTokens = olaTokenRepository
                .findByIdNoAndCreatedTimeAfterAndCreatedTimeBeforeOrderByCreatedTimeDesc(
                        idNo,
                        formattedDate.get("frDate").toString(),
                        formattedDate.get("toDate").toString(),
                        pageable);
        BoConfigGeneric configGeneric = boConfigGenericRepository.findFirstByConfigTypeAndConfigCode("provide_assistance", "dcp_olacasa_validity");
        AuditEventConfig auditEventConfig = auditEventConfigRepository.findByEventCode(EVENT_CODE);
        List<AuditSummaryConfig> auditSummaryConfigPaths = auditSummaryConfigRepository.findPathsByEventCode(EVENT_CODE);

        List<Event> events = olaTokens.getContent().stream().map(olaToken -> {
            Event event = new Event();
            event.setEventId("0");
            event.setEventCode(EVENT_CODE);
            event.setEventCategoryId("10");
            if(!Objects.isNull(auditEventConfig)) {
                event.setEventName(auditEventConfig.getEventName());
            }

            if(!Objects.isNull(configGeneric)) {
                event.setStatus(getStatus(olaToken, configGeneric.getConfigDesc()));
            }

            event.setDescription(getDescription(olaToken, auditSummaryConfigPaths));

            event.setChannel(olaToken.getChannel());
            event.setTimestamp(getOlaCasaDate(olaToken));
            event.setRefId(olaToken.getRefId());
            return event;
        }).collect(Collectors.toList());

        EventCategory eventCategory = new EventCategory();
        eventCategory.setEventCategoryId("10");
        eventCategory.setEventCategoryName("Others");

        OlaCasaAuditEventResponse response = new OlaCasaAuditEventResponse();
        response.getEventCategory().add(eventCategory);
        response.getEvent().addAll(events);
        response.setPagination(getAuditPagination(olaTokens, pageNo));

        return response;
    }

    @Override
    public BoData getAuditDetails(String token) {

        OlaToken olaToken = olaTokenRepository.findFirstByToken(token);
        BoConfigGeneric configGeneric = boConfigGenericRepository.findFirstByConfigTypeAndConfigCode("provide_assistance", "dcp_olacasa_validity");

        if (Objects.isNull(olaToken)) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find olaToken for token: " + token);
        }

        if (Objects.isNull(configGeneric)) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find boConfigGeneric");
        }

        String eventCode = EVENT_CODE;
        AuditEventConfig auditEventConfig = auditEventConfigRepository.findByEventCode(eventCode);
        if (Objects.isNull(auditEventConfig)) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find auditEventConfig for eventCode: " + eventCode);
        }

        List<AuditDetailConfig> auditDetailConfigList = auditDetailConfigRepository.findAllByEventCode(eventCode);
        if (Objects.isNull(auditDetailConfigList)) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Cannot find auditDetailConfigList for eventCode: " + eventCode);
        }

        AuditDetailsActivity auditDetailsActivity = new AuditDetailsActivity();
        AuditDetails auditDetails = new AuditDetails();
        auditDetails.setAuditId(0);
        auditDetails.setEventCode(eventCode);
        auditDetails.setEventName(auditEventConfig.getEventName());
        auditDetails.setDeviceId(olaToken.getDeviceId());
        auditDetails.setChannel(olaToken.getChannel());
        auditDetails.setStatusCode(olaToken.getApiStatusCode());
        auditDetails.setStatusDescription(olaToken.getApiStatusDesc());
        auditDetails.setRefId(olaToken.getRefId());
        auditDetails.setStatusSummary(getStatusSummaryDesc(olaToken.getApiStatusCode()));
        auditDetails.setIp(olaToken.getIpAddress());
        auditDetails.setUsername(olaToken.getUsername());
        auditDetails.setCisNo(profileRepository.getCisNumberByUsername(olaToken.getUsername()));
        auditDetails.setTimestamp(getOlaCasaDate(olaToken));

        if (Objects.isNull(olaToken.getAuditAdditionalData())) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Null value for documentContext when parsing details: " + olaToken.getAuditAdditionalData());
        }

        // Wrap into additionalData root.
        olaToken.setAuditAdditionalData(String.format("{ \"additionalData\": %s }", olaToken.getAuditAdditionalData()));
        DocumentContext documentContext = JsonPath.parse(olaToken.getAuditAdditionalData());

        auditDetailConfigList.forEach(
                auditDetailConfig -> {
                    String fieldName = auditDetailConfig.getFieldName();
                    String fieldPath = auditDetailConfig.getPath();
                    String fieldValue = null;

                    if (StringUtils.equals(fieldName, "Application Status")) {
                        fieldValue = getStatus(olaToken, configGeneric.getConfigDesc());
                    } else {
                        try {
                            fieldValue = documentContext.read(fieldPath).toString();
                        } catch (Exception e) {
                            String errorMessage = String.format("Exception reading %s at %s from %s", fieldName, fieldPath, olaToken.getAuditAdditionalData());
                            logger.error(errorMessage, e);
                        }
                    }

                    auditDetails.addDetails(fieldName, fieldValue);
                }
        );

        auditDetailsActivity.setActivity(auditDetails);

        return auditDetailsActivity;
    }

    private String getOlaCasaDate(OlaToken olaToken) {
        if(Arrays.asList("I","C").contains(olaToken.getStatus())) {
            return olaToken.getUpdatedTime();
        }
        return olaToken.getCreatedTime();
    }

    private String getStatus(OlaToken olaToken, String olaCasaDaysValidity) {
        if(Objects.isNull(olaCasaDaysValidity) || olaCasaDaysValidity.isEmpty()) {
            return EMPTY_STRING;
        }

        if("Positive".equals(olaToken.getAmlScreeningResult()) || "High".equals(olaToken.getAssessmentRiskLevel())) {
            return "Fail";
        } else if(Arrays.asList("N","P","V").contains(olaToken.getStatus())
                && validateOlaCasaValidityExpiry(olaToken.getCreatedTime(), olaCasaDaysValidity)) {
            return "Expired_pending application";
        } else if(olaToken.getStatus().equals("I") && validateOlaCasaValidityExpiry(olaToken.getUpdatedTime(), olaCasaDaysValidity)) {
            return "Expired_pending activation";
        } else if(Arrays.asList("N","P","V").contains(olaToken.getStatus())) {
            return "Pending application";
        } else if(olaToken.getStatus().equals("I")) {
            return "Pending activation";
        } else if(olaToken.getStatus().equals("C")) {
            return "Successful";
        }
        return EMPTY_STRING;
    }

    private boolean validateOlaCasaValidityExpiry(String dateTime, String olaCasaDaysValidity) {
        LocalDateTime expiredDate = LocalDateTime.now().plusDays(Integer.parseInt(olaCasaDaysValidity));
        if(!Objects.isNull(dateTime)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE);
            expiredDate = LocalDateTime.parse(dateTime.split("\\.")[0], formatter).plusDays(Integer.parseInt(olaCasaDaysValidity));
        }
        return LocalDateTime.now().isAfter(expiredDate);
    }

    private String getDescription(OlaToken olaToken, List<AuditSummaryConfig> auditSummaryConfigPaths) {
        if(Objects.isNull(auditSummaryConfigPaths) || auditSummaryConfigPaths.isEmpty()) {
            logger.warn("No audit summary found.");
            return EMPTY_STRING;
        }

        if(Objects.isNull(olaToken.getAuditAdditionalData())) {
            logger.warn("Audit additional data is empty.");
            return EMPTY_STRING;
        }

        // Wrap into additionalData root.
        olaToken.setAuditAdditionalData(String.format("{ \"additionalData\": %s }", olaToken.getAuditAdditionalData()));


        DocumentContext documentContext = JsonPath.parse(olaToken.getAuditAdditionalData());
        if (documentContext == null) {
            logger.warn("Unable to parse Audit additional data.");
            return EMPTY_STRING;
        } else {
            List<String> fieldList = auditSummaryConfigPaths.stream().map(auditSummaryConfig -> {
                String returnValue = EMPTY_STRING;
                try {
                    String fieldName = auditSummaryConfig.getFieldName();
                    String fieldPath = auditSummaryConfig.getPath();
                    if (Objects.isNull(fieldName) || fieldName.isEmpty()) {
                        fieldName = StringUtils.capitalize(StringUtils.join(StringUtils
                                .splitByCharacterTypeCamelCase(fieldPath.substring(fieldPath.lastIndexOf('.') + 1)), ' '));
                    }
                    String fieldValue = documentContext.read(fieldPath).toString();
                    returnValue = String.format("%s : %s", fieldName, fieldValue);
                } catch (Exception e) {
                    logger.error("Error when parsing field value for additional data.");
                }
                return returnValue;
            }).collect(Collectors.toList());

            return String.join("\n", fieldList);
        }
    }

    private OlaCasaPagination getPagination(Page<OlaTokenGroup> customersPage, Integer pageNo) {
        logger.info("Get Page..");
        String pageIndicator = customersPage.getTotalPages() > pageNo ? "N" : PAGINATION_LAST;
        OlaCasaPagination olaCasaPage = new OlaCasaPagination();
        olaCasaPage.setPageNo(pageNo);
        olaCasaPage.setTotalPageNo(customersPage.getTotalPages());
        olaCasaPage.setRecordCount((int) customersPage.getTotalElements());
        olaCasaPage.setPageIndicator(pageIndicator);

        logger.debug(customersPage.getTotalElements());

        return olaCasaPage;
    }

    private AuditPagination getAuditPagination(Page<OlaToken> olaToken, Integer pageNo) {
        logger.info("Get Page..");
        String pageIndicator = olaToken.getTotalPages() > pageNo ? "N" : PAGINATION_LAST;
        AuditPagination auditPagination = new AuditPagination();
        auditPagination.setPageNum(pageNo);
        auditPagination.setTotalPageNum(olaToken.getTotalPages());
        auditPagination.setActivityCount((int) olaToken.getTotalElements());
        auditPagination.setPageIndicator(pageIndicator);

        logger.debug (olaToken.getTotalElements());

        return auditPagination;
    }

    /** IsPremier value can be null, if it's null convert it to false. */
    private String getPremierValue(UserProfile userProfile) {
        if (userProfile.getIsPremier() == null || userProfile.getIsPremier().equals("false")) {
            return Boolean.FALSE.toString();
        }
        return Boolean.TRUE.toString();
    }

    private String getStatusSummaryDesc(String apiStatusCode) {
        if("10000".equals(apiStatusCode)) {
            return "Success";
        }
        return "Failed";
    }
}
