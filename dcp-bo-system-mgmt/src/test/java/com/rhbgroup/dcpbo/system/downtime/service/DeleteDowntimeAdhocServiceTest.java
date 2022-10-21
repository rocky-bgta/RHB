package com.rhbgroup.dcpbo.system.downtime.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.exception.DeleteAdhocNotAllowedException;
import com.rhbgroup.dcpbo.system.exception.PendingApprovalException;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntime;
import com.rhbgroup.dcpbo.system.model.ConfigFunction;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DowntimeAdhocService.class, DeleteDowntimeAdhocServiceTest.Config.class })
public class DeleteDowntimeAdhocServiceTest {
	
	private static final String ADHOC_TYPE = "ADHOC";
	
	private static final String IS_ACTIVE_1 = "1";
	
	private static final String IS_ACTIVE_0 = "0";

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
		
	private DeleteDowntimeAdhocRequestVo deleteDowntimeAdhocRequestVo;
	
	private java.sql.Date pushDate;
	
	private Timestamp now;
	
	private SystemDowntimeConfig systemDowntimeConfig;
	
	private String userName;
	
	private String bankName;
	
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

		deleteDowntimeAdhocRequestVo = new DeleteDowntimeAdhocRequestVo();
		deleteDowntimeAdhocRequestVo.setFunctionId(1);
		
		Date date= new Date();
		long time = date.getTime();
		now = new Timestamp(System.currentTimeMillis());
		
		java.sql.Date sqlDate = new java.sql.Date(time);
		pushDate = sqlDate;
		userName = "James";
		
		bankName= "RHB";
		
