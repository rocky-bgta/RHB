package com.rhbgroup.dcpbo.system.downtime.vo;

import javax.validation.constraints.NotNull;

import com.rhbgroup.dcpbo.system.common.BoData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddDowntimeAdhocVo implements BoData {
	
	@NotNull
	private int functionId;
	
	@NotNull
	private String name;
	
	@NotNull
	private String adhocType;
	
	@NotNull
	private String startTime;
	
	@NotNull
	private String endTime;
	
	@NotNull
	private boolean isPushNotification;
	
	private String pushDate;

}
