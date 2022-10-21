package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class BatchStagedJompayFailureTxn {
	private String billerCode;
	private String paymentChannel;
	private String requestTimeStr;
	private Date requestTime;
	private String reasonForFailure;
	private String fileName;
	private Date createdTime;
}