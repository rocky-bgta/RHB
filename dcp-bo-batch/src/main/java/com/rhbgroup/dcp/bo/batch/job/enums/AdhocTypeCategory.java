package com.rhbgroup.dcp.bo.batch.job.enums;

public enum AdhocTypeCategory {

	SYSTEM(40000), INTERNAL(40001), EXTERNAL(40002);

	private int eventCode;

	AdhocTypeCategory(int evtCode) {
		this.eventCode = evtCode;
	}

	public int getEventCode() {
		return eventCode;
	}
}
