package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedJompayFailureTxn;

public class BatchStagedJompayFailureTxnMapper implements FieldSetMapper<BatchStagedJompayFailureTxn> {

	@Override
    public BatchStagedJompayFailureTxn mapFieldSet(FieldSet fieldSet) throws BindException {
		BatchStagedJompayFailureTxn batchStagedJompayFailureTxn = new BatchStagedJompayFailureTxn();
    	
		batchStagedJompayFailureTxn.setBillerCode(fieldSet.readString("billerCode"));
		batchStagedJompayFailureTxn.setPaymentChannel(fieldSet.readString("paymentChannel"));
		batchStagedJompayFailureTxn.setRequestTimeStr(fieldSet.readString("requestTime"));
		batchStagedJompayFailureTxn.setReasonForFailure(fieldSet.readString("reasonForFailure"));
		
    	return batchStagedJompayFailureTxn;
    }
}
