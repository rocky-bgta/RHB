package com.rhbgroup.dcpbo.system.termDeposit.service.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.connector.JmsConnector;
import com.rhbgroup.dcp.invest.bizlogic.FPXEnquiryAEMessageLogic;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.model.StagedInvestTxn;
import com.rhbgroup.dcpbo.system.model.TdTxn;
import com.rhbgroup.dcpbo.system.model.UserProfile;
import com.rhbgroup.dcpbo.system.termDeposit.controller.TermDepositControllerTest;
import com.rhbgroup.dcpbo.system.termDeposit.dto.TermDepositPlacementConfirmationRequest;
import com.rhbgroup.dcpbo.system.termDeposit.dto.TermDepositPlacementConfirmationResponse;
import com.rhbgroup.dcpbo.system.termDeposit.repository.InvestRepository;
import com.rhbgroup.dcpbo.system.termDeposit.repository.ServerConfigRepository;
import com.rhbgroup.dcpbo.system.termDeposit.repository.StagedInvestRepository;
import com.rhbgroup.dcpbo.system.termDeposit.repository.TxnRepository;
import com.rhbgroup.dcpbo.system.termDeposit.repository.UserProfileRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TermDepositPlacementServiceTest.class,TermDepositPlacementServiceImpl.class})
public class TermDepositPlacementServiceTest {
	
	@MockBean
	private StagedInvestRepository stagedInvestRepository;
	
	@MockBean
	private InvestRepository investRepository;
	
	@MockBean
	private ServerConfigRepository serverConfigRepository;
	
	@MockBean
	private TxnRepository txnRepository;
	
	@MockBean
    private JmsConnector jmsConnector;
	
	@MockBean
	private UserProfileRepository userProfileRepository;
	
	@MockBean
	private FPXEnquiryAEMessageLogic fPXEnquiryAEMessageLogic;
	
	@Autowired
	private TermDepositPlacementServiceImpl termDepositPlacementServiceImpl;
	
	private TermDepositPlacementConfirmationRequest termDepositPlacementConfirmationRequest;
	
	private TermDepositPlacementConfirmationRequest termDepositPlacementConfirmationRequest1;
	
	private static Logger logger = LogManager.getLogger(TermDepositPlacementServiceTest.class);

	@Before
    public void setup(){

		termDepositPlacementConfirmationRequest = new TermDepositPlacementConfirmationRequest();
		
		termDepositPlacementConfirmationRequest.setTxnStatus("SUCCESS");
		termDepositPlacementConfirmationRequest.setTxnToken("26722");
		
		termDepositPlacementConfirmationRequest1 = new TermDepositPlacementConfirmationRequest();
		
		termDepositPlacementConfirmationRequest1.setTxnStatus("PENDING");
		termDepositPlacementConfirmationRequest1.setTxnToken("26722");

    }
	
	@Test
	public void termDepositPlacementServiceSuccessfulTest() {
		
		BigDecimal amount = new BigDecimal("1");
		
		StagedInvestTxn stagedInvestTxn = new StagedInvestTxn();
		stagedInvestTxn.setId(1);
		stagedInvestTxn.setTxnTokenId(48);
		stagedInvestTxn.setMultiFactorAuth("NA");
		stagedInvestTxn.setMainFunction("ASNB");
		stagedInvestTxn.setSubFunction("Test");
		stagedInvestTxn.setFromAccountNo("16431700105150");
		stagedInvestTxn.setFromAccountName("JUNIOR CIS 5");
		stagedInvestTxn.setToFavouriteId(5653);
		stagedInvestTxn.setToAccountNo("21412900273480");
		stagedInvestTxn.setToAccountName("Test");
		stagedInvestTxn.setAmount(amount);
		stagedInvestTxn.setServiceCharge(amount);
		stagedInvestTxn.setGstRate(amount);
		stagedInvestTxn.setGstAmount(amount);
		stagedInvestTxn.setGstTreatmentType("04");
		stagedInvestTxn.setGstCalculationMethod("I");
		stagedInvestTxn.setGstTaxCode("OS");
		stagedInvestTxn.setGstTxnId(37);
		stagedInvestTxn.setFromAccountCtlr3("068");
		stagedInvestTxn.setIsQuickPay(Boolean.TRUE);
		stagedInvestTxn.setServiceChargeWithGst(amount);
		stagedInvestTxn.setToAccountCtrl3("068");
		stagedInvestTxn.setFromIPAddress("10.186.48.16");
		stagedInvestTxn.setSecurePlusTokenId(34);
		stagedInvestTxn.setFromAccountConnectorCode("IND");
		stagedInvestTxn.setPaymentMethod("CASA");
		stagedInvestTxn.setChannel("DB");
		stagedInvestTxn.setRefId("1594889363913103");
		stagedInvestTxn.setTxnId("2c1f3b7c-32fd-45ee-8abb-8832a1d30f9c");
		stagedInvestTxn.setUserId(2381);
		stagedInvestTxn.setTxnCcy("MYR");
		
		UserProfile userProfile = new UserProfile();
		userProfile.setId(1);
		userProfile.setCisNo("123");
		
		TdTxn tdTxn = new TdTxn();
		
		tdTxn.setIsCreditToPrinciple(Boolean.TRUE);
		tdTxn.setTdProductCode("TD");
		tdTxn.setTenure(1);
		tdTxn.setTdCategoryName("TD Category");
		tdTxn.setTdProductName("TD Product");
		tdTxn.setAutoRenewal(Boolean.TRUE);
		tdTxn.setIsJointOwnership(Boolean.FALSE);
		tdTxn.setIsIslamic(Boolean.TRUE);
		tdTxn.setAutoRenewal(Boolean.TRUE);
		
        when(stagedInvestRepository.getStagedInvestTxnByTokenId(Mockito.anyObject())).thenReturn(stagedInvestTxn);
		when(investRepository.getInvestTxnByTokenId(Mockito.anyObject())).thenReturn(null);
        when(userProfileRepository.getProfileByUserId(Mockito.anyObject())).thenReturn(userProfile);
		when(serverConfigRepository.getParameterValue(Mockito.anyObject())).thenReturn("value");
        when(txnRepository.getTdTxnByTokenId(Mockito.anyObject())).thenReturn(tdTxn);
		
        ResponseEntity<BoData> responseEntity =termDepositPlacementServiceImpl.termDepositPlacement(termDepositPlacementConfirmationRequest);
		TermDepositPlacementConfirmationResponse termDepositPlacementResponse = (TermDepositPlacementConfirmationResponse) responseEntity.getBody();
		assertEquals("10000", termDepositPlacementResponse.getCode());
	}
	
	@Test
	public void termDepositPlacementServiceEnquirySuccessfulTest() {
		
        ResponseEntity<BoData> responseEntity =termDepositPlacementServiceImpl.termDepositPlacement(termDepositPlacementConfirmationRequest1);
		TermDepositPlacementConfirmationResponse termDepositPlacementResponse = (TermDepositPlacementConfirmationResponse) responseEntity.getBody();
		assertEquals("10000", termDepositPlacementResponse.getCode());
	}

}
