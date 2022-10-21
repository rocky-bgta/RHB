package com.rhbgroup.dcpbo.user.info;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class UserInfo implements BoData {
	private String name;
	private String email;
}
