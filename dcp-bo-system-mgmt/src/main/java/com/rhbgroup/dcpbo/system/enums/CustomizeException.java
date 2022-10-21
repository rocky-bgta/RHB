package com.rhbgroup.dcpbo.system.enums;

public enum CustomizeException {
	ERROR_CODE_NOT_EXIST("181", "Error Code Not Exist.");

	private final String value;
	private final String reasonPhrase;

	CustomizeException(String value, String reasonPhrase) {
		this.value = value;
		this.reasonPhrase = reasonPhrase;
	}

	public String value() {
		return this.value;
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}
}
