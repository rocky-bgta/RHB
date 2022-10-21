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
public class UserUpdateInputVo {

	private String email;
	private String name;
	private Integer departmentId;
	private String departmentName;
	private List<UserFunctionUserGroupVo> group;
	private String status;

}
