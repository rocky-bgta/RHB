package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;
import com.rhbgroup.dcp.bo.batch.job.model.LoadCardlinkNotification;
import com.rhbgroup.dcp.bo.batch.job.model.LoadCardlinkNotificationTrailer;

public class LoadCardlinkNotificationTrailerFieldSetMapper implements FieldSetMapper<LoadCardlinkNotification>{

	@Override
	public LoadCardlinkNotification mapFieldSet(FieldSet fieldSet) throws BindException {
		LoadCardlinkNotificationTrailer trailer = new LoadCardlinkNotificationTrailer();
		trailer.setRecordIndicator(fieldSet.readString("recordIndicator"));
		trailer.setRecordCount(fieldSet.readString("recordCount"));
		trailer.setHashValue(fieldSet.readString("hashValue"));
		return trailer;
	}

}
