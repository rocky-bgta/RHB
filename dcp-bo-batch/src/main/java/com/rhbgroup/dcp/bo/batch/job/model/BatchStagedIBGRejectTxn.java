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
public class BatchStagedIBGRejectTxn {

	private long id;
	private long jobExecutionId;
	private String date;
	private String teller;
	private String trace;
	private String ref1;
	private String name;
	private String amount;
	private String rejectCode;
	private String rejectDescription;
	private String accountNo;
	private Integer userId;
	private String beneName;
	private String beneAccount;
	private boolean isProcessed;
	private boolean isNotificationSent;	
	private Date createdTime;
	private Date updatedTime;
	private String fileName;
}
