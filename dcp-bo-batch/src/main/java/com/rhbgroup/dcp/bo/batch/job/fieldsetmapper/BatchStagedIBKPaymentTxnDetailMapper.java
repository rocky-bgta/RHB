package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxn;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnDetail;

public class BatchStagedIBKPaymentTxnDetailMapper implements FieldSetMapper<BatchStagedIBKPaymentTxn> {
	
	@Override
    public BatchStagedIBKPaymentTxn mapFieldSet(FieldSet fieldSet) throws BindException {
    	BatchStagedIBKPaymentTxnDetail batchStagedIBKPaymentTxnDetail = new BatchStagedIBKPaymentTxnDetail();
    	
    	batchStagedIBKPaymentTxnDetail.setBillerRefNo1(fieldSet.readString("billerRefNo1"));
    	batchStagedIBKPaymentTxnDetail.setBillerRefNo2(fieldSet.readString("billerRefNo2"));
    	batchStagedIBKPaymentTxnDetail.setBillerRefNo3(fieldSet.readString("billerRefNo3"));
    	batchStagedIBKPaymentTxnDetail.setFilter(fieldSet.readString("filter"));
    	batchStagedIBKPaymentTxnDetail.setRecordType(fieldSet.readString("recordType"));
    	batchStagedIBKPaymentTxnDetail.setTxnAmount(fieldSet.readString("txnAmount"));
    	batchStagedIBKPaymentTxnDetail.setTxnDate(fieldSet.readString("txnDate"));
    	batchStagedIBKPaymentTxnDetail.setTxnDescription(fieldSet.readString("txnDescription"));
    	batchStagedIBKPaymentTxnDetail.setTxnId(fieldSet.readString("txnId"));
    	batchStagedIBKPaymentTxnDetail.setTxnTime(fieldSet.readString("txnTime"));
    	batchStagedIBKPaymentTxnDetail.setTxnType(fieldSet.readString("txnType"));
    	
    	return batchStagedIBKPaymentTxnDetail;
    }
}
