package com.rhbgroup.dcpbo.system.downtime.whitelist.vo;

import javax.validation.constraints.NotNull;

import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteDowntimeAdhocWhitelistRequest implements BoData {
	
	@NotNull
	private Integer functionId;
}
