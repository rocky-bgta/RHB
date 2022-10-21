package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.LoadMassNotifications;
import com.rhbgroup.dcp.bo.batch.job.model.LoadMassNotificationsDetail;

public class LoadMassNotificationsDetailMapper implements FieldSetMapper<LoadMassNotifications> {

	@Override
	public LoadMassNotifications mapFieldSet(FieldSet fieldSet) throws BindException {
		LoadMassNotificationsDetail loadMassNotificationsDetail = new LoadMassNotificationsDetail();
		
		loadMassNotificationsDetail.setRecordIndicator(fieldSet.readString("recordIndicator"));
		loadMassNotificationsDetail.setContent(fieldSet.readString("content"));
		
		return loadMassNotificationsDetail;
	}

}
