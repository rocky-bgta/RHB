package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileHeader;

public class UnitTrustFileHeaderMapper implements FieldSetMapper<UnitTrustFileAbs> {

	@Override
	public UnitTrustFileAbs mapFieldSet(FieldSet fieldSet) throws BindException {
		UnitTrustFileHeader header = new UnitTrustFileHeader();
		header.setRecordIndicator(fieldSet.readString("recordIndicator"));
		header.setProcessingDate(fieldSet.readString("processingDate"));
		header.setSystemDate(fieldSet.readString("systemDate"));
		header.setSystemTime(fieldSet.readString("systemTime"));
		return header;
	}
}
