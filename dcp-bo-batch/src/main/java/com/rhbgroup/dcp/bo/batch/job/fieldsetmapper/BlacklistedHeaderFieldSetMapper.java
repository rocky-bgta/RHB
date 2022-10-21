package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.BlacklistedMsic;
import com.rhbgroup.dcp.bo.batch.job.model.BlacklistedMsicHeader;
import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDeposit;
import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDepositHeader;

public class BlacklistedHeaderFieldSetMapper implements FieldSetMapper<BlacklistedMsic> {

	@Override
	public BlacklistedMsic mapFieldSet(FieldSet fieldSet) throws BindException {
		BlacklistedMsicHeader header = new BlacklistedMsicHeader();
		header.setRecordType(fieldSet.readString("recordType"));
		header.setFileName(fieldSet.readString("fileName"));
		header.setCreationDate(fieldSet.readString("creationDate"));
		return header;
	}

}
