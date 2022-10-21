package com.rhbgroup.dcp.bo.batch.framework.enums;

public enum UserStatus {
	ACTIVE("A"),
	INACTIVE("I"),
	DELETED("D"),
	LOCKED("L");
	
	private String status;
	
	public String getStatus() {
		return status;
	}

	UserStatus(String status) {
		this.status = status;
	}
}
