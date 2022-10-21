package com.rhbgroup.dcpbo.user.create;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPayloadDeleteVo {

	private Integer userId;
	private String username;
	private String name;
	private String status;
	private String email;

}
