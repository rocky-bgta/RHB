package com.rhbgroup.dcpbo.customer.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.rhbgroup.dcp.investments.bizlogic.GetUnitTrustLogic;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.creditcards.bizlogic.GetCreditCardsLogic;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.deposits.casa.bizlogic.GetCasaDepositsSummaryLogic;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaDepositsLogic;
import com.rhbgroup.dcp.loans.bizlogic.GetLoansLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.profiles.bizlogic.GetAccountProfilesLogic;
import com.rhbgroup.dcp.uber.deposits.casa.bizlogic.GetTermDepositsLogic;
import com.rhbgroup.dcp.uber.asnb.bizlogic.GetAsnbAccountInquiryLogic;
import com.rhbgroup.dcpbo.customer.aspect.ApiContext;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.dto.CustomerAccounts;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.InvestmentProfile;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.repository.CustomerRelationshipViewRepo;
import com.rhbgroup.dcpbo.customer.repository.InvestmentProfileRepository;
import com.rhbgroup.dcpbo.customer.repository.UnitTrustAccountHoldingRepository;
import com.rhbgroup.dcpbo.customer.repository.UnitTrustAccountRepository;
import com.rhbgroup.dcpbo.customer.service.CustomerAccountsService;
import com.rhbgroup.dcpbo.customer.model.AppConfig;
import com.rhbgroup.dcpbo.customer.repository.AppConfigRepository;
import com.rhbgroup.dcpbo.customer.utils.CustomerServiceConstant;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CustomerAccountsServiceImpl.class, CustomerAccountsServiceImplTests.Config.class})
public class CustomerAccountsServiceImplTests {
	@Autowired
	CustomerAccountsService customerAccountsService;

	@MockBean
	ProfileRepository profileRepositoryMock;
	
	@MockBean
	AppConfigRepository appConfigRepositoryMock;

	@MockBean
	InvestmentProfileRepository ipRepoMock;
	
	@MockBean
	CustomerRelationshipViewRepo crViewRepoMock;
	
	@MockBean
	UnitTrustAccountRepository utAccountRepoMock;
	
	@MockBean
	UnitTrustAccountHoldingRepository utAccountHoldingRepoMock;
	
	@MockBean
	GetAccountProfilesLogic getAccountProfilesLogicMock;
	
	@MockBean
	GetCasaDepositsSummaryLogic getCasaDepositsSummaryLogicMock;
	
	@MockBean
	GetCreditCardsLogic getCreditCardsLogicMock;
	
	@MockBean
	GetTermDepositsLogic getTermDepositsLogicMock;
	
	@MockBean
	GetLoansLogic getLoansLogicMock;

	@MockBean
	ConfigErrorInterface configErrorInterfaceMock;
	
	@MockBean(name = "getMcaDepositsLogic")
	BusinessAdaptor getMcaDepositsLogicMock;

	@MockBean
	GetUnitTrustLogic getUnitTrustLogic;
	
	@MockBean
	GetAsnbAccountInquiryLogic getAsnbAccountInquiryLogic;
	
	private static Logger logger = LogManager.getLogger(CustomerAccountsServiceImplTests.class);
	
	static class Config {
		@Bean
		@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
		ApiContext apiState() {
			return new ApiContext();
		}
		
		@Bean
		public GetAccountProfilesLogic getGetAccountProfilesLogic() {
			return new GetAccountProfilesLogic();
		}

		@Bean
		public GetCasaDepositsSummaryLogic getGetCasaDepositsSummaryLogic() {
			return new GetCasaDepositsSummaryLogic();
		}

		@Bean
		public GetCreditCardsLogic getGetCreditCardsLogic() {
			return new GetCreditCardsLogic();
		}

		@Bean
		public GetTermDepositsLogic getGetTermDepositsLogic() {
			return new GetTermDepositsLogic();
		}

		@Bean
		public GetLoansLogic getGetLoansLogic() {
			return new GetLoansLogic();
		}
		
		@Bean
		public BusinessAdaptor getMcaDepositsLogicMock() {
			return new GetMcaDepositsLogic();
		}
		
		@Bean
		public GetAsnbAccountInquiryLogic getAsnbAccountInquiryLogic() {
			return new GetAsnbAccountInquiryLogic();
		}
	}

