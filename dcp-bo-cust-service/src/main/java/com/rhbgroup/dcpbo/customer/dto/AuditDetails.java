package com.rhbgroup.dcpbo.customer.dto;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class AuditDetails {
	Integer auditId;
	String eventCode;
	String eventName;
	String timestamp;
	String deviceId;
	String channel;
	String statusCode;
	String statusDescription;
	//DCPBL-13896
	String statusSummary;
	String ip;
	String username;
	String cisNo;
	String refId;

	List<Details> details;
	
	@Setter
	@Getter
	@ToString
	static class Details {
		String fieldName;
		String value;
		
		public Details(String fieldName, String value) {
			this.fieldName = fieldName;
			this.value = value;
		}
	}
	
	public void addDetails(String fieldName, String value) {
		if (details == null)
			details = new LinkedList<Details>();
		
		details.add(new Details(fieldName, value));
	}
}
