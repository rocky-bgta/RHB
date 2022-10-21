package com.rhbgroup.dcpbo.user.usergroup;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class UsergroupFunctionRequestBody implements BoData {
	private String functionId;
	private String functionName;
}
