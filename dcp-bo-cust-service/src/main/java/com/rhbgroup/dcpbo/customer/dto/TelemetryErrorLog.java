package com.rhbgroup.dcpbo.customer.dto;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TelemetryErrorLog implements BoData {
	private String messageId;
	private String operationName;
	private String auditDateTime;
	private String errorCode;
	private String errorReason;
	private String errorDetails;
	private String errorSource;
}
