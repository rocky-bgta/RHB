package com.rhbgroup.dcpbo.system.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { StagedInvestTxn.class })
public class StagedInvestTxnTest {
	private static Logger logger = LogManager.getLogger(StagedInvestTxnTest.class);
	@Test
	public void testStagedInvestTxn() {
		logger.debug("testStagedInvestTxn() Model");

		 Integer id = 1;
		 Integer txnTokenId=2;
	     String multiFactorAuth="multiFactorAuth";
		 String mainFunction="mainFunction";		
	     String fromAccountNo="fromAccountNo";
	     String subFunction="subFunction";
	     String fromAccountName="fromAccountName";
	     Integer toFavouriteId = 1;
	     String toAccountNo = "toAccountNo";
	     BigDecimal amount = new BigDecimal(1);
	     Boolean isSetupFavourite = true;
	     String toAccountName = "toAccountName";
	     Boolean isSetupQuickLink = true;
	     Boolean isSetupQuickPay = true;
	     BigDecimal serviceCharge = new BigDecimal(1);
	     String gstTaxCode = "gstTaxCode";
	     Integer gstTxnId = 1;
	     String gstCalculationMethod = "gstCalculationMethod";
	     String gstTreatmentType = "gstTreatmentType";
	     BigDecimal gstAmount = new BigDecimal(1);
	     BigDecimal gstRate = new BigDecimal(1);
	     String fromAccountCtlr3="fromAccountCtlr3";
	     Boolean isQuickPay = true;
		 BigDecimal serviceChargeWithGst = new BigDecimal(1);
	     String toAccountCtrl3="toAccountCtrl3";
	     String fromIPAddress="fromIPAddress";
	     Integer securePlusTokenId=1;
	     String signingData="signingData";
	     String fromAccountConnectorCode="fromAccountConnectorCode";
	     String paymentMethod="paymentMethod";
	     String channel="channel";
	     Boolean isPreLogin = true;
	     String curfId="curfId";
	     String accessMethod="accessMethod";
	     String errorDesc="errorDesc";
	     String deviceId="deviceId";
	     String refId="refId";
	     String txnId="txnId";
	     Integer userId=1;
	     String txnCcy="txnCcy";
	     String subChannel="subChannel";
	     Date date = new Date();
	     long time = date.getTime();
	     Timestamp createdTime = new Timestamp(time );
	     Timestamp updatedTime = new Timestamp(time );
	     String createdBy="createdBy";
	     String updatedBy="updatedBy";
		
	    StagedInvestTxn stagedInvestTxn = new StagedInvestTxn();
		stagedInvestTxn.setId(id);   
		stagedInvestTxn.setTxnTokenId(txnTokenId);
		stagedInvestTxn.setMultiFactorAuth(multiFactorAuth);
		stagedInvestTxn.setMainFunction(mainFunction);
		stagedInvestTxn.setFromAccountNo(fromAccountNo);
		stagedInvestTxn.setSubFunction(subFunction);
		stagedInvestTxn.setFromAccountName(fromAccountName);
		stagedInvestTxn.setToFavouriteId(toFavouriteId);
		stagedInvestTxn.setToAccountNo(toAccountNo);
		stagedInvestTxn.setAmount(amount);
		stagedInvestTxn.setIsSetupFavourite(isSetupFavourite);
		stagedInvestTxn.setToAccountName(toAccountName);
		stagedInvestTxn.setIsSetupQuickLink(isSetupQuickLink);
		stagedInvestTxn.setIsSetupQuickPay(isSetupQuickPay);
		stagedInvestTxn.setServiceCharge(serviceCharge);
		stagedInvestTxn.setGstTaxCode(gstTaxCode);
		stagedInvestTxn.setGstTxnId(gstTxnId);
		stagedInvestTxn.setGstCalculationMethod(gstCalculationMethod);
		stagedInvestTxn.setGstTreatmentType(gstTreatmentType);
		stagedInvestTxn.setGstAmount(gstAmount);
		stagedInvestTxn.setGstRate(gstRate);
		stagedInvestTxn.setFromAccountCtlr3(fromAccountCtlr3);
		stagedInvestTxn.setIsQuickPay(isQuickPay);
		stagedInvestTxn.setServiceChargeWithGst(serviceChargeWithGst);
		stagedInvestTxn.setToAccountCtrl3(toAccountCtrl3);
		stagedInvestTxn.setFromIPAddress(fromIPAddress);
		stagedInvestTxn.setSecurePlusTokenId(securePlusTokenId);
		stagedInvestTxn.setSigningData(signingData);
		stagedInvestTxn.setFromAccountConnectorCode(fromAccountConnectorCode);
		stagedInvestTxn.setPaymentMethod(paymentMethod);
		stagedInvestTxn.setChannel(channel);
		stagedInvestTxn.setIsPreLogin(isPreLogin);
		stagedInvestTxn.setCurfId(curfId);
		stagedInvestTxn.setAccessMethod(accessMethod);
		stagedInvestTxn.setErrorDesc(errorDesc);
		stagedInvestTxn.setDeviceId(deviceId);
		stagedInvestTxn.setRefId(refId);
		stagedInvestTxn.setTxnId(txnId);
		stagedInvestTxn.setUserId(userId);
		stagedInvestTxn.setTxnCcy(txnCcy);
		stagedInvestTxn.setSubChannel(subChannel);
		stagedInvestTxn.setCreatedTime(createdTime);
		stagedInvestTxn.setUpdatedTime(updatedTime);
		stagedInvestTxn.setCreatedBy(createdBy);
		stagedInvestTxn.setUpdatedBy(updatedBy);

		
		
		assertEquals(id, stagedInvestTxn.getId());
		assertEquals(txnTokenId, stagedInvestTxn.getTxnTokenId());
		assertEquals(multiFactorAuth, stagedInvestTxn.getMultiFactorAuth());
		assertEquals(mainFunction, stagedInvestTxn.getMainFunction());
		assertEquals(fromAccountNo, stagedInvestTxn.getFromAccountNo());
		assertEquals(subFunction, stagedInvestTxn.getSubFunction());
		assertEquals(fromAccountName, stagedInvestTxn.getFromAccountName());
		assertEquals(toFavouriteId, stagedInvestTxn.getToFavouriteId());
		assertEquals(toAccountNo, stagedInvestTxn.getToAccountNo());
		assertEquals(amount, stagedInvestTxn.getAmount());
		assertEquals(isSetupFavourite, stagedInvestTxn.getIsSetupFavourite());
		assertEquals(toAccountName, stagedInvestTxn.getToAccountName());
		assertEquals(isSetupQuickLink, stagedInvestTxn.getIsSetupQuickLink());
		assertEquals(isSetupQuickPay, stagedInvestTxn.getIsSetupQuickPay());
		assertEquals(serviceCharge, stagedInvestTxn.getServiceCharge());
		assertEquals(gstTaxCode, stagedInvestTxn.getGstTaxCode());
		assertEquals(gstTxnId, stagedInvestTxn.getGstTxnId());
		assertEquals(gstCalculationMethod, stagedInvestTxn.getGstCalculationMethod());
		assertEquals(gstTreatmentType, stagedInvestTxn.getGstTreatmentType());
		assertEquals(gstAmount, stagedInvestTxn.getGstAmount());
		assertEquals(gstRate, stagedInvestTxn.getGstRate());
		assertEquals(fromAccountCtlr3, stagedInvestTxn.getFromAccountCtlr3());
		assertEquals(isQuickPay, stagedInvestTxn.getIsQuickPay());
		assertEquals(serviceChargeWithGst, stagedInvestTxn.getServiceChargeWithGst());
		assertEquals(toAccountCtrl3, stagedInvestTxn.getToAccountCtrl3());
		assertEquals(fromIPAddress, stagedInvestTxn.getFromIPAddress());
		assertEquals(securePlusTokenId, stagedInvestTxn.getSecurePlusTokenId());
		assertEquals(signingData, stagedInvestTxn.getSigningData());
		assertEquals(fromAccountConnectorCode, stagedInvestTxn.getFromAccountConnectorCode());
		assertEquals(paymentMethod, stagedInvestTxn.getPaymentMethod());
		assertEquals(channel, stagedInvestTxn.getChannel());
		assertEquals(isPreLogin, stagedInvestTxn.getIsPreLogin());
		assertEquals(curfId, stagedInvestTxn.getCurfId());
		assertEquals(accessMethod, stagedInvestTxn.getAccessMethod());
		assertEquals(errorDesc, stagedInvestTxn.getErrorDesc());
		assertEquals(deviceId, stagedInvestTxn.getDeviceId());
		assertEquals(refId, stagedInvestTxn.getRefId());
		assertEquals(txnId, stagedInvestTxn.getTxnId());
		assertEquals(userId, stagedInvestTxn.getUserId());
		assertEquals(txnCcy, stagedInvestTxn.getTxnCcy());
		assertEquals(subChannel, stagedInvestTxn.getSubChannel());
		assertEquals(createdTime, stagedInvestTxn.getCreatedTime());
		assertEquals(updatedTime, stagedInvestTxn.getUpdatedTime());
		assertEquals(createdBy, stagedInvestTxn.getCreatedBy());
		assertEquals(updatedBy, stagedInvestTxn.getUpdatedBy());
	}
}
