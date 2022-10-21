package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxn;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnHeader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BatchStagedDynamicIBKPaymentTxnHeaderMapper implements FieldSetMapper<BatchStagedIBKPaymentTxn> {

	@Override
    public BatchStagedIBKPaymentTxn mapFieldSet(FieldSet fieldSet) throws BindException {
		Supplier<Stream<String>> names= () -> Arrays.stream(fieldSet.getNames());

		String processDate;
		processDate = getProcessingDate(fieldSet, names);

		BatchStagedIBKPaymentTxnHeader batchStagedIBKPaymentTxnHeader = new BatchStagedIBKPaymentTxnHeader();
		batchStagedIBKPaymentTxnHeader.setProcessDate(processDate);

		batchStagedIBKPaymentTxnHeader.setBillerAccountName(names.get().anyMatch("billerAccountName"::equals) ?fieldSet.readString("billerAccountName"):"");
		batchStagedIBKPaymentTxnHeader.setBillerAccountNo(names.get().anyMatch("billerAccountNumber"::equals) ?fieldSet.readString("billerAccountNumber"):"");

    	batchStagedIBKPaymentTxnHeader.setRecordType(fieldSet.readString("recordType"));
    	
    	return batchStagedIBKPaymentTxnHeader;
    }

	/**
	 * This function is to get process date
	 * @param fieldSet
	 * @param names
	 * @return String
	 */
	private String getProcessingDate(FieldSet fieldSet, Supplier<Stream<String>> names) {
		if (names.get().anyMatch("processDate"::equals)) {
			return fieldSet.readString("processDate");
		}
		if (names.get().anyMatch("transDate"::equals)) {
			return fieldSet.readString("transDate");
		}
		if (names.get().anyMatch("date"::equals))
			return fieldSet.readString("date");
		else
			return names.get().anyMatch("headerXnDate"::equals) ? fieldSet.readString("headerXnDate") : "";
	}
}
