package com.rhbgroup.dcpbo.user.workflow.rejection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@ToString
@JsonInclude
public class WorkflowRejection implements BoData {
	private String approvalId;
}
