package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomer;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFundMasterDetail;

public class UnitTrustFundMasterMapper implements FieldSetMapper<UnitTrustFileAbs> {

	@Override
    public UnitTrustFileAbs mapFieldSet(FieldSet fieldSet) throws BindException {
		UnitTrustFundMasterDetail detail = new UnitTrustFundMasterDetail();
		detail.setRecordIndicator(fieldSet.readString("recordIndicator"));
		detail.setFundId (fieldSet.readString("fundId"));
		detail.setFundName (fieldSet.readString("fundName"));
		detail.setFundCurr (fieldSet.readString("fundCurr"));
		detail.setFundCurrNavPrice (fieldSet.readString("fundCurrNavPrice"));
		detail.setNavDate (fieldSet.readString("navDate"));
		detail.setProdCategoryCode (fieldSet.readString("prodCategoryCode"));
		detail.setProdCategoryDesc (fieldSet.readString("prodCategoryDesc"));
		detail.setRiskLevelCode(fieldSet.readString("riskLevelCode"));
		detail.setRiskLevelDesc (fieldSet.readString("riskLevelDesc"));
		detail.setMyrNavPrice (fieldSet.readString("myrNavPrice"));
    	return detail;
    }
}
