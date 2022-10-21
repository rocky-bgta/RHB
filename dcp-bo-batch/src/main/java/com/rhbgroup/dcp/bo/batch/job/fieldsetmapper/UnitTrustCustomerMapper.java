package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomer;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomerDetail;

public class UnitTrustCustomerMapper implements FieldSetMapper<UnitTrustFileAbs> {

	@Override
	public UnitTrustFileAbs mapFieldSet(FieldSet fieldSet) throws BindException {
		UnitTrustCustomerDetail detail = new UnitTrustCustomerDetail();
		detail.setRecordIndicator(fieldSet.readString("recordIndicator"));
		detail.setCisNo(fieldSet.readString("cisNo"));
		detail.setCustomerName(fieldSet.readString("customerName"));
		return detail;
	}
}
