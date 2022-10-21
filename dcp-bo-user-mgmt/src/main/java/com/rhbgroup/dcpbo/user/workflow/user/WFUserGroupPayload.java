package com.rhbgroup.dcpbo.user.workflow.user;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WFUserGroupPayload {

	private Integer groupId;
	private String groupName;

}
