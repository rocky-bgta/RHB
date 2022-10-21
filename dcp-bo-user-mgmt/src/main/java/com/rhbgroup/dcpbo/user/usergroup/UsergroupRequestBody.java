package com.rhbgroup.dcpbo.user.usergroup;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@ToString
@JsonInclude
public class UsergroupRequestBody implements BoData {
	private String functionId;
	private String groupName;
	private List<UsergroupFunctionRequestBody> function;
	private String accessType;
}
