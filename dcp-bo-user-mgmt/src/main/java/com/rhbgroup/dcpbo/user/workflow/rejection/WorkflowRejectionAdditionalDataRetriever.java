package com.rhbgroup.dcpbo.user.workflow.rejection;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.audit.AuditAdditionalDataRetriever;

import java.util.Map;

public class WorkflowRejectionAdditionalDataRetriever implements AuditAdditionalDataRetriever {

    private AdditionalDataHolder additionalDataHolder;

    public WorkflowRejectionAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
        this.additionalDataHolder = additionalDataHolder;
    }

    @Override
    public Map<String, Object> retrieve() {
        return this.additionalDataHolder.getMap();
    }
}
