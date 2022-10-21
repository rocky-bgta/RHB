package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.BlacklistedMsic;
import com.rhbgroup.dcp.bo.batch.job.model.BlacklistedMsicDetail;

public class BlacklistedDetailFieldSetMapper implements FieldSetMapper<BlacklistedMsic>{

	@Override
	public BlacklistedMsic mapFieldSet(FieldSet fieldSet) throws BindException {
		BlacklistedMsicDetail detail = new BlacklistedMsicDetail();
		detail.setRecordType(fieldSet.readString("recordType"));
		detail.setId(fieldSet.readString("id"));
		detail.setMsicCode(fieldSet.readString("id"));
		detail.setMsic(fieldSet.readString("msic"));
		detail.setDescription(fieldSet.readString("description"));
		detail.setAccountType(fieldSet.readString("accountType"));
		detail.setIslamicIndicator(fieldSet.readString("islamicIndicator"));
		detail.setStatus(fieldSet.readString("status"));
		detail.setCreateApprovedId(fieldSet.readString("createApprovedId"));
		detail.setCreateApprovedDate(fieldSet.readString("createApprovedDate"));
		detail.setUpdateApprovedId(fieldSet.readString("updateApprovedId"));
		detail.setUpdateApprovedDate(fieldSet.readString("updateApprovedDate"));
		return detail;
	}

}
