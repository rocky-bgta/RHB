package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExtractSmsOtpNotification {
	private String ibgBatchFail;
	private String firstTimeSignIn;
    private String welcomeNewUsers;
    private String changeTransactionLimit;
    private String changeEmail;
    private String removeDeviceAccess;
    private String changeQuickloginPin;
    private String changeOtpMobile;
    private String makePrimaryDevice;
    private String securePlusRegistration;
    private String securePlusDeregistration;
    private String securePlusOverride;
    private String txnThreshold;
}
