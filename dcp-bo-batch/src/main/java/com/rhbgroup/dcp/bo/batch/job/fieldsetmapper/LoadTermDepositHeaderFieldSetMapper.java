package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDeposit;
import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDepositHeader;

public class LoadTermDepositHeaderFieldSetMapper implements FieldSetMapper<LoadTermDeposit> {

	@Override
	public LoadTermDeposit mapFieldSet(FieldSet fieldSet) throws BindException {
		LoadTermDepositHeader header = new LoadTermDepositHeader();
		header.setRecordType(fieldSet.readString("recordType"));
		header.setFileBatchDate(fieldSet.readString("fileBatchDate"));
		header.setFileSystemDate(fieldSet.readString("fileSystemDate"));
		header.setFileSystemTime(fieldSet.readString("fileSystemTime"));
		header.setFileBatchJobName(fieldSet.readString("fileBatchJobName"));
		header.setFileBatchJobNumber(fieldSet.readString("fileBatchJobNumber"));
		header.setFileBatchProcStep(fieldSet.readString("fileBatchProcStep"));
		header.setFileBatchProgramId(fieldSet.readString("fileBatchProgramId"));
		header.setFileBatchUserId(fieldSet.readString("fileBatchUserId"));
		header.setFilter(fieldSet.readString("filter"));
		header.setEndIndicator(fieldSet.readString("endIndicator"));
		return header;
	}

}
