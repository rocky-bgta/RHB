package com.rhbgroup.dcpbo.customer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;

import org.springframework.test.context.web.WebAppConfiguration;

import com.rhbgroup.dcp.asnb.model.DcpAsnbTxnRequest;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.eai.adaptors.uber.reaipn05.AsnbTransaction.model.DcpAsnbTransactionDetail;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.uber.asnb.bizlogic.GetAsnbTransactionInquiryLogic;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.dto.AsnbTransactions;
import com.rhbgroup.dcpbo.customer.service.impl.AsnbTransactionsServiceImpl;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@WebAppConfiguration
@EnableAspectJAutoProxy()
@SpringBootTest(classes = { AsnbTransactionsService.class, ProfileRepository.class, AsnbTransactionsServiceTest.class})
public class AsnbTransactionsServiceTest {
	
	@Autowired
	AsnbTransactionsService asnbTransactionServiceMock;
	
	@MockBean
	ProfileRepository profileRepositoryMock;
	
	@MockBean
	private GetAsnbTransactionInquiryLogic getAsnbTransactionsInquiryLogicMock;
	
	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public AsnbTransactionsServiceImpl getasnbTransactionsService() {
			return new AsnbTransactionsServiceImpl();
		}
	}
	
	private static Logger logger = LogManager.getLogger(AsnbTransactionsServiceTest.class);
	

	@Test
	public void retrieveInvestServiceTest() {
		int customerId = 1;
		int userId = customerId;
		String cisNo = "1234567890";
		String username = "ikhwan";
		String ref1 = "test";
		
		String fundId = "AASSGD";
		String identificationNumber  = "751112111011";
		String identificationType = "W";
		String membershipNumber = "000012858169";
		String guardianIdNumber = "";
		boolean isMinor = false;
		
		UserProfile userProfile = new UserProfile();
		userProfile.setCisNo(cisNo);
		userProfile.setUsername(username);
		userProfile.setId(1);
		DcpAsnbTxnRequest asnbTransRequest = new DcpAsnbTxnRequest();
		asnbTransRequest.setFundId("1234");
		asnbTransRequest.setIdentificationNumber("12334");
		asnbTransRequest.setIdentificationType("txntype");
		asnbTransRequest.setMembershipNumber("1234");
		
		when(profileRepositoryMock.getUserProfileByUserId(userId)).thenReturn(userProfile);
		assertEquals(userProfile.getUsername(), username);
		assertEquals(userProfile.getCisNo(), cisNo);

		String jsonStr = JsonUtil.objectToJson(asnbTransRequest);
		
		System.out.println("    jsonStr: " + jsonStr);
		
		Capsule capsule = new Capsule();
		capsule.updateCurrentMessage(jsonStr);
		capsule.setOperationSuccess(true);
		
		AsnbTransactions asnbTransactions = new AsnbTransactions();
		asnbTransactions.setTotalUnits("11");
		asnbTransactions.setUnitHoldings("1");
		DcpAsnbTransactionDetail transactionDetail = new DcpAsnbTransactionDetail();
		List<DcpAsnbTransactionDetail> detailsLst = new ArrayList<>();
		transactionDetail.setConfirmedUnits("1");
		transactionDetail.setTransactionAmount("1");
		transactionDetail.setTransactionDate("09/03/2020");
		transactionDetail.setTransactionType("xyz");
		detailsLst.add(transactionDetail);
		
		asnbTransactions.setTransactionDetail(detailsLst);
		String jsonStr2 = JsonUtil.objectToJson(asnbTransactions);
		
		System.out.println("    jsonStr: " + jsonStr2);
		
		Capsule capsule2 = new Capsule();
		capsule2.updateCurrentMessage(jsonStr2);
		capsule2.setOperationSuccess(true);

		when(getAsnbTransactionsInquiryLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule2);
		
		AsnbTransactions asnbTransactions2 = (AsnbTransactions) asnbTransactionServiceMock.getAsnbTransactions(customerId, fundId, identificationNumber, identificationType, membershipNumber, isMinor, guardianIdNumber);
		logger.debug("    asnbTransactions2: " + asnbTransactions2);
		System.out.println("    asnbTransactions2: " + asnbTransactions2);
		
		AsnbTransactions dataNode = JsonUtil.jsonToObject(jsonStr2, AsnbTransactions.class);

		assertNotNull(asnbTransactions2);
		assertEquals(asnbTransactions2.getTotalUnits(), dataNode.getTotalUnits());
		assertEquals(asnbTransactions2.getUnitHoldings(), dataNode.getUnitHoldings());
		assertEquals(asnbTransactions2.getTransactionDetail().get(0).getConfirmedUnits(), dataNode.getTransactionDetail().get(0).getConfirmedUnits());
		assertNotNull(capsule);

		
	}

	
	

}
