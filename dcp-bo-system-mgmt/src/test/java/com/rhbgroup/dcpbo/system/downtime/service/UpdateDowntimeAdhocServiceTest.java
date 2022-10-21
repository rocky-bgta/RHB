package com.rhbgroup.dcpbo.system.downtime.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.DowntimeAdhoc;
import com.rhbgroup.dcpbo.system.downtime.repository.BoDowntimeAdhocTypeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BankRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoSmApprovalDowntimeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.ConfigFunctionRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.SystemDowntimeConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.service.DowntimeAdhocService;
import com.rhbgroup.dcpbo.system.downtime.service.impl.DowntimeAdhocServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.vo.UpdateDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.exception.AdhocDurationOverlappedException;
import com.rhbgroup.dcpbo.system.exception.PendingApprovalException;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntime;
import com.rhbgroup.dcpbo.system.model.ConfigFunction;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DowntimeAdhocService.class, UpdateDowntimeAdhocServiceTest.Config.class })
public class UpdateDowntimeAdhocServiceTest {
	
	private static final String ADHOC_TYPE = "ADHOC";
	
	private static final String IS_ACTIVE_1 = "1";

	@Autowired
	DowntimeAdhocService downtimeAdhocService;
	
	@MockBean
	private ConfigFunctionRepository configFunctionRepositoryMock;
	
	@MockBean
	private SystemDowntimeConfigRepository systemDowntimeConfigRepositoryMock;
	
	@MockBean
	private ApprovalRepository approvalRepositoryMock;
	
	@MockBean
	private UserRepository userRepositoryMock;
	
	@MockBean
	private BoSmApprovalDowntimeRepository boSmApprovalDowntimeRepositoryMock;
	
	@MockBean
	private BoDowntimeAdhocTypeRepository boDowntimeAdhocTypeRepositoryMock;
	
	@MockBean
    AdditionalDataHolder additionalDataHolder;
	
	@MockBean
	private BankRepository bankRepositoryMock;
		
	private UpdateDowntimeAdhocRequestVo updateDowntimeAdhocRequestVo;
	
	private Timestamp startTime;

	private Timestamp endTime;
	
	private java.sql.Date pushDate;
	
