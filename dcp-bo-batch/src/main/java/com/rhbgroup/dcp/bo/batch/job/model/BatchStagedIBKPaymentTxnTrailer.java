package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode(callSuper=false)
@ToString
public class BatchStagedIBKPaymentTxnTrailer extends BatchStagedIBKPaymentTxn {
    private String processingFlag;
    private String batchTotal;
    private String batchAmount;
    private String hashTotal;
}