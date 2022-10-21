package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadTermDepositHeader extends LoadTermDeposit{
	private String fileBatchDate;
	private String fileSystemDate;
	private String fileSystemTime;
	private String fileBatchJobName;
	private String fileBatchJobNumber;
	private String fileBatchProcStep;
	private String fileBatchProgramId;
	private String fileBatchUserId;
	
}
