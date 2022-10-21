package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDeposit;
import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDepositDetail;

public class LoadTermDepositDetailFieldSetMapper implements FieldSetMapper<LoadTermDeposit>{

	@Override
	public LoadTermDeposit mapFieldSet(FieldSet fieldSet) throws BindException {
		LoadTermDepositDetail detail = new LoadTermDepositDetail();
		detail.setRecordType(fieldSet.readString("recordType"));
		detail.setControl1(fieldSet.readString("control1"));
		detail.setProductType(fieldSet.readString("productType"));
		detail.setProductDescription(fieldSet.readString("productDescription"));
		detail.setTenure(Integer.parseInt(fieldSet.readString("tenure")));
		detail.setInterestRate(Double.parseDouble(fieldSet.readString("interestRate")));
		detail.setEndDate(fieldSet.readString("endDate"));
		detail.setFilter(fieldSet.readString("filter"));
		detail.setEndIndicator(fieldSet.readString("endIndicator"));
		return detail;
	}

}
