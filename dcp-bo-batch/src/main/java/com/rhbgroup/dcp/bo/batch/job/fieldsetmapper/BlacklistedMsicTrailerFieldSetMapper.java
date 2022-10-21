package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.BlacklistedMsic;
import com.rhbgroup.dcp.bo.batch.job.model.BlacklistedMsicTrailer;
import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDeposit;
import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDepositTrailer;

public class BlacklistedMsicTrailerFieldSetMapper implements FieldSetMapper<BlacklistedMsic>{

	@Override
	public BlacklistedMsic mapFieldSet(FieldSet fieldSet) throws BindException {
		BlacklistedMsicTrailer trailer = new BlacklistedMsicTrailer();
		trailer.setRecordType(fieldSet.readString("recordType"));
		trailer.setTotalRecordCount(fieldSet.readString("totalRecordCount"));
		return trailer;
	}

}
