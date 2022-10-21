package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadCardlinkNotificationTrailer  extends LoadCardlinkNotification{
	private String recordCount;
	private String hashValue;
}
