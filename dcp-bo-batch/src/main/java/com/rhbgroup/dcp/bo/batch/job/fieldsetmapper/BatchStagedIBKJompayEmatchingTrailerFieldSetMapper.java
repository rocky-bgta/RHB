package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKJompayEmatching;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKJompayEmatchingTrailer;

public class BatchStagedIBKJompayEmatchingTrailerFieldSetMapper implements FieldSetMapper<BatchStagedIBKJompayEmatching>{

	@Override
	public BatchStagedIBKJompayEmatching mapFieldSet(FieldSet fieldSet) throws BindException {
		BatchStagedIBKJompayEmatchingTrailer trailer = new BatchStagedIBKJompayEmatchingTrailer();
		trailer.setRecordType(fieldSet.readString("recordType"));
		trailer.setTotalRecord( Integer.parseInt(fieldSet.readString("totalRecord")));
		trailer.setTotalAmount(Double.parseDouble(fieldSet.readString("totalAmount")));
		return trailer;
	}

}
