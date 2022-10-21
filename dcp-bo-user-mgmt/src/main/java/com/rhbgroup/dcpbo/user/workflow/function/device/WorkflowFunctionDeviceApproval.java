package com.rhbgroup.dcpbo.user.workflow.function.device;

import com.rhbgroup.dcpbo.user.common.BoData;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class WorkflowFunctionDeviceApproval implements BoData {
	private Integer deviceId;
	private String deviceName;
	private String primaryDevice;
	private String os;
	private String lastSigned;
	private String registered;
	private int approvalId;
	private String actionType;
	private String createdTime;
	private String reason;
	private CreatedBy createdBy;
	private String updatedBy;
	private String updatedTime;
	private String isCreator;
    private String idNo;
    private String username;
    private String approvalStatus;
	
	public void setCreatedByName(String name) {
		if (createdBy == null)
			createdBy = new CreatedBy();
		createdBy.name = name;
	}

	@Setter
	@Getter
	@ToString
	class CreatedBy {
		private String name;
	}
}
