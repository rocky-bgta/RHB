package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "DCP_TELEMETRY_ERROR_LOG")
public class DcpTelemetryErrorLog implements Serializable {
	@Column(name = "MESSAGE_ID")
	private String messageId;
	
	@Column(name = "OPERATION_NAME")
	private String operationName;
	
	@Id
	@Column(name = "AUDIT_DATE_TIME", nullable = false)
	private Timestamp auditDateTime;
	
	@Column(name = "ERROR_CODE")
	private String errorCode;
	
	@Column(name = "ERROR_REASON")
	private String errorReason;
	
	@Column(name = "ERROR_DETAILS")
	private String errorDetails;
	
	@Column(name = "ERROR_SOURCE")
	private String errorSource;
	
	public String getAuditDateTimeString() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    	String strDate = dateFormat.format(getAuditDateTime());
    	return strDate;
	}
}
