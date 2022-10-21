package com.rhbgroup.dcpbo.user.create;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPayloadVo {

	private String username;
	private UserFunctionDepartmentVo department;
	private String email;
	private String name;
	private List<UserFunctionUserGroupVo> group;
	private String status;

}
