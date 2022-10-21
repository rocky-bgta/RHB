package com.rhbgroup.dcpbo.user.workflow.usergroup;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WFUserGroupFunctionPayload implements BoData {
	private Integer functionId;
	private String functionName;
}
