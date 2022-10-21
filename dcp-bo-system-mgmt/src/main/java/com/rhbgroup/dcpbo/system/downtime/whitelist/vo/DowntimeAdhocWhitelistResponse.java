package com.rhbgroup.dcpbo.system.downtime.whitelist.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude
public class DowntimeAdhocWhitelistResponse implements BoData {
	private int approvalId = 0;
}
