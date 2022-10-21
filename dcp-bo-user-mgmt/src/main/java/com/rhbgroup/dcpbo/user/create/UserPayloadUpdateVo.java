package com.rhbgroup.dcpbo.user.create;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPayloadUpdateVo {

	private String username;
	private Integer departmentId;
	private String departmentName;
	private String email;
	private String name;
	private List<UserFunctionUserGroupVo> group;
	private String status;
	
}
