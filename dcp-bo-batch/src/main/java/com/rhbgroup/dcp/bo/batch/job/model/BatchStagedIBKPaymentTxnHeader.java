package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@ToString
public class BatchStagedIBKPaymentTxnHeader extends BatchStagedIBKPaymentTxn {
	private String batchNumber;
	private String processDate;
	private String transactionDate;
    private String billerAccountNo;
    private String billerAccountName;
}