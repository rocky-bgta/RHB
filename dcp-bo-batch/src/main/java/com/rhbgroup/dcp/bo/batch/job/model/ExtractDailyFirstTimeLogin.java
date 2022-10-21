package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExtractDailyFirstTimeLogin {
	private String date;
	private String time;
    private String cisNo;
    private String name;
    private String nricNo;
    private String idType;
    private String staff;
    private String channel;
    private String loginType;
    private String status;
    private String email;
    private String authorisedDeviceName;
    private String dateRegisteredDevice;
    private String lastLoginDate;
    private String lastLoginTime;
}
