package com.rhbgroup.dcpbo.customer.audit.collector;

import java.util.Map;

public class PutUnlockFacilityAdditionalDataRetriever implements AuditAdditionalDataRetriever {
	
	private AdditionalDataHolder additionalDataHolder;

    public PutUnlockFacilityAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
        this.additionalDataHolder = additionalDataHolder;
    }

    @Override
    public Map<String, String> retrieve() {
        return this.additionalDataHolder.getMap();
    }

}
