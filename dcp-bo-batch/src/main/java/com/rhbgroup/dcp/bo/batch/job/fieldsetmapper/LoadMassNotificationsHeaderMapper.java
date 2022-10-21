package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.LoadMassNotifications;
import com.rhbgroup.dcp.bo.batch.job.model.LoadMassNotificationsHeader;

public class LoadMassNotificationsHeaderMapper implements FieldSetMapper<LoadMassNotifications> {

	@Override
	public LoadMassNotifications mapFieldSet(FieldSet fieldSet) throws BindException {
		LoadMassNotificationsHeader loadMassNotificationsHeader = new LoadMassNotificationsHeader();
		
		loadMassNotificationsHeader.setRecordIndicator(fieldSet.readString("recordIndicator"));
		loadMassNotificationsHeader.setEventCode(fieldSet.readString("eventCode"));
		
		return loadMassNotificationsHeader;
	}

}
