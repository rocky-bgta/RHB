package com.rhbgroup.dcp.bo.batch.job.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CardlinkNotification implements Serializable {
	private String fileName;
	private String eventCode;
	private String keyType;
	private String systemDate;
	private String systemTime;
	private String cardNumber;
	private String paymentDueDate;
	private String cardType;
	private String minimumAmount;
	private String outstandingAmount;
	private String statementAmount;
	private String statementDate;
	private Long notificationRawId;
	private Long userId;
}
