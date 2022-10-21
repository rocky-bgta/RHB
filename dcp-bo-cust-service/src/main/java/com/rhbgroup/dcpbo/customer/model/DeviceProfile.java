package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table( name = "TBL_DEVICE_PROFILE")
public class DeviceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "USER_ID", nullable = false)
    private Integer userId;

    @Column(name = "DEVICE_ID", nullable = false)
    private String deviceId;

    @Column(name = "DEVICE_NAME", nullable = false)
    private String deviceName;

    @Column(name = "OS", nullable = false)
    private String os;

    @Column(name = "QUICK_LOGIN_REFRESH_TOKEN")
    private String quickLoginRefreshToken;

    @Column(name = "PUSH_NOTIFICATION_SUBSCRIPTION_TOKEN")
    private String pushNotificationSubscriptionToken;

    @Column(name = "PUSH_NOTIFICATION_PLATFORM")
    private String pushNotificationPlatform;

    @Column(name = "LAST_LOGIN", nullable = false)
    private Date lastLogin;

    @Column(name = "CREATED_TIME", nullable = false)
    private Date createdTime;

    @Column(name = "IS_QUICK_LOGIN_BIO_ENABLED", nullable = false)
    private Boolean isQuickLoginBioEnabled;

    @Column(name = "SUBSCRIBER_ID")
    private String subscriberId;

    @Column(name = "SECURE_PLUS_SEQUENCE_NO")
    private Integer securePlusSequenceNo;

    @Column(name = "SECURE_PLUS_SETUP", nullable = false)
    private Boolean securePlusSetup;

    @Column(name = "RSA_CHALLENGE_STATUS")
    private String rsaChallengeStatus;

    @Column(name = "SESSION_TOKEN_EXPIRY")
    private String sessionTokenExpiry;

    @Column(name = "DEVICE_STATUS")
    private String deviceStatus;

}
