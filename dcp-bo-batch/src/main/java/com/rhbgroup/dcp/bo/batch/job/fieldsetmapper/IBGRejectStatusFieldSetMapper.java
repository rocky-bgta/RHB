package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBGRejectStatusTxn;

public class IBGRejectStatusFieldSetMapper implements FieldSetMapper<BatchStagedIBGRejectStatusTxn> {
    final static Logger logger = Logger.getLogger(IBGRejectStatusFieldSetMapper.class);
	@Override
	public BatchStagedIBGRejectStatusTxn mapFieldSet(FieldSet fieldSet) throws BindException {
		// TODO Auto-generated method stub
		BatchStagedIBGRejectStatusTxn ibgRejectStatus = new BatchStagedIBGRejectStatusTxn();
		ibgRejectStatus.setTeller(fieldSet.readString("teller"));
		ibgRejectStatus.setTrace(fieldSet.readString("trace"));
		ibgRejectStatus.setRef1(fieldSet.readString("ref1"));
		ibgRejectStatus.setName(fieldSet.readString("name"));
		ibgRejectStatus.setRejectCode(fieldSet.readString("rejectCode"));
		ibgRejectStatus.setAccountNo(fieldSet.readString("accountNumber"));
		ibgRejectStatus.setBeneName(fieldSet.readString("beneName"));
		ibgRejectStatus.setBeneAccount(fieldSet.readString("beneAccount"));
        String dateString = fieldSet.readString("date");
        String amountString= StringUtils.stripStart(fieldSet.readString("amount"),"0");
        ibgRejectStatus.setDate(dateString);
        ibgRejectStatus.setAmount(amountString);
		return ibgRejectStatus;
	}


}
