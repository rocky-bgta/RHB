package com.rhbgroup.dcpbo.user.workflow.user;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WFDepartmentPayload {

	private Integer departmentId;
	private String departmentName;

}
