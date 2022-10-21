package com.rhbgroup.dcpbo.user.function.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude
public class ModuleVo implements BoData {
	private int moduleId;
	private String moduleName;
	private List<FunctionVo> function;
}
