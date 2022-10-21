package com.rhbgroup.dcpbo.system.downtime.whitelist.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.ConfigFunctionRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.dto.ApprovalDowntimeAdhocWhitelistPayload;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.BoSmApprovalDowntimeWhitelistRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.DowntimeWhitelistRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.SystemDowntimeWhitelistConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.impl.DowntimeAdhocWhitelistServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.AddDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.DowntimeAdhocWhitelistResponse;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntimeWhitelist;
import com.rhbgroup.dcpbo.system.model.ConfigFunction;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeWhitelistConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DowntimeAdhocWhitelistService.class, DowntimeAdhocWhitelistServiceTest.Config.class })
public class DowntimeAdhocWhitelistServiceTest {

	@Autowired
	DowntimeAdhocWhitelistService downtimeAdhocWhitelistService;

	@MockBean
	private ConfigFunctionRepository configFunctionRepositoryMock;

	@MockBean
	private SystemDowntimeWhitelistConfigRepository systemDowntimeWhitelistConfigRepositoryMock;

	@MockBean
	private ApprovalRepository approvalRepositoryMock;

	@MockBean
	private UserRepository userRepositoryMock;

	@MockBean
	private BoSmApprovalDowntimeWhitelistRepository boSmApprovalDowntimeWhitelistRepositoryMock;

	@MockBean
	private ProfileRepository profileRepositoryMock;

	@MockBean
	AdditionalDataHolder additionalDataHolder;

	@MockBean
	DowntimeWhitelistRepository downtimeWhitelistRepositoryMock;
	
	private AddDowntimeAdhocWhitelistRequest request;

	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public DowntimeAdhocWhitelistService getDowntimeAdhocWhitelistService() {
			return new DowntimeAdhocWhitelistServiceImpl();
		}

