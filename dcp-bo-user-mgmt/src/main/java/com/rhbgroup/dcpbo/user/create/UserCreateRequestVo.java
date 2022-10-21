package com.rhbgroup.dcpbo.user.create;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude
public class UserCreateRequestVo {

	private Integer functionId;
	private String username;
	private String email;
	private String name;
	private UserFunctionDepartmentVo department;
	private List<UserFunctionUserGroupVo> usergroup;
	private String errorCode;
	private String errorDesc;
	private Integer approvalId;
	private String isWritten;

}
