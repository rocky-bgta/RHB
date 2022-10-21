package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileTrailer;

public class UnitTrustFileTrailerMapper implements FieldSetMapper<UnitTrustFileAbs> {

	@Override
    public UnitTrustFileAbs mapFieldSet(FieldSet fieldSet) throws BindException {
		UnitTrustFileTrailer detail = new UnitTrustFileTrailer();
		detail.setRecordIndicator(fieldSet.readString("recordIndicator"));
		detail.setRecCount(fieldSet.readString("recordCount"));
		return detail;
    }
}
