package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceProfile{
	
    private Integer id;
    private Integer userId;
    private String deviceId;
    private String deviceName;
    private String os;
    private String quickLoginRefreshToken;
    private String pushNotificationSubscriptionToken;
    private String pushNotificationPlatform;
    private Date lastLogin;
    private Date createdTime;
    private Boolean isQuickLoginBioEnabled;
    private Boolean securePlusSetup;    
    private Integer securePlusSequenceNo;    
    private String subscriberId;
    private String rsaChallengeStatus;
    private String sessionTokenExpiry;
    private String deviceStatus;
    private String quickLoginAccessToken;
    private Date updatedTime;
    private String updatedBy;
    private String deviceType;
}