package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxn;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnTrailer;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class BatchStagedDynamicIBKPaymentTxnTrailerMapper implements FieldSetMapper<BatchStagedIBKPaymentTxn> {

	@Override
    public BatchStagedIBKPaymentTxn mapFieldSet(FieldSet fieldSet) throws BindException {
		BatchStagedIBKPaymentTxnTrailer batchStagedIBKPaymentTxnTrailer = new BatchStagedIBKPaymentTxnTrailer();

    	batchStagedIBKPaymentTxnTrailer.setRecordType(fieldSet.readString("recordType"));
    	batchStagedIBKPaymentTxnTrailer.setHashTotal(fieldSet.readString("hashTotal"));

    	return batchStagedIBKPaymentTxnTrailer;
    }
}
