package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxn;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnTrailer;

public class BatchStagedIBKPaymentTxnTrailerMapper implements FieldSetMapper<BatchStagedIBKPaymentTxn> {

	@Override
    public BatchStagedIBKPaymentTxn mapFieldSet(FieldSet fieldSet) throws BindException {
		BatchStagedIBKPaymentTxnTrailer batchStagedIBKPaymentTxnTrailer = new BatchStagedIBKPaymentTxnTrailer();
		
    	batchStagedIBKPaymentTxnTrailer.setRecordType(fieldSet.readString("recordType"));
    	batchStagedIBKPaymentTxnTrailer.setProcessingFlag(fieldSet.readString("processingFlag"));
    	batchStagedIBKPaymentTxnTrailer.setBatchTotal(fieldSet.readString("batchTotal"));
    	batchStagedIBKPaymentTxnTrailer.setBatchAmount(fieldSet.readString("batchAmount"));
    	batchStagedIBKPaymentTxnTrailer.setHashTotal(fieldSet.readString("hashTotal"));
    	batchStagedIBKPaymentTxnTrailer.setFilter(fieldSet.readString("filter"));
    	
    	return batchStagedIBKPaymentTxnTrailer;
    }
}