	@Test
	public void getCustomerAccountsTest() throws Throwable {
		logger.debug("getCustomerAccountsTest()");
		logger.debug("    customerAccountsService: " + customerAccountsService);
		
		int customerId = 1;
		
        String cisNo = "1234567890";
        String username = "ikhwan";

        UserProfile userProfile = new UserProfile();
        userProfile.setId(customerId);
        userProfile.setCisNo(cisNo);
        userProfile.setUsername(username);
        when(profileRepositoryMock.getUserProfileByUserId(Mockito.anyInt())).thenReturn(userProfile);
		
        Capsule capsule = createCapsule("GetAccountProfilesLogic.json");
		when(getAccountProfilesLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

		capsule = createCapsule("GetCasaDepositsSummaryLogic.json");
		when(getCasaDepositsSummaryLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
        capsule = createCapsule("GetCreditCardsLogic.json");
		when(getCreditCardsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
        capsule = createCapsule("GetTermDepositsLogic.json");
		when(getTermDepositsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
        capsule = createCapsule("GetLoansLogic.json");
		when(getLoansLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		capsule = createCapsule("GetMcaDepositsLogic.json");
		when(getMcaDepositsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

		capsule = createCapsule("GetUnitTrustLogic.json");
		when(getUnitTrustLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

		capsule = createCapsule("GetASNBLogic.json");
		when(getAsnbAccountInquiryLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		CustomerAccounts customerAccounts = (CustomerAccounts) customerAccountsService.getCustomerAccounts(customerId);
		logger.debug("    customerAccounts: " + customerAccounts);
		assertNotNull(customerAccounts);
	}
	
	private Capsule createCapsule(String filename) throws IOException {
		logger.debug("Loading file: " + filename + " ...");

		InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sbld = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null)
			sbld.append(line);
		br.close();
		is.close();
		
        String jsonStr = sbld.toString();
		logger.debug("    jsonStr: " + jsonStr);
		
		Capsule capsule = new Capsule();
		capsule.updateCurrentMessage(jsonStr);
		capsule.setOperationSuccess(true);
		return capsule;
	}

	@Test(expected = CommonException.class)
	public void getCustomerAccountsTest_notFound() throws Exception {
		logger.debug("getCustomerAccountsTest_notFound()");

		BoExceptionResponse exceptionResponse = new BoExceptionResponse(CommonException.GENERIC_ERROR_CODE, "Cannot find customer account");
		when(configErrorInterfaceMock.getConfigError(Mockito.anyString())).thenReturn(exceptionResponse);
		
		when(profileRepositoryMock.getUserProfileByUserId(Mockito.anyInt())).thenReturn(null);

		int customerId = 1;

		customerAccountsService.getCustomerAccounts(customerId);
	}
	
	@Test
	public void getCustomerAccountsPrepaidCardTest() throws Throwable {
		logger.debug("getCustomerAccountsPrepaidCardTest()");
		logger.debug("    customerAccountsService: " + customerAccountsService);
		
		int customerId = 1;
		
        String cisNo = "1234567890";
        String username = "ikhwan";

        UserProfile userProfile = new UserProfile();
        userProfile.setId(customerId);
        userProfile.setCisNo(cisNo);
        userProfile.setUsername(username);
        when(profileRepositoryMock.getUserProfileByUserId(Mockito.anyInt())).thenReturn(userProfile);
		
        Capsule capsule = createCapsule("GetAccountProfilesLogic.json");
		when(getAccountProfilesLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

		capsule = createCapsule("GetCasaDepositsSummaryLogic.json");
		when(getCasaDepositsSummaryLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
        capsule = createCapsule("GetPrepaidCardsLogic.json");
		when(getCreditCardsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
        capsule = createCapsule("GetTermDepositsLogic.json");
		when(getTermDepositsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
        capsule = createCapsule("GetLoansLogic.json");
		when(getLoansLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		capsule = createCapsule("GetMcaDepositsLogic.json");
		when(getMcaDepositsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

		capsule = createCapsule("GetUnitTrustLogic.json");
		when(getUnitTrustLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		CustomerAccounts customerAccounts = (CustomerAccounts) customerAccountsService.getCustomerAccounts(customerId);
		logger.debug("    customerAccounts: " + customerAccounts);
		assertNotNull(customerAccounts);
	}
	
	@Test
	public void getCustomerAccountsDebitCardTest() throws Throwable {
		logger.debug("getCustomerAccountsDebitCardTest()");
		logger.debug("    customerAccountsService: " + customerAccountsService);
		
		int customerId = 1;
		
        String cisNo = "1234567890";
        String username = "ikhwan";

        UserProfile userProfile = new UserProfile();
        userProfile.setId(customerId);
        userProfile.setCisNo(cisNo);
        userProfile.setUsername(username);
        when(profileRepositoryMock.getUserProfileByUserId(Mockito.anyInt())).thenReturn(userProfile);
		
        Capsule capsule = createCapsule("GetAccountProfilesLogic.json");
		when(getAccountProfilesLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

		capsule = createCapsule("GetCasaDepositsSummaryLogic.json");
		when(getCasaDepositsSummaryLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
        capsule = createCapsule("GetDebitCardsLogic.json");
		when(getCreditCardsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
        capsule = createCapsule("GetTermDepositsLogic.json");
		when(getTermDepositsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
        capsule = createCapsule("GetLoansLogic.json");
		when(getLoansLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		capsule = createCapsule("GetMcaDepositsLogic.json");
		when(getMcaDepositsLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

		capsule = createCapsule("GetUnitTrustLogic.json");
		when(getUnitTrustLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		CustomerAccounts customerAccounts = (CustomerAccounts) customerAccountsService.getCustomerAccounts(customerId);
		logger.debug("    customerAccounts: " + customerAccounts);
		assertNotNull(customerAccounts);
	}
	
}