	private Timestamp now;
	
	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public DowntimeAdhocService getDowntimeAdhocService() {
			return new DowntimeAdhocServiceImpl();
		}
	}
	
	@Before
    public void setup(){

		updateDowntimeAdhocRequestVo = new UpdateDowntimeAdhocRequestVo();
		
		updateDowntimeAdhocRequestVo.setAdhocType("ADHOC");
		updateDowntimeAdhocRequestVo.setFunctionId(1);
		updateDowntimeAdhocRequestVo.setName("Service down");
		updateDowntimeAdhocRequestVo.setPushNotification(true);
		updateDowntimeAdhocRequestVo.setPushDate("2019-04-01");
		updateDowntimeAdhocRequestVo.setStartTime("2019-04-07T00:00:00%2B08:00");
		updateDowntimeAdhocRequestVo.setEndTime("2019-04-07T09:00:00%2B08:00");
		updateDowntimeAdhocRequestVo.setAdhocCategory("System");
		updateDowntimeAdhocRequestVo.setBankId("040");
		
		Date date= new Date();
		long time = date.getTime();
		now = new Timestamp(System.currentTimeMillis());
		startTime = now;
		endTime = now;
		
		java.sql.Date sqlDate = new java.sql.Date(time);
		pushDate = sqlDate;

    }
	
	@Test
	public void testUpdateDowntimeAdhocRequireApprovalSuccessTest() {
		int id = 1;
		String userId = "1";
		String userName = "James";
		String adhocType = "DuitNow_QR";
		String adhocTypeName = "DuitNow QR";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		
		//existing system config
		SystemDowntimeConfig systemDowntimeConfig = new SystemDowntimeConfig();
		systemDowntimeConfig.setId(1);
		systemDowntimeConfig.setName(updateDowntimeAdhocRequestVo.getName());
		systemDowntimeConfig.setStartTime(startTime);
		systemDowntimeConfig.setEndTime(endTime);
		systemDowntimeConfig.setPushNotification(updateDowntimeAdhocRequestVo.isPushNotification());
		systemDowntimeConfig.setPushDate(pushDate);
		systemDowntimeConfig.setType(ADHOC_TYPE);
		systemDowntimeConfig.setAdhocType(adhocType);
		systemDowntimeConfig.setCreatedBy(userName);
		systemDowntimeConfig.setCreatedTime(now);
		systemDowntimeConfig.setUpdatedBy(userName);
		systemDowntimeConfig.setUpdatedTime(now);
		systemDowntimeConfig.setBankId(040);
		
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findByStartTimeAndEndTime(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(systemDowntimeConfigs);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(systemDowntimeConfig);
		
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(approvalRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), 
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(approvalIdLs);
		when(boSmApprovalDowntimeRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(1);
		
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
		DowntimeAdhoc downtimeAdhoc = (DowntimeAdhoc) response.getBody();
		assertEquals(approvalId, downtimeAdhoc.getApprovalId());
	}
	
	@Test
	public void testUpdateDowntimeAdhocNotRequireApprovalSuccessTest() {
		int id = 1;
		String userId = "1";
		String userName = "James";
		String adhocType = "DuitNow_QR";
		String adhocTypeName = "DuitNow QR";
	
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		
		//existing system config
		SystemDowntimeConfig systemDowntimeConfig = new SystemDowntimeConfig();
		systemDowntimeConfig.setId(1);
		systemDowntimeConfig.setName(updateDowntimeAdhocRequestVo.getName());
		systemDowntimeConfig.setStartTime(startTime);
		systemDowntimeConfig.setEndTime(endTime);
		systemDowntimeConfig.setPushNotification(updateDowntimeAdhocRequestVo.isPushNotification());
		systemDowntimeConfig.setPushDate(pushDate);
		systemDowntimeConfig.setType(ADHOC_TYPE);
		systemDowntimeConfig.setAdhocType(updateDowntimeAdhocRequestVo.getAdhocType());
		systemDowntimeConfig.setCreatedBy(userName);
		systemDowntimeConfig.setCreatedTime(now);
		systemDowntimeConfig.setUpdatedBy(userName);
		systemDowntimeConfig.setUpdatedTime(now);
		
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findByStartTimeAndEndTime(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(systemDowntimeConfigs);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(systemDowntimeConfig);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(systemDowntimeConfigRepositoryMock.saveAndFlush(Mockito.anyObject())).thenReturn(Mockito.anyObject());
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
		DowntimeAdhoc downtimeAdhoc = (DowntimeAdhoc) response.getBody();
		assertEquals(0, downtimeAdhoc.getApprovalId());
	}
	
	@Test
	public void testUpdateDowntimeAdhocNotRequireApprovalIsPushNotificationFalseSuccessTest() {
		int id = 1;
		String userId = "1";
		String userName = "James";
		String adhocType = "DuitNow_QR";
		String adhocTypeName = "DuitNow QR";
		
		updateDowntimeAdhocRequestVo.setPushNotification(false);
		updateDowntimeAdhocRequestVo.setPushDate(null);
	
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		
		//existing system config
				SystemDowntimeConfig systemDowntimeConfig = new SystemDowntimeConfig();
				systemDowntimeConfig.setId(1);
				systemDowntimeConfig.setName(updateDowntimeAdhocRequestVo.getName());
				systemDowntimeConfig.setStartTime(startTime);
				systemDowntimeConfig.setEndTime(endTime);
				systemDowntimeConfig.setPushNotification(updateDowntimeAdhocRequestVo.isPushNotification());
				systemDowntimeConfig.setPushDate(pushDate);
				systemDowntimeConfig.setType(ADHOC_TYPE);
				systemDowntimeConfig.setAdhocType(updateDowntimeAdhocRequestVo.getAdhocType());
				systemDowntimeConfig.setCreatedBy(userName);
				systemDowntimeConfig.setCreatedTime(now);
				systemDowntimeConfig.setUpdatedBy(userName);
				systemDowntimeConfig.setUpdatedTime(now);
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findByStartTimeAndEndTime(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(systemDowntimeConfigs);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(systemDowntimeConfig);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(systemDowntimeConfigRepositoryMock.saveAndFlush(Mockito.anyObject())).thenReturn(Mockito.anyObject());
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
		DowntimeAdhoc downtimeAdhoc = (DowntimeAdhoc) response.getBody();
		assertEquals(0, downtimeAdhoc.getApprovalId());
	}
	
	@Test(expected = CommonException.class)
	public void testUpdateDowntimeAdhocNotRequireApprovalIsPushNotificationTrueAndPushDateIsNullFailTest() {
		int id = 1;
		String userId = "1";
		String userName = "James";
		
		updateDowntimeAdhocRequestVo.setPushNotification(true);
		updateDowntimeAdhocRequestVo.setPushDate(null);
	
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected = CommonException.class)
	public void testUpdateDowntimeAdhocNotRequireApprovalInvalidUserIdFailTest() {
		int id = 1;
		String userId = "1";
		String userName = "James";
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected = CommonException.class)
	public void testUpdateDowntimeAdhocNotRequireApprovalInvalidStartTimeFormatFailTest() {
		int id = 1;
		String userId = "1";
		String userName = "James";
		
		updateDowntimeAdhocRequestVo.setStartTime("2019-04-07T00:00:00 08:00");
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected = CommonException.class)
	public void testUpdateDowntimeAdhocNotRequireApprovalInvalidPushDateFormatFailTest() {
		int id = 1;
		String userId = "1";
		String userName = "James";
		
		updateDowntimeAdhocRequestVo.setPushDate("20190401");
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected = AdhocDurationOverlappedException.class)
	public void testUpdateDowntimeAdhocRequireApprovalDurationOverlappedFailTest() {
		int id = 1;
		String userId = "1";
		String userName = "James";
		int approvalId = 3;
		Date date = new Date();

		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		List<SystemDowntimeConfig> duplicatesystemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		SystemDowntimeConfig systemDowntimeConfig = new SystemDowntimeConfig();
		systemDowntimeConfig.setId(1);
		systemDowntimeConfig.setName("Downtime adhoc 1");
		systemDowntimeConfig.setAdhocType("System");
		systemDowntimeConfig.setType(ADHOC_TYPE);
		systemDowntimeConfig.setCreatedTime(new Timestamp(date.getTime()));
		systemDowntimeConfig.setUpdatedTime(new Timestamp(date.getTime()));
		systemDowntimeConfig.setIsActive("1");
		systemDowntimeConfig.setPushNotification(false);
		systemDowntimeConfig.setStartTime(startTime);
		systemDowntimeConfig.setEndTime(endTime);
		systemDowntimeConfig.setPushDate(pushDate);
		systemDowntimeConfig.setCreatedBy(userName);
		systemDowntimeConfig.setCreatedTime(now);
		systemDowntimeConfig.setUpdatedBy(userName);
		systemDowntimeConfig.setUpdatedTime(now);
		duplicatesystemDowntimeConfigs.add(systemDowntimeConfig);
		
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findByStartTimeAndEndTimeForUpdate(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(duplicatesystemDowntimeConfigs);
		
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);

	}
	
	@Test(expected=PendingApprovalException.class)
	public void testUpdateDowntimeAdhocRequireApprovalPendingApprovalFailTest() {
		int id = 1;
		String userId = "1";
		String userName = "James";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		
		//existing system config
		SystemDowntimeConfig systemDowntimeConfig = new SystemDowntimeConfig();
		systemDowntimeConfig.setId(1);
		systemDowntimeConfig.setName(updateDowntimeAdhocRequestVo.getName());
		systemDowntimeConfig.setStartTime(startTime);
		systemDowntimeConfig.setEndTime(endTime);
		systemDowntimeConfig.setPushNotification(updateDowntimeAdhocRequestVo.isPushNotification());
		systemDowntimeConfig.setPushDate(pushDate);
		systemDowntimeConfig.setType(ADHOC_TYPE);
		systemDowntimeConfig.setAdhocType(updateDowntimeAdhocRequestVo.getAdhocType());
		systemDowntimeConfig.setCreatedBy(userName);
		systemDowntimeConfig.setCreatedTime(now);
		systemDowntimeConfig.setUpdatedBy(userName);
		systemDowntimeConfig.setUpdatedTime(now);
		
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);
		
		List<Integer> approvalIds= new ArrayList<Integer>();
		approvalIds.add(1);
		
		List<BoSmApprovalDowntime> boSmApprovalDowntimes = new ArrayList<BoSmApprovalDowntime>();
		BoSmApprovalDowntime boSmApprovalDowntime = new BoSmApprovalDowntime();
		boSmApprovalDowntimes.add(boSmApprovalDowntime);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findByStartTimeAndEndTime(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(systemDowntimeConfigs);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(systemDowntimeConfig);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(approvalRepositoryMock.findIdByFunctionIdAndStatus(Mockito.anyInt(), Mockito.anyString())).thenReturn(approvalIds);
		when(boSmApprovalDowntimeRepositoryMock.findByApprovalIdAndLockingId(Mockito.anyObject(), Mockito.anyString())).thenReturn(boSmApprovalDowntimes);
		
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected=CommonException.class)
	public void testUpdateDowntimeAdhocRequireApprovalSystemConfigNotFoundFailTest() {
		int id = 1;
		String userId = "1";
		String userName = "James";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		
		//existing system config
		SystemDowntimeConfig systemDowntimeConfig = new SystemDowntimeConfig();
		systemDowntimeConfig.setId(1);
		systemDowntimeConfig.setName(updateDowntimeAdhocRequestVo.getName());
		systemDowntimeConfig.setStartTime(startTime);
		systemDowntimeConfig.setEndTime(endTime);
		systemDowntimeConfig.setPushNotification(updateDowntimeAdhocRequestVo.isPushNotification());
		systemDowntimeConfig.setPushDate(pushDate);
		systemDowntimeConfig.setType(ADHOC_TYPE);
		systemDowntimeConfig.setAdhocType(updateDowntimeAdhocRequestVo.getAdhocType());
		systemDowntimeConfig.setCreatedBy(userName);
		systemDowntimeConfig.setCreatedTime(now);
		systemDowntimeConfig.setUpdatedBy(userName);
		systemDowntimeConfig.setUpdatedTime(now);
		
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);
		
		List<Integer> approvalIds= new ArrayList<Integer>();
		approvalIds.add(1);
		
		List<BoSmApprovalDowntime> boSmApprovalDowntimes = new ArrayList<BoSmApprovalDowntime>();
		BoSmApprovalDowntime boSmApprovalDowntime = new BoSmApprovalDowntime();
		boSmApprovalDowntimes.add(boSmApprovalDowntime);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findByStartTimeAndEndTime(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(systemDowntimeConfigs);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(null);
		
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected=CommonException.class)
	public void testUpdateDowntimeAdhocRequireApprovalMissingMandatoryParamIdFailTest() {
		int id = 1;
		String userId = "1";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, 0, userId);
	}
	
	@Test(expected=CommonException.class)
	public void testUpdateDowntimeAdhocRequireApprovalMissingMandatoryParamNameFailTest() {
		int id = 1;
		String userId = "1";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		updateDowntimeAdhocRequestVo.setName(null);
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected=CommonException.class)
	public void testUpdateDowntimeAdhocRequireApprovalMissingMandatoryParamAdhocTypeFailTest() {
		int id = 1;
		String userId = "1";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		updateDowntimeAdhocRequestVo.setAdhocType(null);
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected=CommonException.class)
	public void testUpdateDowntimeAdhocRequireApprovalMissingMandatoryParamcFunctionIdFailTest() {
		int id = 1;
		String userId = "1";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		updateDowntimeAdhocRequestVo.setFunctionId(0);
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected=CommonException.class)
	public void testUpdateDowntimeAdhocRequireApprovalMissingMandatoryParamStartTimeFailTest() {
		int id = 1;
		String userId = "1";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		updateDowntimeAdhocRequestVo.setStartTime(null);
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected=CommonException.class)
	public void testUpdateDowntimeAdhocRequireApprovalMissingMandatoryParamEndTimeFailTest() {
		int id = 1;
		String userId = "1";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		updateDowntimeAdhocRequestVo.setEndTime(null);
		ResponseEntity<BoData> response = downtimeAdhocService.updateDowntimeAdhoc(updateDowntimeAdhocRequestVo, id, userId);
	}
}
