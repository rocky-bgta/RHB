package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.model.AuditEventFunctionCategoryVo;
import com.rhbgroup.dcpbo.customer.service.AuditEventsFunctionService;
import com.rhbgroup.dcpbo.customer.model.AuditEventsResponseVo;
import com.rhbgroup.dcpbo.customer.model.EventsDetails;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.AuditEventConfig;
import com.rhbgroup.dcpbo.customer.model.DcpAuditCategoryConfig;
import com.rhbgroup.dcpbo.customer.repository.DcpAuditCategoryConfigRepository;
import com.rhbgroup.dcpbo.customer.repository.DcpAuditEventConfigRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class AuditEventsFunctionServiceImpl implements AuditEventsFunctionService {

    @Autowired
    DcpAuditEventConfigRepository dcpAuditEventConfigRepository;

    @Autowired
    DcpAuditCategoryConfigRepository dcpAuditCategoryConfigRepository;

    private static Logger logger = LogManager.getLogger(AuditEventsFunctionServiceImpl.class);

    @Override
    public BoData getAuditEventslisting(String eventCode) {
        logger.debug("Inside getAuditEventslisting...");

        AuditEventsResponseVo auditEvents = new AuditEventsResponseVo();

        List<AuditEventFunctionCategoryVo> auditEventFunctionCategoryVoList = new ArrayList<>();

        if(!(StringUtils.isEmpty(eventCode))){
            EventsDetails eventDetailsEC = new EventsDetails();
            List<EventsDetails> eventsDetailsListEC = new ArrayList<>();
            AuditEventFunctionCategoryVo auditEventFunctionCategoryVoEC = new AuditEventFunctionCategoryVo();
            AuditEventConfig auditEventConfig = dcpAuditEventConfigRepository.findByEventCode(eventCode);

            if (auditEventConfig == null) {
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Null return value for Event Code", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if(auditEventConfig != null) {
                DcpAuditCategoryConfig auditCategoryConfig = dcpAuditCategoryConfigRepository.findOne(auditEventConfig.getEventCategoryId());

                auditEventFunctionCategoryVoEC.setCategoryId(auditEventConfig.getEventCategoryId());
                auditEventFunctionCategoryVoEC.setCategoryName(auditCategoryConfig.getCategoryName());

                eventDetailsEC.setEventId(auditEventConfig.getId());
                eventDetailsEC.setEventCode(auditEventConfig.getEventCode());
                eventDetailsEC.setEventName(auditEventConfig.getEventName());
                eventsDetailsListEC.add(eventDetailsEC);

                auditEventFunctionCategoryVoEC.setEvents(eventsDetailsListEC);
                auditEventFunctionCategoryVoList.add(auditEventFunctionCategoryVoEC);
                auditEvents.setCategory(auditEventFunctionCategoryVoList);
            }
        }

        else {
            //Retrieve full list of event categories
            List<DcpAuditCategoryConfig> eventCategoriesList = dcpAuditCategoryConfigRepository.getAll();

            if (eventCategoriesList.isEmpty()) {
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Null return value for dcpAuditCategoryConfigRepository.getAll()", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (!eventCategoriesList.isEmpty() && eventCategoriesList.size() > 0) {
                for (DcpAuditCategoryConfig dcpAuditCategoryConfig : eventCategoriesList) {
                    List<EventsDetails> eventsDetailsList = new ArrayList<>();

                    AuditEventFunctionCategoryVo auditEventFunctionCategoryVo = new AuditEventFunctionCategoryVo();

                    Integer categoryId = dcpAuditCategoryConfig.getId();
                    String categoryName = dcpAuditCategoryConfig.getCategoryName();
                    List<AuditEventConfig> auditEventConfigList = dcpAuditEventConfigRepository.findByCategoryId(dcpAuditCategoryConfig.getId());

                    if (!auditEventConfigList.isEmpty() && auditEventConfigList.size() > 0) {
                        for (AuditEventConfig auditEventConfig : auditEventConfigList) {
                            EventsDetails eventsDetails = new EventsDetails();

                            eventsDetails.setEventId(auditEventConfig.getId());
                            eventsDetails.setEventCode(auditEventConfig.getEventCode());
                            eventsDetails.setEventName(auditEventConfig.getEventName());
                            eventsDetailsList.add(eventsDetails);
                        }
                        auditEventFunctionCategoryVo.setCategoryId(categoryId);
                        auditEventFunctionCategoryVo.setCategoryName(categoryName);
                        auditEventFunctionCategoryVo.setEvents(eventsDetailsList);
                    }
                    auditEventFunctionCategoryVoList.add(auditEventFunctionCategoryVo);
                }
            }

            auditEvents.setCategory(auditEventFunctionCategoryVoList);
        }

        return auditEvents;
    }
}
