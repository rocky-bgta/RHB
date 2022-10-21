package com.rhbgroup.dcpbo.user.workflow.function.device;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ApprovalDevicePayload {
	private Integer boRefNo;
	private String moduleName;
	private String customerId;
	private String customerIdType;
	private String customerIdNo;
	private String deviceName;
	private String deviceOs;
	private Integer deviceId;
	private String createdTime;
	private String lastLogin;
	private String primaryDevice;
	private String username;
}
