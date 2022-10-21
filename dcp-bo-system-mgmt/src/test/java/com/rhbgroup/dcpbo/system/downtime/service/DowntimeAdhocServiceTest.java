package com.rhbgroup.dcpbo.system.downtime.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
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
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BankRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoDowntimeAdhocTypeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoSmApprovalDowntimeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.ConfigFunctionRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.SystemDowntimeConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.service.DowntimeAdhocService;
import com.rhbgroup.dcpbo.system.downtime.service.impl.DowntimeAdhocServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.vo.AddDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.exception.AdhocDurationOverlappedException;
import com.rhbgroup.dcpbo.system.model.ConfigFunction;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DowntimeAdhocService.class, DowntimeAdhocServiceTest.Config.class })
public class DowntimeAdhocServiceTest {

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
	private BoDowntimeAdhocTypeRepository adhocTypeRepositoryMock;
	
	@MockBean
    AdditionalDataHolder additionalDataHolder;
	
	@MockBean
	private BankRepository bankRepositoryMock;
		
	private AddDowntimeAdhocRequestVo addDowntimeAdhocRequestVo;

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

		addDowntimeAdhocRequestVo = new AddDowntimeAdhocRequestVo();
		
		addDowntimeAdhocRequestVo.setAdhocType("ADHOC");
		addDowntimeAdhocRequestVo.setFunctionId(1);
		addDowntimeAdhocRequestVo.setName("Service down");
		addDowntimeAdhocRequestVo.setPushNotification(true);
		addDowntimeAdhocRequestVo.setPushDate("2019-04-01");
		addDowntimeAdhocRequestVo.setStartTime("2019-04-07T00:00:00%2B08:00");
		addDowntimeAdhocRequestVo.setEndTime("2019-04-07T09:00:00%2B08:00");
		addDowntimeAdhocRequestVo.setAdhocCategory("System");
		addDowntimeAdhocRequestVo.setBankId("040");
		addDowntimeAdhocRequestVo.setBankName("RHB");

    }
	
	@Test
	public void testAddDowntimeAdhocRequireApprovalSuccessTest() {
		String userId = "1";
		String userName = "James";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findByStartTimeAndEndTime(Mockito.anyObject(), Mockito.anyObject(),Mockito.anyObject(),Mockito.anyObject(),Mockito.anyObject())).thenReturn(systemDowntimeConfigs);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(approvalRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), 
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(approvalIdLs);
		when(boSmApprovalDowntimeRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(1);
		
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);
		DowntimeAdhoc downtimeAdhoc = (DowntimeAdhoc) response.getBody();
		assertEquals(approvalId, downtimeAdhoc.getApprovalId());
	}
	
	@Test
	public void testAddDowntimeAdhocNotRequireApprovalSuccessTest() {
		String userId = "1";
		String userName = "James";
	
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findByStartTimeAndEndTime(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(systemDowntimeConfigs);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(systemDowntimeConfigRepositoryMock.saveAndFlush(Mockito.anyObject())).thenReturn(Mockito.anyObject());
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);
		DowntimeAdhoc downtimeAdhoc = (DowntimeAdhoc) response.getBody();
		assertEquals(0, downtimeAdhoc.getApprovalId());
	}
	
	@Test
	public void testAddDowntimeAdhocNotRequireApprovalIsPushNotificationFalseSuccessTest() {
		String userId = "1";
		String userName = "James";
		
		addDowntimeAdhocRequestVo.setPushNotification(false);
		addDowntimeAdhocRequestVo.setPushDate(null);
	
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findByStartTimeAndEndTime(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(systemDowntimeConfigs);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(systemDowntimeConfigRepositoryMock.saveAndFlush(Mockito.anyObject())).thenReturn(Mockito.anyObject());
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);
		DowntimeAdhoc downtimeAdhoc = (DowntimeAdhoc) response.getBody();
		assertEquals(0, downtimeAdhoc.getApprovalId());
	}
	
	@Test(expected = CommonException.class)
	public void testAddDowntimeAdhocNotRequireApprovalIsPushNotificationTrueAndPushDateIsNullFailTest() {
		String userId = "1";
		String userName = "James";
		
		addDowntimeAdhocRequestVo.setPushNotification(true);
		addDowntimeAdhocRequestVo.setPushDate(null);
	
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void testAddDowntimeAdhocNotRequireApprovalInvalidUserIdFailTest() {
		String userId = "1";
		String userName = "James";
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void testAddDowntimeAdhocNotRequireApprovalInvalidStartTimeFormatFailTest() {
		String userId = "1";
		String userName = "James";
		
		addDowntimeAdhocRequestVo.setStartTime("2019-04-07T00:00:00%2B08:00");
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void testAddDowntimeAdhocNotRequireApprovalInvalidPushDateFormatFailTest() {
		String userId = "1";
		String userName = "James";
		
		addDowntimeAdhocRequestVo.setPushDate("20190401");
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);
	}
	
	@Test(expected = AdhocDurationOverlappedException.class)
	public void testAddDowntimeAdhocRequireApprovalDurationOverlappedFailTest() {
		String userId = "1";
		String userName = "James";
		int approvalId = 3;
		Date date = new Date();

		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		SystemDowntimeConfig systemDowntimeConfig = new SystemDowntimeConfig();
		systemDowntimeConfig.setName("Downtime adhoc 1");
		systemDowntimeConfig.setAdhocType("System");
		systemDowntimeConfig.setType("ADHOC");
		systemDowntimeConfig.setCreatedTime(new Timestamp(date.getTime()));
		systemDowntimeConfig.setUpdatedTime(new Timestamp(date.getTime()));
		systemDowntimeConfig.setIsActive("1");
		systemDowntimeConfig.setPushNotification(false);
		systemDowntimeConfigs.add(systemDowntimeConfig);
		
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findByStartTimeAndEndTime(Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(systemDowntimeConfigs);
		
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);

	}
	
	@Test(expected = CommonException.class)
	public void testAddDowntimeAdhocNotRequireApprovalMissingMandatoryParamNameFailTest() {
		String userId = "1";
		
		addDowntimeAdhocRequestVo.setPushDate("20190401");
		addDowntimeAdhocRequestVo.setName(null);
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void testAddDowntimeAdhocNotRequireApprovalMissingMandatoryParamAdhocTypeFailTest() {
		String userId = "1";
		
		addDowntimeAdhocRequestVo.setPushDate("20190401");
		addDowntimeAdhocRequestVo.setAdhocType(null);
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void testAddDowntimeAdhocNotRequireApprovalMissingMandatoryParamFunctionIdFailTest() {
		String userId = "1";
		
		addDowntimeAdhocRequestVo.setPushDate("20190401");
		addDowntimeAdhocRequestVo.setFunctionId(0);
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void testAddDowntimeAdhocNotRequireApprovalMissingMandatoryParamStartTimeTest() {
		String userId = "1";
		
		addDowntimeAdhocRequestVo.setPushDate("20190401");
		addDowntimeAdhocRequestVo.setStartTime(null);
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void testAddDowntimeAdhocNotRequireApprovalMissingMandatoryParamEndTimeTest() {
		String userId = "1";
		
		addDowntimeAdhocRequestVo.setPushDate("20190401");
		addDowntimeAdhocRequestVo.setEndTime(null);
		
		List<SystemDowntimeConfig> systemDowntimeConfigs = new ArrayList<SystemDowntimeConfig>();
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		ResponseEntity<BoData> response = downtimeAdhocService.addDowntimeAdhoc(addDowntimeAdhocRequestVo, userId);
	}
}
