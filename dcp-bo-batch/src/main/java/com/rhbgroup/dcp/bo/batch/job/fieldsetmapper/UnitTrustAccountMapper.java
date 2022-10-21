package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustAccount;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustAccountDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;

public class UnitTrustAccountMapper implements FieldSetMapper<UnitTrustFileAbs> {

	@Override
    public UnitTrustFileAbs mapFieldSet(FieldSet fieldSet) throws BindException {
		UnitTrustAccountDetail detail = new UnitTrustAccountDetail();
		detail.setAccountNo(fieldSet.readString("accountNo"));
		detail.setSignatoryCode(fieldSet.readString("signatoryCode"));
		detail.setSignatoryDescription (fieldSet.readString("signatoryDescription"));
		detail.setAccountType(fieldSet.readString("accountType"));
		detail.setAccountStatusCode(fieldSet.readString("accountStatusCode"));
		detail.setAccountStatusDesc(fieldSet.readString("accountStatusDesc"));
		detail.setAccountInvestProduct (fieldSet.readString("accountInvestProduct"));
		detail.setLastPerformedTxnDate (fieldSet.readString("lastPerformedTxnDate"));
    	return detail;
    }
}
