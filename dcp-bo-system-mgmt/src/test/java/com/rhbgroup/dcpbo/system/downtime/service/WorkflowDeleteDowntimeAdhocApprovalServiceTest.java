package com.rhbgroup.dcpbo.system.downtime.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.common.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.common.exception.ConfigErrorInterface;
import com.rhbgroup.dcpbo.common.exception.ErrorResponse;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.UpdateApprovalDowntimeAdhoc;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoDowntimeAdhocTypeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoSmApprovalDowntimeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.ConfigFunctionRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.SystemDowntimeConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.service.impl.WorkflowDowntimeServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocApprovalRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocApprovalResponseVo;
import com.rhbgroup.dcpbo.system.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.system.exception.DeleteAdhocNotAllowedException;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;
import com.rhbgroup.dcpbo.system.model.Approval;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntime;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {WorkflowDeleteDowntimeAdhocApprovalServiceTest.class, WorkflowDowntimeServiceImpl.class})
public class WorkflowDeleteDowntimeAdhocApprovalServiceTest {
	
	private static final String ADHOC_TYPE = "ADHOC";
	
	private static final String IS_ACTIVE_1 = "1";
	
	@Autowired
	WorkflowDowntimeService workflowDowntimeService;
	
	@MockBean
	ApprovalRepository approvalRepositoryMock;
	
	@MockBean
	BoSmApprovalDowntimeRepository boSmApprovalDowntimeRepositoryMock;

	@MockBean
	UserRepository userRepositoryMock;

	@MockBean
	private ConfigFunctionRepository configFunctionRepositoryMock;
	
	@MockBean
	private SystemDowntimeConfigRepository systemDowntimeConfigRepositoryMock;
	
	@MockBean
    BoDowntimeAdhocTypeRepository boDowntimeAdhocTypeRepositoryMock; 
	
	@MockBean
    AdditionalDataHolder additionalDataHolder;
		
	private DeleteDowntimeAdhocApprovalRequestVo deleteDowntimeAdhocApprovalRequestVo;
	
	private java.sql.Date pushDate;
	
	private Timestamp now;
	
	private SystemDowntimeConfig systemDowntimeConfig;
	
	private BoSmApprovalDowntime boSmApprovalDowntime;
	
	private Approval approval;
	
	private String userName;
	
	@TestConfiguration
	static class Config {
		@Bean
		public CommonException getCommonException() {
			return new CommonException(CommonException.GENERIC_ERROR_CODE, "NA");
		}

		@Bean
		public DowntimeExceptionAdvice getDowntimeExceptionAdvice() {
			return new DowntimeExceptionAdvice();
		}
		
		@Bean
		public CommonExceptionAdvice getCommonExceptionAdvice() {
			return new CommonExceptionAdvice();
		}
		
		@Bean
		public ConfigErrorInterface getConfigErrorInterface() {
			return new ConfigInterfaceImpl();
		}
	
		class ConfigInterfaceImpl implements ConfigErrorInterface {
			@Override
			public ErrorResponse getConfigError(String errorCode) {
				return new ErrorResponse(errorCode, "Invalid date format");
			}
		}
	}


