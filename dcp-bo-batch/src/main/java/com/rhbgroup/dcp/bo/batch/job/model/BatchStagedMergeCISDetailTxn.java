package com.rhbgroup.dcp.bo.batch.job.model;
import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BatchStagedMergeCISDetailTxn extends BaseModel {
	private String cisNo;
	private String newCISNo;
	private String processingDate;
	private String fileName;
}
