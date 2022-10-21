package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedMergeCISDetailTxn;
public class MergeCISFieldSetMapper implements FieldSetMapper<BatchStagedMergeCISDetailTxn>{
	@Override
	public BatchStagedMergeCISDetailTxn mapFieldSet(FieldSet fieldSet) throws BindException {
		BatchStagedMergeCISDetailTxn mergeCISTxn = new BatchStagedMergeCISDetailTxn();
		mergeCISTxn.setCisNo(fieldSet.readString("cisNo"));
		mergeCISTxn.setNewCISNo(fieldSet.readString("newCISNo"));
		mergeCISTxn.setProcessingDate(fieldSet.readString("processingDate"));
		return mergeCISTxn;
	}
}
