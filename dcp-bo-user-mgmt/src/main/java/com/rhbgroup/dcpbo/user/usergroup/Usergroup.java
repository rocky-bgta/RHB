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
public class Usergroup implements BoData {
	private String approvalId;
	private String isWritten;
}
