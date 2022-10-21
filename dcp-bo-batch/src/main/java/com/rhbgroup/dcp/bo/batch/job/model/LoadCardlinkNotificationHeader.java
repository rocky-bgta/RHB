package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadCardlinkNotificationHeader extends LoadCardlinkNotification{
	private String processingDate;
	private String systemDate;
	private String systemTime;
	private String eventCode;
	private String keyType;
}
