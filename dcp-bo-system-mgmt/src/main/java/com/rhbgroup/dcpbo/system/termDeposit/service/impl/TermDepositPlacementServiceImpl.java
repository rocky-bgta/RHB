package com.rhbgroup.dcpbo.system.termDeposit.service.impl;

import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.rhbgroup.dcp.connector.JmsConnector;
import com.rhbgroup.dcp.data.entity.fpx.FPXTxn;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaInterestRateLogic;
import com.rhbgroup.dcp.exception.DcpServiceException;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.util.ConfigUtil;
import com.rhbgroup.dcp.util.DateTimeUtil;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.ExtractExchangeRateServiceImpl;
import com.rhbgroup.dcpbo.system.model.InvestTxn;
import com.rhbgroup.dcpbo.system.model.StagedInvestTxn;
import com.rhbgroup.dcpbo.system.model.TdTxn;
import com.rhbgroup.dcpbo.system.model.UserProfile;
import com.rhbgroup.dcpbo.system.termDeposit.constants.JmsQueue;
import com.rhbgroup.dcpbo.system.termDeposit.dto.TermDepositPlacementConfirmationRequest;
import com.rhbgroup.dcpbo.system.termDeposit.dto.TermDepositPlacementConfirmationResponse;
import com.rhbgroup.dcpbo.system.termDeposit.dto.TermDepositPlacementRequest;
import com.rhbgroup.dcpbo.system.termDeposit.repository.InvestRepository;
import com.rhbgroup.dcpbo.system.termDeposit.repository.ServerConfigRepository;
import com.rhbgroup.dcpbo.system.termDeposit.repository.StagedInvestRepository;
import com.rhbgroup.dcpbo.system.termDeposit.repository.TxnRepository;
import com.rhbgroup.dcpbo.system.termDeposit.repository.UserProfileRepository;
import com.rhbgroup.dcpbo.system.termDeposit.service.TermDepositPlacementService;
import com.rhbgroup.dcp.invest.bizlogic.FPXEnquiryAEMessageLogic;
import com.rhbgroup.dcp.fpxcore.model.FPXRequest;

@Service
public class TermDepositPlacementServiceImpl implements TermDepositPlacementService{

	private static Logger logger = LogManager.getLogger(TermDepositPlacementServiceImpl.class);

	@Autowired
	private StagedInvestRepository stagedInvestRepository;
	
	@Autowired
	private InvestRepository investRepository;
	
	@Autowired
	private ServerConfigRepository serverConfigRepository;
	
	@Autowired
	private TxnRepository txnRepository;
	
	@Autowired
    private JmsConnector jmsConnector;
	
	@Autowired
	private UserProfileRepository userProfileRepository;
	
	@Autowired
	private FPXEnquiryAEMessageLogic fPXEnquiryAEMessageLogic;
	
	public TermDepositPlacementServiceImpl(){
	    	String jmsConnectionFactory = ConfigUtil.getDcpConfig("jms-queue:connection-factory");
	        try {
	            jmsConnector = new JmsConnector(jmsConnectionFactory, JmsQueue.TERM_DEPOSIT_PLACEMENT );
	        } catch (Exception e) {
	            jmsConnector = null;
	        }
	}
	
