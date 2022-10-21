package com.rhbgroup.dcpbo.user.create;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateUMPayloadVo {

	private Integer userId;
	private String username;
	private UserFunctionDepartmentVo department;
	private String email;
	private String name;
	private List<UserFunctionUserGroupVo> group;
	private String status;
}
