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
public class BatchUnitTrustJobStatusControl  {
	long id;
	long jobExecutionId;
	Date batchProcessDate;
	Date batchEndDatetime;
	int targetDataset;
	int status;
	int tblUtCustomerStatus;
	int tblUtCustomerRelStatus;
	int tblUtAccountStatus;
	int tblUtAccountHoldingStatus;
	int tblUtFundMasterStatus;
	String createdBy;
	Date createdTime;
	String updatedBy;
	Date updatedTime;
}

