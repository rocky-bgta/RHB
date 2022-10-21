package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;
import com.rhbgroup.dcp.bo.batch.job.model.LoadCardlinkNotification;
import com.rhbgroup.dcp.bo.batch.job.model.LoadCardlinkNotificationHeader;

public class LoadCardlinkNotificationHeaderFieldSetMapper implements FieldSetMapper<LoadCardlinkNotification>{

	@Override
	public LoadCardlinkNotification mapFieldSet(FieldSet fieldSet) throws BindException {
		LoadCardlinkNotificationHeader header = new LoadCardlinkNotificationHeader();
		header.setProcessingDate(fieldSet.readString("processingDate"));
		header.setSystemDate(fieldSet.readString("systemDate"));
		header.setSystemTime(fieldSet.readString("systemTime"));
		header.setRecordIndicator(fieldSet.readString("recordIndicator"));
		header.setKeyType(fieldSet.readString("keyType"));
		header.setEventCode(fieldSet.readString("eventCode") );
		return header;
	}

}
