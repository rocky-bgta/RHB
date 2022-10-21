package com.rhbgroup.dcpbo.customer.service.impl;

import com.jayway.jsonpath.JsonPath;
import com.rhbgroup.dcpbo.customer.dcpbo.AuditDetailsVW;
import com.rhbgroup.dcpbo.customer.dto.InvestigationEvent;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.AuditSummaryConfig;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.InvestigationAuditListCSVService;
import com.rhbgroup.dcpbo.customer.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvestigationAuditListCsvServiceImpl implements InvestigationAuditListCSVService {

    @Autowired
    private AuditDetailsVWRepository auditDetailsVWRepository;
    @Autowired
    private LookupStatusRepository lookupStatusRepository;
    @Autowired
    private AuditSummaryConfigRepository auditSummaryConfigRepository;
    @Autowired
    private DcpAuditEventConfigRepository dcpAuditEventConfigRepository;
    @Autowired
    private BoRepositoryHelper boRepositoryHelper;
    @Autowired
    private AuditEventsService auditEventsService;

    private final String STRING_ALL = "all";
    private static final String STRING_NOT_APPLICABLE = "N/A";
    private final String STATUS_FAIL = "F";
    private final String STATUS_SUCCESS = "S";
    public static final Integer RECORDS_MAX = 10000;
    public static final String PATH_REF_ID = "$.request.refId";

    private final Logger log = LogManager.getLogger(InvestigationAuditListCsvServiceImpl.class);

    @Override
    public void listing(HttpServletResponse response, String eventCodes, String frDateStr, String toDateStr, String status) {

        HashMap formattedDate = auditEventsService.parseInputDate(frDateStr, toDateStr);
        Timestamp frDate = (Timestamp) formattedDate.get("frDate");
        Timestamp toDate = (Timestamp) formattedDate.get("toDate");

        List<AuditDetailsVW> auditDetailsVWList;
        List<InvestigationEvent> investigationEventList = new ArrayList<>();
        try {
            List<Integer> codeList;
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
                auditDetailsVWList = auditDetailsVWRepository.getCustomerAuditDetailsByAuditIds(RECORDS_MAX, eventCodeList, codeList, frDate, toDate);
            } else {
                auditDetailsVWList = auditDetailsVWRepository.getCustomerAuditDetailsByAuditIds(RECORDS_MAX, codeList, frDate, toDate);
            }
            List<AuditSummaryConfig> auditSummaryConfigList = auditSummaryConfigRepository.findAll();
            if (auditDetailsVWList.size() != 0) {
                //Constructing event response
                for (AuditDetailsVW auditDetailsVW : auditDetailsVWList) {
                    InvestigationEvent investigationEvent = new InvestigationEvent();
                    String auditEventCode = auditDetailsVW.getEvent_code();
                    investigationEvent.setEventCode(auditEventCode);
                    investigationEvent.setChannel(auditDetailsVW.getChannel());
                    investigationEvent.setEventName(auditDetailsVW.getEvent_name());
                    investigationEvent.setAuditId(String.valueOf(auditDetailsVW.getId()));
                    investigationEvent.setUsername(String.valueOf(auditDetailsVW.getUsername()));
                    investigationEvent.setStatusDescription(String.valueOf(auditDetailsVW.getStatus_description()));
                    investigationEvent.setTimestamp(String.valueOf(auditDetailsVW.getTimestamp()));
                    String auditDetails = auditDetailsVW.getDetails();
                    if (StringUtils.isNotEmpty(auditDetails)) {
                        List<String> auditSummaryConfigPathList = auditSummaryConfigList.stream().filter(o -> o.getEventCode().equals(auditEventCode)).map(
                                o -> o.getPath()).collect(Collectors.toList());
                        Iterator<String> iter = auditSummaryConfigPathList.iterator();
                        StringBuilder sb = new StringBuilder();
                        while (iter.hasNext()) {
                            try {
                                String path = iter.next();
                                String fieldName = path.substring(path.lastIndexOf('.') + 1);
                                String fieldValue = JsonPath.parse(auditDetails).read(path, String.class);
                                if (StringUtils.isNotEmpty(fieldValue) && !"null".equals(fieldValue)) {
                                    if (StringUtils.isNotEmpty(fieldName)) {
                                        sb.append(StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(fieldName), ' ')));
                                        sb.append(" : ");
                                    }
                                    sb.append(fieldValue);
                                    if (iter.hasNext()) {
                                        sb.append("; ");
                                    }
                                }
                            } catch (Exception ex) {
                                log.warn("Exception caught while parsing details", ex);
                            }
                        }

                        investigationEvent.setSummaryDescription(sb.toString());
                        try {
                            if (JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_REF_ID) != null) {
                                String refId = JsonUtils.getJsonPathValueWithDefaultNull(auditDetails, PATH_REF_ID);
                                investigationEvent.setRefId("=\"" + refId + "\"");
                            }
                        } catch (Exception e) {
                            log.warn("Error parsing auditDetails::", e);
                        }
                    }
                    investigationEventList.add(investigationEvent);
                }
            }

            //Generate CSV
            try (PrintWriter responseWriter = response.getWriter();
                 ICsvBeanWriter beanWriter = new CsvBeanWriter(responseWriter, CsvPreference.STANDARD_PREFERENCE)) {
                Field[] fields = InvestigationEvent.class.getDeclaredFields();
                List<String[]> fieldNames = getHeaderAndMappingFields(fields);
                final CellProcessor[] processors = getProcessors(fieldNames.get(0).length);
                // write the header
                beanWriter.writeHeader(fieldNames.get(0));
                // write the beans
                for (final InvestigationEvent investigationEvent : investigationEventList) {
                    beanWriter.write(investigationEvent, fieldNames.get(1), processors);
                }
                beanWriter.flush();
                responseWriter.flush();
            } catch (IOException e) {
                log.error("Exception caught while writing to csv ", e);
            }
        } catch (Exception e) {
            log.error("Main Issue in Downloading audit log:::::", e);
        }
    }

    private static CellProcessor[] getProcessors(Integer listSize) {
        final CellProcessor[] processors = new CellProcessor[listSize];
        for (int i = 0; i < listSize; i++) {
            processors[i] = new ConvertNullTo(STRING_NOT_APPLICABLE);
        }
        return processors;
    }

    private static List<String[]> getHeaderAndMappingFields(Field[] fields) {
        List<String> headerFields = new ArrayList<>();
        List<String> mappingFieldNames = new ArrayList<>();
        List<String[]> arrayList = new ArrayList<>();
        for (Field field : fields) {
            mappingFieldNames.add(field.getName());
            StringBuilder sb = new StringBuilder();
            sb.append(StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(field.getName()), ' ')));
            headerFields.add(sb.toString());
        }
        String[] headerArray = new String[headerFields.size()];
        String[] mappingArray = new String[headerFields.size()];
        arrayList.add(headerFields.toArray(headerArray));
        arrayList.add(mappingFieldNames.toArray(mappingArray));
        return arrayList;
    }
}
