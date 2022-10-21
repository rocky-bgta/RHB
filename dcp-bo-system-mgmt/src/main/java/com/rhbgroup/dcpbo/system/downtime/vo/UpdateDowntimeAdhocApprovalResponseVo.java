package com.rhbgroup.dcpbo.system.downtime.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class UpdateDowntimeAdhocApprovalResponseVo implements BoData {
	private int approvalId;
}
