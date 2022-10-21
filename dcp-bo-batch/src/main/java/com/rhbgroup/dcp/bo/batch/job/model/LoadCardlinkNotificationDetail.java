package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadCardlinkNotificationDetail extends LoadCardlinkNotification {
	private String runningNumber;
	private String creditCard;
	private String paymentDueDate;
	private String creditCardType;
	private String minAmount;
	private String outstandingAmount;
	private String statementAmount;
	private String statementDate;
}
