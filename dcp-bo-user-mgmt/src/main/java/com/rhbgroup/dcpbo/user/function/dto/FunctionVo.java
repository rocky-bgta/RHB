package com.rhbgroup.dcpbo.user.function.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude
public class FunctionVo implements BoData {
	private int functionId;
	private String functionName;
}
