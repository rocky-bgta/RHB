package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.Audit;
//import com.rhbgroup.dcpbo.customer.model.DcpAuditCategoryConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class BoRepositoryHelper {

    public List<Integer> constructAuditConfigCategories(String eventCategoryIdDelimited) {
        String[] eventCategoryIdsStr = eventCategoryIdDelimited.split(",");
        List<Integer> auditCategories = new ArrayList<>();
        for(int i = 0; i < eventCategoryIdsStr.length; i++) {
            auditCategories.add(Integer.valueOf(eventCategoryIdsStr[i]));
        }
        return auditCategories;
    }

    public List<String> constructEventCodesFromAudits(List<Audit> dcpAudits) {
        List<String> eventCodes = new ArrayList<>();
        for(Audit dcpAudit:dcpAudits) {
            eventCodes.add(dcpAudit.getEventCode());
        }
        return eventCodes;
    }

    public List<String> constructEventCodeList(String eventCodeDelimited) {
        String[] eventCategoryIdsStr = eventCodeDelimited.split(",");
        List<String> auditCategories = new ArrayList<>();
        for(int i = 0; i < eventCategoryIdsStr.length; i++) {
            auditCategories.add(eventCategoryIdsStr[i]);
        }
        return auditCategories;
    }
    
    public List<String> constructEventCodesFromAuditList(List<Audit> dcpAudits) {
    	Set<String> codes = new HashSet<>();
        for(Audit dcpAudit:dcpAudits) {
            codes.add(dcpAudit.getEventCode());
        }
        
        List<String> eventCodes = new ArrayList<>();
        for(String code : codes) {
        	eventCodes.add(code);
        }
        
        return eventCodes;
    }
}