		@Bean
		@Primary
		public AdditionalDataHolder getAdditionalDataHolder() {
			return new AdditionalDataHolder();
		}
	}

	@Before
	public void setup() {

		request = new AddDowntimeAdhocWhitelistRequest();
		request.setFunctionId(16);
		request.setUserId(12345);

	}

	@Test
	public void testAddDowntimeAdhocWhitelistRequireApprovalSuccessTest() {
		Integer userId = 1;
		String userName = "James";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);

		ApprovalDowntimeAdhocWhitelistPayload coverage = new ApprovalDowntimeAdhocWhitelistPayload();
		coverage.toString(); //just for code coverage
		
		UserProfile userProfile = new UserProfile();
		userProfile.setId(23123);
		userProfile.setName("Ali");
		userProfile.setUsername("alibaba1688");
		userProfile.setMobileNo("60134850344");
		userProfile.setIdNo("850203058212");
		userProfile.setIdType("MK");
		userProfile.setCisNo("029343242340");
		
		when(profileRepositoryMock.getUserProfile(Mockito.anyInt())).thenReturn(userProfile);
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(approvalRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject())).thenReturn(approvalIdLs);
		when(boSmApprovalDowntimeWhitelistRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject())).thenReturn(1);

		ResponseEntity<BoData> response = downtimeAdhocWhitelistService.addDowntimeAdhocWhitelist(request, userId);
		DowntimeAdhocWhitelistResponse downtimeAdhocWhitelistResponse = (DowntimeAdhocWhitelistResponse) response
				.getBody();
		assertEquals(approvalId, downtimeAdhocWhitelistResponse.getApprovalId());
	}

	@Test
	public void testAddDowntimeAdhocWhitelistNotRequireApprovalSuccessTest() {
		Integer userId = 1;
		String userName = "James";

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);

		UserProfile userProfile = new UserProfile();
		userProfile.setId(23123);
		userProfile.setName("Ali");
		userProfile.setUsername("alibaba1688");
		userProfile.setMobileNo("60134850344");
		userProfile.setIdNo("850203058212");
		userProfile.setIdType("MK");
		userProfile.setCisNo("029343242340");
		
		when(profileRepositoryMock.getUserProfile(Mockito.anyInt())).thenReturn(userProfile);
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(systemDowntimeWhitelistConfigRepositoryMock.saveAndFlush(Mockito.anyObject()))
				.thenReturn(Mockito.anyObject());
		ResponseEntity<BoData> response = downtimeAdhocWhitelistService.addDowntimeAdhocWhitelist(request, userId);
		DowntimeAdhocWhitelistResponse downtimeAdhocWhitelistResponse = (DowntimeAdhocWhitelistResponse) response
				.getBody();
		assertEquals(0, downtimeAdhocWhitelistResponse.getApprovalId());
	}

	@Test(expected = CommonException.class)
	public void testAddDowntimeAdhocWhitelistNotRequireApprovalInvalidUserIdFailTest() {
		Integer userId = 1;

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);

		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		downtimeAdhocWhitelistService.addDowntimeAdhocWhitelist(request, userId);
	}

	@Test(expected = CommonException.class)
	public void testAddDowntimeAdhocWhitelistExistsFailTest() {
		Integer userId = 1;
		String userName = "James";

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);

		List<SystemDowntimeWhitelistConfig> systemDowntimeWhitelistConfigList = new ArrayList<>();
		SystemDowntimeWhitelistConfig systemDowntimeWhitelistConfig = new SystemDowntimeWhitelistConfig();
		systemDowntimeWhitelistConfig.setId(1);
		systemDowntimeWhitelistConfig.setType("ADHOC");
		systemDowntimeWhitelistConfig.setUserId(123);
		systemDowntimeWhitelistConfig.setCreatedBy("test");
		systemDowntimeWhitelistConfig.setUpdatedBy("test");
		systemDowntimeWhitelistConfig.setCreatedTime(new Timestamp(System.currentTimeMillis()));
		systemDowntimeWhitelistConfig.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
		systemDowntimeWhitelistConfigList.add(systemDowntimeWhitelistConfig);

		when(systemDowntimeWhitelistConfigRepositoryMock.findByUserIdAndType(Mockito.anyInt(), Mockito.anyObject()))
				.thenReturn(systemDowntimeWhitelistConfigList);
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);

		downtimeAdhocWhitelistService.addDowntimeAdhocWhitelist(request, userId);
	}

	@Test(expected = com.rhbgroup.dcpbo.system.exception.PendingApprovalException.class)
	public void testAddDowntimeAdhocWhitelistFoundDuplicatePendingApprovalFailTest() {
		Integer userId = 1;
		String userName = "James";

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);

		List<Integer> approvalIds = new ArrayList<>();
		approvalIds.add(2);
		approvalIds.add(3);

		List<BoSmApprovalDowntimeWhitelist> duplicatePendingApprovalList = new ArrayList<>();
		BoSmApprovalDowntimeWhitelist boSmApprovalDowntimeWhitelist = new BoSmApprovalDowntimeWhitelist();
		boSmApprovalDowntimeWhitelist.setId(323);
		boSmApprovalDowntimeWhitelist.setApprovalId(44);
		duplicatePendingApprovalList.add(boSmApprovalDowntimeWhitelist);
		boSmApprovalDowntimeWhitelist = new BoSmApprovalDowntimeWhitelist();
		boSmApprovalDowntimeWhitelist.setApprovalId(55);
		duplicatePendingApprovalList.add(boSmApprovalDowntimeWhitelist);

		UserProfile userProfile = new UserProfile();
		userProfile.setId(23123);
		userProfile.setName("Ali");
		userProfile.setUsername("alibaba1688");
		userProfile.setMobileNo("60134850344");
		userProfile.setIdNo("850203058212");
		userProfile.setIdType("MK");
		userProfile.setCisNo("029343242340");
		
		when(profileRepositoryMock.getUserProfile(Mockito.anyInt())).thenReturn(userProfile);
		when(boSmApprovalDowntimeWhitelistRepositoryMock.findByApprovalIdAndLockingId(Mockito.anyObject(),
				Mockito.anyString())).thenReturn(duplicatePendingApprovalList);
		when(approvalRepositoryMock.findIdByFunctionIdAndStatus(Mockito.anyInt(), Mockito.anyString()))
				.thenReturn(approvalIds);
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);

		downtimeAdhocWhitelistService.addDowntimeAdhocWhitelist(request, userId);
	}

	@Test(expected = CommonException.class)
	public void testAddDowntimeAdhocWhitelistFoundPendingButNotDuplicateFailTest() {
		Integer userId = 1;
		String userName = "James";

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);

		List<Integer> approvalIds = new ArrayList<>();
		approvalIds.add(2);
		approvalIds.add(3);

		when(approvalRepositoryMock.findIdByFunctionIdAndStatus(Mockito.anyInt(), Mockito.anyString()))
				.thenReturn(approvalIds);
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);

		downtimeAdhocWhitelistService.addDowntimeAdhocWhitelist(request, userId);
	}

}
