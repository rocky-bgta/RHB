package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKJompayEmatching;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKJompayEmatchingHeader;

public class BatchStagedIBKJompayEmatchingHeaderFieldSetMapper implements FieldSetMapper<BatchStagedIBKJompayEmatching> {

	@Override
	public BatchStagedIBKJompayEmatching mapFieldSet(FieldSet fieldSet) throws BindException {
		BatchStagedIBKJompayEmatchingHeader header = new BatchStagedIBKJompayEmatchingHeader();
		header.setRecordType(fieldSet.readString("recordType"));
		header.setProgramName(fieldSet.readString("programName")); 
		header.setProcessingDate(fieldSet.readString("processingDate")); 
		header.setSystemDate(fieldSet.readString("systemDate")); 
		header.setSystemTime(fieldSet.readString("systemTime")); 
		header.setEmatching(fieldSet.readString("ematching")); 
		return  header;
	}

}
