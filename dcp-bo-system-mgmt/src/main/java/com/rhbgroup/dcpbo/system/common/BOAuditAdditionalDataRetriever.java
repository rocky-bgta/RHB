package com.rhbgroup.dcpbo.system.common;

import java.util.Map;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.audit.AuditAdditionalDataRetriever;

public class BOAuditAdditionalDataRetriever implements AuditAdditionalDataRetriever {

	private AdditionalDataHolder additionalDataHolder;

	public BOAuditAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
		this.additionalDataHolder = additionalDataHolder;
	}

	@Override
	public Map<String, Object> retrieve() {
		return this.additionalDataHolder.getMap();
	}
}