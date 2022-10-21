package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class AsnbBatch {

	private String headerInd;
	private String agetCode;
	private String tranDate;
	private String totalRecords;
	private String totalAmount;
	private String endOfRecord;
	private List<AsnbBatchDetails> asnbBtachDetails;
	private String trlInd;
	private String endOfFile;
	
}
