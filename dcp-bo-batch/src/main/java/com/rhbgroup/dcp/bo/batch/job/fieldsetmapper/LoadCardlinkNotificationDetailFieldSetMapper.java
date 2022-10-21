package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;
import com.rhbgroup.dcp.bo.batch.job.model.LoadCardlinkNotification;
import com.rhbgroup.dcp.bo.batch.job.model.LoadCardlinkNotificationDetail;

public class LoadCardlinkNotificationDetailFieldSetMapper implements FieldSetMapper<LoadCardlinkNotification>{

	@Override
	public LoadCardlinkNotification mapFieldSet(FieldSet fieldSet) throws BindException {
		LoadCardlinkNotificationDetail detail = new LoadCardlinkNotificationDetail();
		detail.setRecordIndicator(fieldSet.readString("recordIndicator"));
		detail.setRunningNumber(fieldSet.readString("runningNumber"));
		detail.setCreditCard(fieldSet.readString("creditCard"));
		detail.setPaymentDueDate(fieldSet.readString("paymentDueDate"));
		detail.setCreditCardType(fieldSet.readString("creditCardType"));
		detail.setMinAmount(fieldSet.readString("minAmount"));
		detail.setOutstandingAmount(fieldSet.readString("outstandingAmount"));
		detail.setStatementAmount(fieldSet.readString("statementAmount"));
		detail.setStatementDate(fieldSet.readString("statementDate"));
		return detail;
	}

}
