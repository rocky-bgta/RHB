package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;
import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDeposit;
import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDepositTrailer;

public class LoadTermDepositTrailerFieldSetMapper implements FieldSetMapper<LoadTermDeposit>{

	@Override
	public LoadTermDeposit mapFieldSet(FieldSet fieldSet) throws BindException {
		LoadTermDepositTrailer trailer = new LoadTermDepositTrailer();
		trailer.setRecordType(fieldSet.readString("recordType"));
		trailer.setTotalRecordCount(fieldSet.readString("totalRecordCount"));
		trailer.setTotalAmount(Integer.parseInt(fieldSet.readString("totalAmount")));
		trailer.setFilter(fieldSet.readString("filter"));
		trailer.setEndIndicator(fieldSet.readString("endIndicator"));
		return trailer;
	}

}
