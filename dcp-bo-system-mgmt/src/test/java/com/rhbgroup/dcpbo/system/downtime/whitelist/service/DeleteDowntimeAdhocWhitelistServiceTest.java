package com.rhbgroup.dcpbo.system.downtime.whitelist.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.BoSmApprovalDowntimeWhitelistRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.DowntimeWhitelistRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.SystemDowntimeWhitelistConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.impl.DowntimeAdhocWhitelistServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.DeleteDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.DowntimeAdhocWhitelistResponse;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntimeWhitelist;
import com.rhbgroup.dcpbo.system.model.ConfigFunction;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeWhitelistConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DowntimeAdhocWhitelistService.class, DeleteDowntimeAdhocWhitelistServiceTest.Config.class })
public class DeleteDowntimeAdhocWhitelistServiceTest {

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
        private DowntimeWhitelistRepository whitelistRepo;

	private DeleteDowntimeAdhocWhitelistRequest request;

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

		request = new DeleteDowntimeAdhocWhitelistRequest();
		request.setFunctionId(16);

	}

	@Test
	public void testDeleteDowntimeAdhocWhitelistRequireApprovalSuccessTest() {
		Integer userId = 1;
		Integer id = 1;
		String userName = "James";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);

		SystemDowntimeWhitelistConfig tobeDeleted = new SystemDowntimeWhitelistConfig();
		tobeDeleted.setId(5);
		tobeDeleted.setUserId(6);

		UserProfile userProfile = new UserProfile();
		userProfile.setId(23123);
		userProfile.setName("Ali");
		userProfile.setUsername("alibaba1688");
		userProfile.setMobileNo("60134850344");
		userProfile.setIdNo("850203058212");
		userProfile.setIdType("MK");
		userProfile.setCisNo("029343242340");

		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(systemDowntimeWhitelistConfigRepositoryMock.findOne(Mockito.anyInt())).thenReturn(tobeDeleted);
		when(profileRepositoryMock.getUserProfile(Mockito.anyInt())).thenReturn(userProfile);
		when(approvalRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject())).thenReturn(approvalIdLs);
		when(boSmApprovalDowntimeWhitelistRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject())).thenReturn(1);

		ResponseEntity<BoData> response = downtimeAdhocWhitelistService.deleteDowntimeAdhocWhitelist(request, userId,
				id);
		DowntimeAdhocWhitelistResponse downtimeAdhocWhitelistResponse = (DowntimeAdhocWhitelistResponse) response
				.getBody();
		assertEquals(approvalId, downtimeAdhocWhitelistResponse.getApprovalId());
	}

	@Test
	public void testDeleteDowntimeAdhocWhitelistNotRequireApprovalSuccessTest() {
		Integer userId = 1;
		Integer id = 1;
		String userName = "James";

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);

		SystemDowntimeWhitelistConfig tobeDeleted = new SystemDowntimeWhitelistConfig();
		tobeDeleted.setId(5);
		tobeDeleted.setUserId(6);

		UserProfile userProfile = new UserProfile();
		userProfile.setId(23123);
		userProfile.setName("Ali");
		userProfile.setUsername("alibaba1688");
		userProfile.setMobileNo("60134850344");
		userProfile.setIdNo("850203058212");
		userProfile.setIdType("MK");
		userProfile.setCisNo("029343242340");

		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(systemDowntimeWhitelistConfigRepositoryMock.findOne(Mockito.anyInt())).thenReturn(tobeDeleted);
		when(profileRepositoryMock.getUserProfile(Mockito.anyInt())).thenReturn(userProfile);
		when(boSmApprovalDowntimeWhitelistRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject())).thenReturn(1);

		ResponseEntity<BoData> response = downtimeAdhocWhitelistService.deleteDowntimeAdhocWhitelist(request, userId,
				id);
		DowntimeAdhocWhitelistResponse downtimeAdhocWhitelistResponse = (DowntimeAdhocWhitelistResponse) response
				.getBody();
		assertEquals(0, downtimeAdhocWhitelistResponse.getApprovalId());
	}

	@Test(expected = CommonException.class)
	public void testDeleteDowntimeAdhocWhitelistRequireApprovalWriteFailTest() {
		Integer userId = 1;
		Integer id = 1;
		String userName = "James";

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);

		SystemDowntimeWhitelistConfig tobeDeleted = new SystemDowntimeWhitelistConfig();
		tobeDeleted.setId(5);
		tobeDeleted.setUserId(6);

		UserProfile userProfile = new UserProfile();
		userProfile.setId(23123);
		userProfile.setName("Ali");
		userProfile.setUsername("alibaba1688");
		userProfile.setMobileNo("60134850344");
		userProfile.setIdNo("850203058212");
		userProfile.setIdType("MK");
		userProfile.setCisNo("029343242340");

		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(systemDowntimeWhitelistConfigRepositoryMock.findOne(Mockito.anyInt())).thenReturn(tobeDeleted);
		when(profileRepositoryMock.getUserProfile(Mockito.anyInt())).thenReturn(userProfile);
		when(approvalRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject())).thenReturn(new ArrayList<Integer>());
		when(boSmApprovalDowntimeWhitelistRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject())).thenReturn(1);

		downtimeAdhocWhitelistService.deleteDowntimeAdhocWhitelist(request, userId, id);
	}

	@Test(expected = CommonException.class)
	public void testDeleteDowntimeAdhocWhitelistNotRequireApprovalInvalidUserIdFailTest() {
		Integer userId = 1;
		Integer id = 1;
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);

		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);

		downtimeAdhocWhitelistService.deleteDowntimeAdhocWhitelist(request, userId, id);
	}

	@Test(expected = CommonException.class)
	public void testDeleteDowntimeAdhocWhitelistNotRequireApprovalWhitelistNotFoundFailTest() {
		Integer userId = 1;
		Integer id = 1;
		String userName = "James";
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);

		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(systemDowntimeWhitelistConfigRepositoryMock.findOne(Mockito.anyInt())).thenReturn(null);
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);

		downtimeAdhocWhitelistService.deleteDowntimeAdhocWhitelist(request, userId, id);
	}

	@Test(expected = CommonException.class)
	public void testDeleteDowntimeAdhocWhitelistNotRequireApprovalUserProfileNotFoundFailTest() {
		Integer userId = 1;
		Integer id = 1;
		String userName = "James";
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);

		SystemDowntimeWhitelistConfig tobeDeleted = new SystemDowntimeWhitelistConfig();

		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(systemDowntimeWhitelistConfigRepositoryMock.findOne(Mockito.anyInt())).thenReturn(tobeDeleted);
		when(profileRepositoryMock.getUserProfile(Mockito.anyInt())).thenReturn(null);
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		downtimeAdhocWhitelistService.deleteDowntimeAdhocWhitelist(request, userId, id);
	}

	@Test(expected = com.rhbgroup.dcpbo.system.exception.PendingApprovalException.class)
	public void testDeleteDowntimeAdhocWhitelistFoundDuplicatePendingApprovalFailTest() {
		Integer userId = 1;
		Integer id = 1;
		String userName = "James";

		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);

		SystemDowntimeWhitelistConfig tobeDeleted = new SystemDowntimeWhitelistConfig();
		tobeDeleted.setId(5);
		tobeDeleted.setUserId(6);

		UserProfile userProfile = new UserProfile();
		userProfile.setId(23123);
		userProfile.setName("Ali");
		userProfile.setUsername("alibaba1688");
		userProfile.setMobileNo("60134850344");
		userProfile.setIdNo("850203058212");
		userProfile.setIdType("MK");
		userProfile.setCisNo("029343242340");

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

		when(boSmApprovalDowntimeWhitelistRepositoryMock.findByApprovalIdAndLockingId(Mockito.anyObject(),
				Mockito.anyString())).thenReturn(duplicatePendingApprovalList);
		when(profileRepositoryMock.getUserProfile(Mockito.anyInt())).thenReturn(userProfile);
		when(approvalRepositoryMock.findIdByFunctionIdAndStatus(Mockito.anyInt(), Mockito.anyString()))
				.thenReturn(approvalIds);
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(systemDowntimeWhitelistConfigRepositoryMock.findOne(Mockito.anyInt())).thenReturn(tobeDeleted);
		when(approvalRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject())).thenReturn(approvalIdLs);
		when(boSmApprovalDowntimeWhitelistRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject())).thenReturn(1);
		downtimeAdhocWhitelistService.deleteDowntimeAdhocWhitelist(request, userId, id);
	}

	@Test
	public void testDeleteDowntimeAdhocWhitelistNoDuplicatePendingApprovalFailTest() {
		Integer userId = 1;
		Integer id = 1;
		String userName = "James";

		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);

		SystemDowntimeWhitelistConfig tobeDeleted = new SystemDowntimeWhitelistConfig();
		tobeDeleted.setId(5);
		tobeDeleted.setUserId(6);

		UserProfile userProfile = new UserProfile();
		userProfile.setId(23123);
		userProfile.setName("Ali");
		userProfile.setUsername("alibaba1688");
		userProfile.setMobileNo("60134850344");
		userProfile.setIdNo("850203058212");
		userProfile.setIdType("MK");
		userProfile.setCisNo("029343242340");

		List<Integer> approvalIds = new ArrayList<>();
		approvalIds.add(2);
		approvalIds.add(3);

		when(boSmApprovalDowntimeWhitelistRepositoryMock.findByApprovalIdAndLockingId(Mockito.anyObject(),
				Mockito.anyString())).thenReturn(new ArrayList<>());
		when(profileRepositoryMock.getUserProfile(Mockito.anyInt())).thenReturn(userProfile);
		when(approvalRepositoryMock.findIdByFunctionIdAndStatus(Mockito.anyInt(), Mockito.anyString()))
				.thenReturn(approvalIds);
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(systemDowntimeWhitelistConfigRepositoryMock.findOne(Mockito.anyInt())).thenReturn(tobeDeleted);
		when(approvalRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject())).thenReturn(approvalIdLs);
		when(boSmApprovalDowntimeWhitelistRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject())).thenReturn(1);
		downtimeAdhocWhitelistService.deleteDowntimeAdhocWhitelist(request, userId, id);
	}

	/*
	 * 
	 * 
	 * 
	 * @Test(expected = CommonException.class) public void
	 * testDeleteDowntimeAdhocWhitelistFoundPendingButNotDuplicateFailTest() {
	 * Integer userId = 1; String userName = "James";
	 * 
	 * ConfigFunction configFunction = new ConfigFunction();
	 * configFunction.setApprovalRequired(true);
	 * 
	 * List<Integer> approvalIds = new ArrayList<>(); approvalIds.add(2);
	 * approvalIds.add(3);
	 * 
	 * when(approvalRepositoryMock.findIdByFunctionIdAndStatus(Mockito.anyInt(),
	 * Mockito.anyString())) .thenReturn(approvalIds);
	 * when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(
	 * userName);
	 * when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(
	 * configFunction);
	 * 
	 * downtimeAdhocWhitelistService.addDowntimeAdhocWhitelist(request, userId); }
	 */

}
