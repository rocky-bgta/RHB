package com.rhbgroup.dcpbo.customer.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.enums.FundTransferMainFunctionType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.entity.transfers.NADStatistic;
import com.rhbgroup.dcp.data.repository.CommonRepository;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.data.repository.TransferRepository;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.DuitnowDetail;
import com.rhbgroup.dcpbo.customer.service.impl.DuitnowEnquiryServiceImpl;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DuitnowEnquiryServiceImpl.class, DuitnowEnquiryServiceTests.Config.class, AdditionalDataHolder.class})
public class DuitnowEnquiryServiceTests {

	private static Logger LOGGER = LogManager.getLogger();
	
	//private static final String CONST_DCP_DUITNOW_MAIN_FUNCTION = "DUITNOW";
	private static final String CONST_DCP_DUITNOW_NAD_MAX_ABUSE_COUNTER = "nad.max.abuse.counter";

	@Autowired
	DuitnowEnquiryService duitnowEnquiryService;

	@MockBean(name = "duitnowSendersLogic")
	BusinessAdaptor getDuitNowSendersLogicMock;

	@MockBean(name = "duitnowEnquiryLogic")
	BusinessAdaptor getDuitnowEnquiryLogic;

	@MockBean(name = "profileRepository")
	private ProfileRepository profileRepository;
	
	@MockBean(name = "transferRepository")
	private TransferRepository transferRepository;
	
	@MockBean(name = "commonRepository")
	private CommonRepository commonRepository;

	@MockBean
    RestTemplate restTemplate;

	@TestConfiguration
	static class Config {

		@Bean
		@Primary
		public DuitnowEnquiryService getDuitnowEnquiryService(
				@Qualifier("duitnowSendersLogic") BusinessAdaptor getDuitNowSendersLogic,
				@Qualifier("duitnowEnquiryLogic") BusinessAdaptor getDuitnowEnquiryLogic) {
			return new DuitnowEnquiryServiceImpl(getDuitNowSendersLogic, getDuitnowEnquiryLogic);
		}

	}

	public void getDuitnowEnquiryTest(boolean capsuleSuccessful, String nadMaxAbuseCounter, 
			int nadLookupCount, int transferCompletionCount, int addFavCompletionCount) throws Exception {
		LOGGER.debug("duitnowEnquiryService: {}", duitnowEnquiryService);
		LOGGER.debug("getDuitNowSendersLogicMock: {}", getDuitNowSendersLogicMock);
		LOGGER.debug("getDuitnowEnquiryLogic: {}", getDuitnowEnquiryLogic);
		Integer userId = 1;

		when(restTemplate.getForObject( any(), any())).thenReturn("123");

		Capsule capsule = null;
		
		NADStatistic nadStatistic = new NADStatistic();
		nadStatistic.setId(1);
		nadStatistic.setUserId(1);
		nadStatistic.setNadLookupCount(nadLookupCount);
		nadStatistic.setTransferCompletionCount(transferCompletionCount);
		nadStatistic.setAddFavCompletionCount(addFavCompletionCount);
		nadStatistic.setMainFunction(FundTransferMainFunctionType.DUITNOW.toString());
		
		int intNadMaxAbuseCounter = Integer.parseInt(nadMaxAbuseCounter);
		if(intNadMaxAbuseCounter > 0) {
			// If (nad_lookup_count - transfer_completion_count - add_fav_completion_count) >= nad.max.abuse.counter, return "Disabled" else return "Enable"
			if(nadStatistic.getNadLookupCount() - nadStatistic.getTransferCompletionCount() - nadStatistic.getAddFavCompletionCount() >= Integer.parseInt(nadMaxAbuseCounter)) {
				capsule = getDCPLogicResponse(capsuleSuccessful, "GetDuitnowSendersLogic.json");
			} else {
				capsule = getDCPLogicResponse(capsuleSuccessful, "GetDuitnowSendersLogicEnableNad.json");
			}
		} else if (intNadMaxAbuseCounter == 0) {
				capsule = getDCPLogicResponse(capsuleSuccessful, "GetDuitnowSendersLogic.json");
		} else {
			// nadMaxAbuseCounter < 0
			capsule = getDCPLogicResponse(capsuleSuccessful, "GetDuitnowSendersLogicEnableNad.json");
		}

		when(getDuitNowSendersLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		// Mock TransferRepository
		when(transferRepository.getTodayNADStatisticByUserId(userId, FundTransferMainFunctionType.DUITNOW.toString())).thenReturn(nadStatistic);
		
		// Mock CommonRepository
		when(commonRepository.getAppConfigLookup(CONST_DCP_DUITNOW_NAD_MAX_ABUSE_COUNTER)).thenReturn(nadMaxAbuseCounter);

		Capsule capsule2 = getDCPLogicResponse(capsuleSuccessful, "GetDuitnowEnquiryLogic.json");

		when(getDuitnowEnquiryLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule2);

		UserProfile userProfile = new UserProfile();
		userProfile.setId(1);
		userProfile.setCisNo("1234");
		BDDMockito.given(profileRepository.getUserProfileByUserId(1)).willReturn(userProfile);

		DuitnowDetail duitnowDetail = (DuitnowDetail) duitnowEnquiryService.getDuitnowDetails(userId);
		LOGGER.debug("duitnowDetail: {}", JsonUtil.objectToJson(duitnowDetail));
	}

	private Capsule getDCPLogicResponse(boolean capsuleSuccessful, String resource) throws IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream(resource);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sbld = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null)
			sbld.append(line);
		br.close();
		is.close();

		String jsonStr = sbld.toString();

