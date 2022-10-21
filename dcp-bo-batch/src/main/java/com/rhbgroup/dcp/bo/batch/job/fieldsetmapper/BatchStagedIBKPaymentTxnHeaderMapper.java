package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxn;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnHeader;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BatchStagedIBKPaymentTxnHeaderMapper implements FieldSetMapper<BatchStagedIBKPaymentTxn> {

	@Override
    public BatchStagedIBKPaymentTxn mapFieldSet(FieldSet fieldSet) throws BindException {

		BatchStagedIBKPaymentTxnHeader batchStagedIBKPaymentTxnHeader = new BatchStagedIBKPaymentTxnHeader();
    	
    	batchStagedIBKPaymentTxnHeader.setBatchNumber(fieldSet.readString("batchNumber"));
    	batchStagedIBKPaymentTxnHeader.setBillerAccountName(fieldSet.readString("billerAccountName"));
    	batchStagedIBKPaymentTxnHeader.setBillerAccountNo(fieldSet.readString("billerAccountNo"));
    	batchStagedIBKPaymentTxnHeader.setFilter(fieldSet.readString("filter"));
    	batchStagedIBKPaymentTxnHeader.setProcessDate(fieldSet.readString("processDate"));
    	batchStagedIBKPaymentTxnHeader.setRecordType(fieldSet.readString("recordType"));
    	
    	return batchStagedIBKPaymentTxnHeader;
    }
}
