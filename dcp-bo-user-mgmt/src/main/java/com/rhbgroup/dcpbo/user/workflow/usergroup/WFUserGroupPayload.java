package com.rhbgroup.dcpbo.user.workflow.usergroup;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WFUserGroupPayload implements BoData {
	private Integer groupId;
	private String groupName;
	private List<WFUserGroupFunctionPayload> function;
	private String accessType;
	private List<Integer> functionId;
}
