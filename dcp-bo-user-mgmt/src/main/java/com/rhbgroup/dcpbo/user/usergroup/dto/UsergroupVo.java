package com.rhbgroup.dcpbo.user.usergroup.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude
public class UsergroupVo implements BoData {
	private int groupId;
	private String groupName;
}
