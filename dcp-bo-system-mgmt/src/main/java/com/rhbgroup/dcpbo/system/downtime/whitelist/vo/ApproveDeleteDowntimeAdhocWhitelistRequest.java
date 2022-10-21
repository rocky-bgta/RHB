package com.rhbgroup.dcpbo.system.downtime.whitelist.vo;

import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveDeleteDowntimeAdhocWhitelistRequest implements BoData {
	
	private String reason;

}
