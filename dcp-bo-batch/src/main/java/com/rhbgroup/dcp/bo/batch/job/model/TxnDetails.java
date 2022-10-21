package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TxnDetails {
	private String txnId;
	private String txnAmount;
	private String filler;
}
