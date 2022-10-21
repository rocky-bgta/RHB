package com.rhbgroup.dcpbo.user.function.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.common.BoData;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude
public class ModuleListVo implements BoData {
	private List<ModuleVo> module;
}
