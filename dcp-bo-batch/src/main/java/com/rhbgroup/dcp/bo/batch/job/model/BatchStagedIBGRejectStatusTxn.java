package com.rhbgroup.dcp.bo.batch.job.model;


import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
public class BatchStagedIBGRejectStatusTxn extends BaseModel{
	String date;
	String teller;
	String trace;
	String ref1;
	String name;
	String amount;
	String rejectCode;
	String accountNo;
	String beneName;
	String beneAccount;
	String fileName;
}