		Capsule capsule = new Capsule();
		capsule.updateCurrentMessage(jsonStr);
		capsule.setOperationSuccess(capsuleSuccessful);
		return capsule;
	}

	@Test
	public void getDuitnowEnquiryTest_capsuleSuccessful() throws Exception {
		getDuitnowEnquiryTest(true, "0", 0, 0, 0);
	}
	
	@Test
	public void getDuitnowEnquiryTestNadMaxAbuseCounterNegative_capsuleSuccessful() throws Exception {
		getDuitnowEnquiryTest(true, "-1", 0, 0, 0);
	}
	
	@Test
	public void getDuitnowEnquiryTestNadMaxAbuseCounterPositiveDisable_capsuleSuccessful() throws Exception {
		getDuitnowEnquiryTest(true, "5", 10, 1, 3);
	}
	
	@Test
	public void getDuitnowEnquiryTestNadMaxAbuseCounterPositiveEnable_capsuleSuccessful() throws Exception {
		getDuitnowEnquiryTest(true, "5", 10, 1, 7);
	}

	@Test(expected = CommonException.class)
	public void getDuitnowEnquiryTest_capsuleUnsuccessful() throws Exception {
		getDuitnowEnquiryTest(false, "0", 0, 0, 0);
	}

	@Test(expected = CommonException.class)
	public void getDuitnowIEnquiryTest_capsuleUnsuccessful2() throws Exception {
		LOGGER.debug("duitnowEnquiryService: {}", duitnowEnquiryService);
		LOGGER.debug("getDuitNowSendersLogicMock: {}", getDuitNowSendersLogicMock);
		LOGGER.debug("getDuitnowEnquiryLogic: {}", getDuitnowEnquiryLogic);
		Integer userId = 1;

		Capsule capsule = getDCPLogicResponse(true, "GetDuitnowSendersLogic.json");

		when(getDuitNowSendersLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		NADStatistic nadStatistic = new NADStatistic();
		nadStatistic.setId(1);
		nadStatistic.setUserId(1);
		nadStatistic.setNadLookupCount(0);
		nadStatistic.setTransferCompletionCount(0);
		nadStatistic.setAddFavCompletionCount(0);
		nadStatistic.setMainFunction(FundTransferMainFunctionType.DUITNOW.toString());
		
		// Mock TransferRepository
		when(transferRepository.getTodayNADStatisticByUserId(userId, FundTransferMainFunctionType.DUITNOW.toString())).thenReturn(nadStatistic);
		
		// Mock CommonRepository
		when(commonRepository.getAppConfigLookup(CONST_DCP_DUITNOW_NAD_MAX_ABUSE_COUNTER)).thenReturn("0");		

		Capsule capsule2 = getDCPLogicResponse(false, "GetDuitnowEnquiryLogic.json");

		when(getDuitnowEnquiryLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule2);

		UserProfile userProfile = new UserProfile();
		userProfile.setId(1);
		userProfile.setCisNo("1234");
		BDDMockito.given(profileRepository.getUserProfileByUserId(1)).willReturn(userProfile);

		DuitnowDetail duitnowDetail = (DuitnowDetail) duitnowEnquiryService.getDuitnowDetails(userId);
		LOGGER.debug("duitnowDetail: {}", JsonUtil.objectToJson(duitnowDetail));
	}
	
	public void getDuitnowEnquiryNADEmptyTest(boolean capsuleSuccessful, String nadMaxAbuseCounter, 
			int nadLookupCount, int transferCompletionCount, int addFavCompletionCount) throws Exception {
		
	}
	
	@Test
	public void getDuitnowEnquiryNADEmptyTest_capsuleSuccessful() throws Exception {
		LOGGER.debug("duitnowEnquiryService: {}", duitnowEnquiryService);
		LOGGER.debug("getDuitNowSendersLogicMock: {}", getDuitNowSendersLogicMock);
		LOGGER.debug("getDuitnowEnquiryLogic: {}", getDuitnowEnquiryLogic);
		
		boolean capsuleSuccessful = true;
		String nadMaxAbuseCounter = "5";
		
		Integer userId = 1;

		when(restTemplate.getForObject( any(), any())).thenReturn("123");

		Capsule capsule = null;
		
		NADStatistic nadStatistic = null;
		
		capsule = getDCPLogicResponse(capsuleSuccessful, "GetDuitnowSendersLogicEnableNad.json");

		when(getDuitNowSendersLogicMock.executeBusinessLogic(Mockito.any())).thenReturn(capsule);
		
		// Mock TransferRepository
		when(transferRepository.getTodayNADStatisticByUserId(userId, FundTransferMainFunctionType.DUITNOW.toString())).thenReturn(nadStatistic);
		
		// Mock CommonRepository
		when(commonRepository.getAppConfigLookup(CONST_DCP_DUITNOW_NAD_MAX_ABUSE_COUNTER)).thenReturn(nadMaxAbuseCounter);

		Capsule capsule2 = getDCPLogicResponse(capsuleSuccessful, "GetDuitnowEnquiryLogic.json");

		when(getDuitnowEnquiryLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule2);

		UserProfile userProfile = new UserProfile();
		userProfile.setId(1);
		userProfile.setCisNo("1234");
		BDDMockito.given(profileRepository.getUserProfileByUserId(1)).willReturn(userProfile);

		DuitnowDetail duitnowDetail = (DuitnowDetail) duitnowEnquiryService.getDuitnowDetails(userId);
		LOGGER.debug("duitnowDetail: {}", JsonUtil.objectToJson(duitnowDetail));
	}

}
