package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustAccountHolding;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustAccountHoldingDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;

public class UnitTrustAccountHoldingMapper implements FieldSetMapper<UnitTrustFileAbs> {

	@Override
	public UnitTrustFileAbs mapFieldSet(FieldSet fieldSet) throws BindException {
		UnitTrustAccountHoldingDetail detail = new UnitTrustAccountHoldingDetail();
		detail.setAcctNo(fieldSet.readString("accountNo"));
		detail.setFundId(fieldSet.readString("fundId"));
		detail.setHoldingUnit(fieldSet.readString("holdingUnit"));
		detail.setFundCurrMarketVal(fieldSet.readString("fundCurrMarketVal"));
		detail.setFundCurrUnrealisedGainLoss(fieldSet.readString("fundCurrUnrealisedGainLoss"));
		detail.setFundCurrUnrealisedGainLossPercent(fieldSet.readString("fundCurrUnrealisedGainLossPercent"));
		detail.setFundCurrInvestAmnt(fieldSet.readString("fundCurrInvestAmnt"));
		detail.setFundCurrAvgUnitPrice(fieldSet.readString("fundCurrAvgUnitPrice"));
		detail.setFundMyrMarketVal(fieldSet.readString("fundMyrMarketVal"));
		detail.setFundMyrUnrealisedGainLoss(fieldSet.readString("fundMyrUnrealisedGainLoss"));
		detail.setFundMyrUnrealisedGainLossPercent(fieldSet.readString("fundMyrUnrealisedGainLossPercent"));
		detail.setFundMyrInvestAmnt(fieldSet.readString("fundMyrInvestAmnt"));
		detail.setFundMyrAvgUnitPrice(fieldSet.readString("fundMyrAvgUnitPrice"));
		return detail;
	}
}
