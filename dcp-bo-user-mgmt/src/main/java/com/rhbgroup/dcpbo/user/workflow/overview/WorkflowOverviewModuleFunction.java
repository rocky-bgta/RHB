package com.rhbgroup.dcpbo.user.workflow.overview;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class WorkflowOverviewModuleFunction implements BoData {
	private Integer functionId;
	private String functionName;
	private String pendingCount;
	private String approvedCount;
	private String rejectedCount;
}
