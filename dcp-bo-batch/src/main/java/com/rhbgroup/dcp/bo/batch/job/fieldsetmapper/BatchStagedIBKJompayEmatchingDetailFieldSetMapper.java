package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKJompayEmatching;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKJompayEmatchingDetail;

public class BatchStagedIBKJompayEmatchingDetailFieldSetMapper implements FieldSetMapper<BatchStagedIBKJompayEmatching> {

	@Override
	public BatchStagedIBKJompayEmatching mapFieldSet(FieldSet fieldSet) throws BindException {
		BatchStagedIBKJompayEmatchingDetail batchStagedIBKJompay = new BatchStagedIBKJompayEmatchingDetail();
		batchStagedIBKJompay.setChannelId(fieldSet.readString("channelId"));
		batchStagedIBKJompay.setChannelStatus(fieldSet.readString("channelStatus"));
		batchStagedIBKJompay.setApplicationId(fieldSet.readString("applicationId"));
		batchStagedIBKJompay.setAcctCtrl1(fieldSet.readString("acctCtrl1"));
		batchStagedIBKJompay.setAcctCtrl2(fieldSet.readString("acctCtrl2"));
		batchStagedIBKJompay.setAcctCtrl3(fieldSet.readString("acctCtrl3"));
		batchStagedIBKJompay.setAccountNo(fieldSet.readString("accountNo"));
		batchStagedIBKJompay.setDebitCreditInd(fieldSet.readString("debitCreditInd"));
		batchStagedIBKJompay.setUserTranCode(fieldSet.readString("userTranCode"));
		batchStagedIBKJompay.setAmount(Double.parseDouble(StringUtils.remove(fieldSet.readString("amount"), "+")));
		batchStagedIBKJompay.setTxnBranch(fieldSet.readString("txnBranch"));
		batchStagedIBKJompay.setTxnDate(fieldSet.readString("txnDate"));
		batchStagedIBKJompay.setTxnTime(fieldSet.readString("txnTime"));
		return batchStagedIBKJompay;
	}

}