	@Before
        public void setup(){

		deleteDowntimeAdhocApprovalRequestVo = new DeleteDowntimeAdhocApprovalRequestVo();
		deleteDowntimeAdhocApprovalRequestVo.setReason("Wrong config");
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		Date date= new Date();
		long time = date.getTime();
		now = new Timestamp(System.currentTimeMillis());
		
		java.sql.Date sqlDate = new java.sql.Date(time);
		pushDate = sqlDate;
		userName = "James";
		Integer approvalId = 123;
		
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
		
		approval = new Approval();
		approval.setFunctionId(15);
		approval.setStatus("P");
		approval.setActionType("DELETE");
		approval.setCreatedBy("dcpbo2");
		approval.setCreatedTime(new Timestamp(System.currentTimeMillis() - 100000));
		approval.setUpdatedBy("dcpbo2");
		approval.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
		approval.setCreatorId(234);
		
		String payloadB = "";
		UpdateApprovalDowntimeAdhoc updateApprovalDowntimeAdhocB = new UpdateApprovalDowntimeAdhoc();
		updateApprovalDowntimeAdhocB.setId(systemDowntimeConfig.getId());
		updateApprovalDowntimeAdhocB.setName(systemDowntimeConfig.getName());
		updateApprovalDowntimeAdhocB.setStartTime(systemDowntimeConfig.getStartTimeString());
		updateApprovalDowntimeAdhocB.setEndTime(systemDowntimeConfig.getEndTimeString());
		updateApprovalDowntimeAdhocB.setIsPushNotification(systemDowntimeConfig.isPushNotification());
		updateApprovalDowntimeAdhocB.setPushDate(systemDowntimeConfig.getPushDateString());
		updateApprovalDowntimeAdhocB.setType(systemDowntimeConfig.getType());
		updateApprovalDowntimeAdhocB.setAdhocType(systemDowntimeConfig.getAdhocType());
		try {
			payloadB = objectMapper.writeValueAsString(updateApprovalDowntimeAdhocB);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		boSmApprovalDowntime = new BoSmApprovalDowntime();
		boSmApprovalDowntime.setApprovalId(approvalId);
		boSmApprovalDowntime.setPayload(payloadB);
		boSmApprovalDowntime.setCreatedBy(userName);
		boSmApprovalDowntime.setCreatedTime(now);

    }
	
	@Test
	public void deleteDowntimeAdhocApprovalSuccessTest() throws Exception {

		Integer approvalId = 123;
		String userId = "1";
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		when(boSmApprovalDowntimeRepositoryMock.findOneByApprovalId(Mockito.anyInt())).thenReturn(boSmApprovalDowntime);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(systemDowntimeConfig);
		when(approvalRepositoryMock.saveAndFlush(approval)).thenReturn(Mockito.anyObject());
		when(systemDowntimeConfigRepositoryMock.saveAndFlush(systemDowntimeConfig)).thenReturn(Mockito.anyObject());
		
		BoData response = workflowDowntimeService.deleteApproval(approvalId, deleteDowntimeAdhocApprovalRequestVo, userId);
		DeleteDowntimeAdhocApprovalResponseVo deleteDowntimeAdhocApprovalResponseVo = (DeleteDowntimeAdhocApprovalResponseVo) response;
		
		System.out.println("    aprroval ID: " + deleteDowntimeAdhocApprovalResponseVo.getApprovalId());
		assertEquals((int)approvalId, deleteDowntimeAdhocApprovalResponseVo.getApprovalId());
	}
	
	@Test(expected = DeleteAdhocNotAllowedException.class)
	public void deleteDowntimeAdhocApprovalSystemConfigIsActivatedFailTest() {
		Integer approvalId = 123;
		String userId = "1";

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now.getTime());
		cal.add(Calendar.HOUR, 2);
		Timestamp endTime = new Timestamp(cal.getTime().getTime());
		cal.add(Calendar.HOUR, -4);
		Timestamp startTime = new Timestamp(cal.getTime().getTime());
		systemDowntimeConfig.setStartTime(startTime);
		systemDowntimeConfig.setEndTime(endTime);
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		when(boSmApprovalDowntimeRepositoryMock.findOneByApprovalId(Mockito.anyInt())).thenReturn(boSmApprovalDowntime);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(systemDowntimeConfig);
		
		workflowDowntimeService.deleteApproval(approvalId, deleteDowntimeAdhocApprovalRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void deleteDowntimeAdhocApprovalInvalidUserIdFailTest() {
		Integer approvalId = 123;
		String userId = "1";

		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		workflowDowntimeService.deleteApproval(approvalId, deleteDowntimeAdhocApprovalRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void deleteDowntimeAdhocApprovalInvalidApprovalIdFailTest() {
		Integer approvalId = 123;
		String userId = "1";

		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(null);
		workflowDowntimeService.deleteApproval(approvalId, deleteDowntimeAdhocApprovalRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void deleteDowntimeAdhocApprovalDetailNotFoundFailTest() {
		Integer approvalId = 123;
		String userId = "1";

		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		when(boSmApprovalDowntimeRepositoryMock.findOneByApprovalId(Mockito.anyInt())).thenReturn(null);
		workflowDowntimeService.deleteApproval(approvalId, deleteDowntimeAdhocApprovalRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void deleteDowntimeAdhocApprovalActionTypeNotDeleteFailTest() {
		Integer approvalId = 123;
		String userId = "1";

		//set action type = ADD instead of DELETE
		approval.setActionType(MaintenanceActionType.ADD.getValue());
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		workflowDowntimeService.deleteApproval(approvalId, deleteDowntimeAdhocApprovalRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void deleteDowntimeAdhocApprovalStatusNotPendingFailTest() {
		Integer approvalId = 123;
		String userId = "1";

		//set status = A (approved) instead of P (pending)
		approval.setStatus(MaintenanceActionType.STATUS_APPROVED.getValue());
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		workflowDowntimeService.deleteApproval(approvalId, deleteDowntimeAdhocApprovalRequestVo, userId);
	}
	
	@Test(expected = CommonException.class)
	public void deleteDowntimeAdhocApprovalSystemDowntimeConfigNotFoundFailTest() {
		Integer approvalId = 123;
		String userId = "1";

		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		when(boSmApprovalDowntimeRepositoryMock.findOneByApprovalId(Mockito.anyInt())).thenReturn(boSmApprovalDowntime);
		when(systemDowntimeConfigRepositoryMock.findOneById(Mockito.anyInt())).thenReturn(null);
		workflowDowntimeService.deleteApproval(approvalId, deleteDowntimeAdhocApprovalRequestVo, userId);
	}
	
}