		//setting start time and end time to future date
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now.getTime());
		cal.add(Calendar.DATE, 10);
		cal.add(Calendar.HOUR, 2);
		Timestamp endTime = new Timestamp(cal.getTime().getTime());
		cal.add(Calendar.HOUR, -4);
		Timestamp startTime = new Timestamp(cal.getTime().getTime());
		
		systemDowntimeConfig = new SystemDowntimeConfig();
		systemDowntimeConfig.setId(1);
		systemDowntimeConfig.setName("Downtime adhoc 1");
		systemDowntimeConfig.setStartTime(startTime);
		systemDowntimeConfig.setEndTime(endTime);
		systemDowntimeConfig.setPushNotification(true);
		systemDowntimeConfig.setPushDate(pushDate);
		systemDowntimeConfig.setType(ADHOC_TYPE);
		systemDowntimeConfig.setAdhocType("ADHOC");
		systemDowntimeConfig.setIsActive(IS_ACTIVE_1);
		systemDowntimeConfig.setCreatedBy(userName);
		systemDowntimeConfig.setCreatedTime(now);
		systemDowntimeConfig.setUpdatedBy(userName);
		systemDowntimeConfig.setUpdatedTime(now);
		systemDowntimeConfig.setBankId(040);

    }
	
	@Test
	public void testDeleteDowntimeAdhocRequireApprovalSuccessTest() {
		int id = 1;
		String userId = "1";
		
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(systemDowntimeConfig);
		
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(approvalRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), 
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(approvalIdLs);
		when(boSmApprovalDowntimeRepositoryMock.insert(Mockito.anyInt(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(),
				Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject())).thenReturn(1);
		when(bankRepositoryMock.getBankNameById(Mockito.anyObject())).thenReturn(bankName);
		
		ResponseEntity<BoData> response = downtimeAdhocService.deleteDowntimeAdhoc(deleteDowntimeAdhocRequestVo, id, userId);
		DowntimeAdhoc downtimeAdhoc = (DowntimeAdhoc) response.getBody();
		assertEquals(approvalId, downtimeAdhoc.getApprovalId());
	}
	
	@Test
	public void testDeleteDowntimeAdhocNotRequireApprovalSuccessTest() {
		int id = 1;
		String userId = "1";
			
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(systemDowntimeConfig);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(bankRepositoryMock.getBankNameById(Mockito.anyObject())).thenReturn(bankName);
		when(systemDowntimeConfigRepositoryMock.saveAndFlush(Mockito.anyObject())).thenReturn(Mockito.anyObject());
		ResponseEntity<BoData> response = downtimeAdhocService.deleteDowntimeAdhoc(deleteDowntimeAdhocRequestVo, id, userId);
		DowntimeAdhoc downtimeAdhoc = (DowntimeAdhoc) response.getBody();
		assertEquals(0, downtimeAdhoc.getApprovalId());
	}
	

	@Test(expected = CommonException.class)
	public void testDeleteDowntimeAdhocNotRequireApprovalInvalidUserIdFailTest() {
		int id = 1;
		String userId = "1";

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(false);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		ResponseEntity<BoData> response = downtimeAdhocService.deleteDowntimeAdhoc(deleteDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected = CommonException.class)
	public void testDeleteDowntimeAdhocRequireApprovalSystemConfigNotActiveFailTest() {
		int id = 1;
		String userId = "1";

		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);
		
		systemDowntimeConfig.setIsActive(IS_ACTIVE_0);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(systemDowntimeConfig);
		ResponseEntity<BoData> response = downtimeAdhocService.deleteDowntimeAdhoc(deleteDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected = DeleteAdhocNotAllowedException.class)
	public void testDeleteDowntimeAdhocRequireApprovalSystemConfigIsActivatedFailTest() {
		int id = 1;
		String userId = "1";

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now.getTime());
		cal.add(Calendar.HOUR, 2);
		Timestamp endTime = new Timestamp(cal.getTime().getTime());
		cal.add(Calendar.HOUR, -4);
		Timestamp startTime = new Timestamp(cal.getTime().getTime());
		systemDowntimeConfig.setStartTime(startTime);
		systemDowntimeConfig.setEndTime(endTime);
		
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(systemDowntimeConfig);
		
		ResponseEntity<BoData> response = downtimeAdhocService.deleteDowntimeAdhoc(deleteDowntimeAdhocRequestVo, id, userId);

	}
	
	@Test(expected=PendingApprovalException.class)
	public void testDeleteDowntimeAdhocRequireApprovalPendingApprovalFailTest() {
		int id = 1;
		String userId = "1";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);
		
		List<Integer> approvalIds= new ArrayList<Integer>();
		approvalIds.add(1);
		
		List<BoSmApprovalDowntime> boSmApprovalDowntimes = new ArrayList<BoSmApprovalDowntime>();
		BoSmApprovalDowntime boSmApprovalDowntime = new BoSmApprovalDowntime();
		boSmApprovalDowntimes.add(boSmApprovalDowntime);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(systemDowntimeConfig);
		when(configFunctionRepositoryMock.findOne(Mockito.anyInt())).thenReturn(configFunction);
		when(approvalRepositoryMock.findIdByFunctionIdAndStatus(Mockito.anyInt(), Mockito.anyString())).thenReturn(approvalIds);
		when(boSmApprovalDowntimeRepositoryMock.findByApprovalIdAndLockingId(Mockito.anyObject(), Mockito.anyString())).thenReturn(boSmApprovalDowntimes);
		
		ResponseEntity<BoData> response = downtimeAdhocService.deleteDowntimeAdhoc(deleteDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected=CommonException.class)
	public void testDeleteDowntimeAdhocRequireApprovalSystemConfigNotFoundFailTest() {
		int id = 1;
		String userId = "1";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setApprovalRequired(true);
		
		List<Integer> approvalIds= new ArrayList<Integer>();
		approvalIds.add(1);
		
		List<BoSmApprovalDowntime> boSmApprovalDowntimes = new ArrayList<BoSmApprovalDowntime>();
		BoSmApprovalDowntime boSmApprovalDowntime = new BoSmApprovalDowntime();
		boSmApprovalDowntimes.add(boSmApprovalDowntime);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(null);
		
		ResponseEntity<BoData> response = downtimeAdhocService.deleteDowntimeAdhoc(deleteDowntimeAdhocRequestVo, id, userId);
	}
	
	@Test(expected=CommonException.class)
	public void testDeleteDowntimeAdhocRequireApprovalMissingMandatoryParamIdFailTest() {
		String userId = "1";
		int approvalId = 3;
		List<Integer> approvalIdLs = new ArrayList<Integer>();
		approvalIdLs.add(approvalId);
		
		ResponseEntity<BoData> response = downtimeAdhocService.deleteDowntimeAdhoc(deleteDowntimeAdhocRequestVo, 0, userId);
	}

}
