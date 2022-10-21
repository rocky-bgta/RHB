package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomerRelationDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;

public class UnitTrustCustomerRelationshipMapper implements FieldSetMapper<UnitTrustFileAbs> {

	@Override
    public UnitTrustFileAbs mapFieldSet(FieldSet fieldSet) throws BindException {
		UnitTrustCustomerRelationDetail detail = new UnitTrustCustomerRelationDetail();
		detail.setRecordIndicator(fieldSet.readString("recordIndicator"));
		detail.setCisNo(fieldSet.readString("cisNo"));
		detail.setJoinType(fieldSet.readString("joinType"));
		detail.setAccountNo(fieldSet.readString("accountNo"));
    	return detail;
    }
}
