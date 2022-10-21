package com.rhbgroup.dcpbo.user.workflow.user;

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
public class WFUserPayload implements BoData {
	private Integer userId;
	private String username;
	private String email;
	private String name;
	private WFDepartmentPayload department;
	private List<WFUserGroupPayload> group;
	private String status;
}
