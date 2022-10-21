package com.rhbgroup.dcpbo.user.create;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude
public class UserUpdateRequestVo {

	private Integer functionId;
	private UserPayloadUpdateVo cache;
	private UserPayloadUpdateVo input;

}