	public TermDepositPlacementServiceImpl(
			FPXEnquiryAEMessageLogic fPXEnquiryAEMessageLogic) {
        this.fPXEnquiryAEMessageLogic = fPXEnquiryAEMessageLogic;
    }
	
	
	@Override
	public ResponseEntity<BoData> termDepositPlacement(TermDepositPlacementConfirmationRequest request) {
	
		TermDepositPlacementConfirmationResponse termDepositPlacementResponse = new TermDepositPlacementConfirmationResponse();
		StagedInvestTxn stagedInvestTxn;
		ResponseEntity<BoData> responseEntity;
		
		if(request.getTxnStatus().equals("PENDING")) {
			FPXRequest fpxRequest = new FPXRequest();
			fpxRequest.setFpxSellerExId(request.getSellerExId());
			fpxRequest.setFpxSellerExOrderNo(request.getSellerExOrderNo());
			fpxRequest.setFpxSellerTxnTime(request.getSellerTxnTime());
			fpxRequest.setFpxSellerOrderNo(request.getSellerOrderNo());
			fpxRequest.setFpxSellerId(request.getSellerId());
			fpxRequest.setFpxSellerBankCode(request.getSellerBankCode());
			fpxRequest.setFpxTxnCurrency(request.getTxnCurrency());
			fpxRequest.setFpxTxnAmount(request.getTxnAmount());
			fpxRequest.setFpxBuyerBankId(request.getBankId());
			fpxRequest.setFpxVersion(request.getFpxVersion());


			String jsonStr = JsonUtil.objectToJson(fpxRequest);
			
			Capsule capsule = new Capsule();
	        capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
	        capsule.setMessageId(UUID.randomUUID().toString());
	        capsule.setProperty(Constants.OPERATION_NAME, "GetFPXEnquiryAEMessage");
			capsule.updateCurrentMessage(jsonStr);
			logger.debug("    capsule: " + capsule);
			FPXTxn fpxTxn = new FPXTxn();
						
			try {
				fPXEnquiryAEMessageLogic.enquiryAEMessage(capsule, fpxTxn);
			}catch (Exception e) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE,
						"Error calling " + e.getMessage() + ".executeBusinessLogic()");
			}
			logger.debug("    capsule: " + capsule);
	        logger.debug("        isOperationSuccesful: " + capsule.isOperationSuccessful());
			
			
		}else if(request.getTxnStatus().equals("SUCCESS")) {
			stagedInvestTxn = stagedInvestRepository.getStagedInvestTxnByTokenId(request.getTxnToken());
			if (stagedInvestTxn == null) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE,
						"No staged transaction is found for: " + request.getTxnToken());
			}
			
			InvestTxn investTxn = investRepository.getInvestTxnByTokenId(request.getTxnToken());
			if (investTxn != null) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE,
						"Term deposit placement was done earlier for " + request.getTxnToken());
			}
			
			UserProfile userProfile = userProfileRepository.getProfileByUserId(stagedInvestTxn.getUserId());
			
	    	insertInvestEntity(stagedInvestTxn);
	    	
	    	serverConfigRepository.getParameterValue("fpx.fd.collection.account");

	    	TdTxn tdTxn = txnRepository.getTdTxnByTokenId(request.getTxnToken());
			
			publishTransaction(tdTxn, stagedInvestTxn, userProfile);
		}
		
		termDepositPlacementResponse.setCode("10000");
		termDepositPlacementResponse.setStatusType("success");

		responseEntity = new ResponseEntity<>(termDepositPlacementResponse, HttpStatus.OK);
		
		return responseEntity;
	}
	
	void publishTransaction(TdTxn tdTxn, StagedInvestTxn stagedInvestTxn,UserProfile userProfile){
		
		TermDepositPlacementRequest termDepositPlacementRequest  = new TermDepositPlacementRequest();
		termDepositPlacementRequest.setTxnTokenId(stagedInvestTxn.getTxnTokenId());
		termDepositPlacementRequest.setMainFunction("TERM_DEPOSIT");
		termDepositPlacementRequest.setSubFunction("PLACEMENT");
		termDepositPlacementRequest.setCisNo(userProfile.getCisNo());
		termDepositPlacementRequest.setFrCtrl3(stagedInvestTxn.getFromAccountCtlr3());
		termDepositPlacementRequest.setIsCreditToPrinciple(tdTxn.getIsCreditToPrinciple());
		termDepositPlacementRequest.setFromAccountNo(stagedInvestTxn.getFromAccountNo());
		termDepositPlacementRequest.setFdAcctNo("");
		termDepositPlacementRequest.setProductCode(tdTxn.getTdProductCode());
		termDepositPlacementRequest.setTenure(tdTxn.getTenure());
		termDepositPlacementRequest.setCategoryName(tdTxn.getTdCategoryName());
		termDepositPlacementRequest.setProductName(tdTxn.getTdProductName());
		termDepositPlacementRequest.setConnectorCode(stagedInvestTxn.getFromAccountConnectorCode());
		termDepositPlacementRequest.setGstRate(stagedInvestTxn.getGstRate());
		termDepositPlacementRequest.setGstAmount(stagedInvestTxn.getGstAmount());
		termDepositPlacementRequest.setGstTreatmentType(stagedInvestTxn.getGstTreatmentType());
		termDepositPlacementRequest.setGstCalculationMethod(stagedInvestTxn.getGstCalculationMethod());
		termDepositPlacementRequest.setGstTaxCode(stagedInvestTxn.getGstTaxCode());
		termDepositPlacementRequest.setGstTxnId(stagedInvestTxn.getGstTxnId().toString());
		termDepositPlacementRequest.setStpIndicator("");
		termDepositPlacementRequest.setRef1("");
		termDepositPlacementRequest.setRef2("");
		termDepositPlacementRequest.setSenderName("");
		termDepositPlacementRequest.setRecipientRef("");
		termDepositPlacementRequest.setOtherPaymentDetails("");
		termDepositPlacementRequest.setBeneficiaryName("");
		termDepositPlacementRequest.setBankCode("");
		termDepositPlacementRequest.setStatementType("5");
		termDepositPlacementRequest.setMainFunction(stagedInvestTxn.getMainFunction());
		termDepositPlacementRequest.setFromBank("RHB");
		termDepositPlacementRequest.setAutoRenewal(Boolean.TRUE.equals(tdTxn.getAutoRenewal())?"1":"0");
		termDepositPlacementRequest.setUserCd1(Boolean.TRUE.equals(tdTxn.getIsJointOwnership())?"02":"01");
		termDepositPlacementRequest.setServiceCharge(stagedInvestTxn.getServiceCharge());
		termDepositPlacementRequest.setEntity(Boolean.TRUE.equals(tdTxn.getIsIslamic())?"4":"0");
		termDepositPlacementRequest.setAmount(stagedInvestTxn.getAmount());
		termDepositPlacementRequest.setInterestDistCode(Boolean.TRUE.equals(tdTxn.getAutoRenewal())?"2":"0");
		termDepositPlacementRequest.setRefId(stagedInvestTxn.getRefId());
		termDepositPlacementRequest.setChannel(stagedInvestTxn.getChannel());
		termDepositPlacementRequest.setIpAddress(stagedInvestTxn.getFromIPAddress());
		termDepositPlacementRequest.setCollCtrl3("068");
		termDepositPlacementRequest.setAffiliateAcctCtrl3(stagedInvestTxn.getFromAccountCtlr3());
		termDepositPlacementRequest.setAffiliateAcctNo(stagedInvestTxn.getFromAccountNo());
		termDepositPlacementRequest.setGstRefNo(Constants.SENDER_GST_TRANS_REF_NO);
		
		
        if (null == jmsConnector) {
        	throw new CommonException(CommonException.GENERIC_ERROR_CODE,"JMS connection failed ");
        }
        String queuePayload = JsonUtil.objectToJson(termDepositPlacementRequest);
		Capsule transactionCapsule = new Capsule(queuePayload);
		jmsConnector.send(transactionCapsule);
    }
	
	private InvestTxn insertInvestEntity(StagedInvestTxn stagedInvestTxn){
		InvestTxn investTxn= new InvestTxn();
		investTxn.setTxnId(UUID.randomUUID().toString());
		investTxn.setRefId(stagedInvestTxn.getRefId());
		investTxn.setMainFunction(stagedInvestTxn.getMainFunction());
		investTxn.setSubFunction(stagedInvestTxn.getSubFunction());
		investTxn.setFromAccountNo(stagedInvestTxn.getFromAccountNo());
		investTxn.setFromAccountName(stagedInvestTxn.getFromAccountName());
		investTxn.setAmount(stagedInvestTxn.getAmount());
		investTxn.setMultiFactorAuth(stagedInvestTxn.getMultiFactorAuth());
		investTxn.setTxnStatus("PENDING");
		investTxn.setTxnTime(DateTimeUtil.getCurrentTimestamp());
		investTxn.setServiceCharge(stagedInvestTxn.getServiceCharge());
		investTxn.setGstCalculationMethod(stagedInvestTxn.getGstCalculationMethod());
		investTxn.setGstTaxCode(stagedInvestTxn.getGstTaxCode());
		investTxn.setGstTxnId(stagedInvestTxn.getGstTxnId());
		investTxn.setGstRate(stagedInvestTxn.getGstRate());
		investTxn.setGstAmount(stagedInvestTxn.getGstAmount());
		investTxn.setFromIPAddress(stagedInvestTxn.getFromIPAddress());
		investTxn.setFromAccountConnectorCode(stagedInvestTxn.getFromAccountConnectorCode());
		investTxn.setTxnStatusCode("10001");
		investTxn.setChannel(stagedInvestTxn.getChannel());
		investTxn.setTxnTokenId(stagedInvestTxn.getTxnTokenId());
		investTxn.setTxnCcy(stagedInvestTxn.getTxnCcy());
		investTxn.setIsQuickPay(stagedInvestTxn.getIsQuickPay());
		investTxn.setCreatedBy(stagedInvestTxn.getCreatedBy());
		investTxn.setUpdatedBy(stagedInvestTxn.getUpdatedBy());
		investTxn.setCreatedTime(DateTimeUtil.getCurrentTimestamp());
		investTxn.setUpdatedTime(DateTimeUtil.getCurrentTimestamp());
		investTxn.setUserId(stagedInvestTxn.getUserId());
		investTxn.setGstTreatmentType(stagedInvestTxn.getGstTreatmentType());
		
		investRepository.save(investTxn);
		
		return investTxn;
	}
}
