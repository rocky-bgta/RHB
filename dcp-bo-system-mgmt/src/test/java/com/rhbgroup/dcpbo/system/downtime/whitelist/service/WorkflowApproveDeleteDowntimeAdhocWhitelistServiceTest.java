package com.rhbgroup.dcpbo.system.downtime.whitelist.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

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
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.common.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.common.exception.ConfigErrorInterface;
import com.rhbgroup.dcpbo.common.exception.ErrorResponse;
import com.rhbgroup.dcpbo.system.downtime.dto.AdhocApproval;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.dto.ApprovalDowntimeAdhocWhitelistPayload;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.BoSmApprovalDowntimeWhitelistRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.repository.SystemDowntimeWhitelistConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.impl.WorkflowDowntimeAdhocWhitelistServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.ApproveDeleteDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;
import com.rhbgroup.dcpbo.system.model.Approval;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntimeWhitelist;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeWhitelistConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {WorkflowApproveDeleteDowntimeAdhocWhitelistServiceTest.class, WorkflowDowntimeAdhocWhitelistServiceImpl.class})
public class WorkflowApproveDeleteDowntimeAdhocWhitelistServiceTest {
	
	private static final String ADHOC_TYPE = "ADHOC";
	
	@Autowired
	private WorkflowDowntimeAdhocWhitelistService workflowDowntimeAdhocWhitelistService;
	
	@MockBean
	private ApprovalRepository approvalRepositoryMock;
	
	@MockBean
	private BoSmApprovalDowntimeWhitelistRepository boSmApprovalDowntimeWhitelistRepositoryMock;

	@MockBean
	private UserRepository userRepositoryMock;

	@MockBean
	private SystemDowntimeWhitelistConfigRepository systemDowntimeWhitelistConfigRepositoryMock;
	
	@MockBean
	private AdditionalDataHolder additionalDataHolder;
		
	private Timestamp now;
	
	private SystemDowntimeWhitelistConfig systemDowntimeWhitelistConfig;
	
	private BoSmApprovalDowntimeWhitelist boSmApprovalDowntimeWhitelist;
	
	private Approval approval;
	
	private String userName;
	
	private ApproveDeleteDowntimeAdhocWhitelistRequest approveDeleteDowntimeAdhocWhitelistRequest;
	
	private List<SystemDowntimeWhitelistConfig> systemDowntimeWhitelistConfigs;
	
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

		approveDeleteDowntimeAdhocWhitelistRequest = new ApproveDeleteDowntimeAdhocWhitelistRequest();
		approveDeleteDowntimeAdhocWhitelistRequest.setReason("Ok");
		
		ObjectMapper objectMapper = new ObjectMapper();
		
		Date date= new Date();
		long time = date.getTime();
		now = new Timestamp(System.currentTimeMillis());
		
		userName = "James";
		Integer approvalId = 123;
		
		systemDowntimeWhitelistConfigs = new ArrayList<SystemDowntimeWhitelistConfig>();
		systemDowntimeWhitelistConfig = new SystemDowntimeWhitelistConfig();
		systemDowntimeWhitelistConfig.setId(1);
		systemDowntimeWhitelistConfig.setUserId(1);
		systemDowntimeWhitelistConfig.setType(ADHOC_TYPE);
		systemDowntimeWhitelistConfig.setCreatedBy(userName);
		systemDowntimeWhitelistConfig.setCreatedTime(now);
		systemDowntimeWhitelistConfig.setUpdatedBy(userName);
		systemDowntimeWhitelistConfig.setUpdatedTime(now);
		
		approval = new Approval();
		approval.setFunctionId(15);
		approval.setStatus("P");
		approval.setActionType("DELETE");
		approval.setCreatedBy("dcpbo2");
		approval.setCreatedTime(new Timestamp(System.currentTimeMillis() - 100000));
		approval.setUpdatedBy("dcpbo2");
		approval.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
		approval.setCreatorId(234);
		
		String payloadA = "";
		ApprovalDowntimeAdhocWhitelistPayload approvalDowntimeAdhocWhitelistPayload = new ApprovalDowntimeAdhocWhitelistPayload();
		approvalDowntimeAdhocWhitelistPayload.setUserId(systemDowntimeWhitelistConfig.getUserId());
		approvalDowntimeAdhocWhitelistPayload.setName(userName);
		approvalDowntimeAdhocWhitelistPayload.setUsername("dcpbo12");
		approvalDowntimeAdhocWhitelistPayload.setType(ADHOC_TYPE);
		approvalDowntimeAdhocWhitelistPayload.setMobileNo("01289384");
		approvalDowntimeAdhocWhitelistPayload.setIdNo("760909106868");
		approvalDowntimeAdhocWhitelistPayload.setIdType("MK");
		approvalDowntimeAdhocWhitelistPayload.setCisNo("90394093434");

		try {
			payloadA = objectMapper.writeValueAsString(approvalDowntimeAdhocWhitelistPayload);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		boSmApprovalDowntimeWhitelist = new BoSmApprovalDowntimeWhitelist();
		boSmApprovalDowntimeWhitelist.setApprovalId(approvalId);
		boSmApprovalDowntimeWhitelist.setPayload(payloadA);
		boSmApprovalDowntimeWhitelist.setCreatedBy(userName);
		boSmApprovalDowntimeWhitelist.setCreatedTime(now);

    }
	
	@Test
	public void deleteDowntimeAdhocWhitelistApprovalSuccessTest() throws Exception {

		Integer approvalId = 123;
		String userId = "1";
		
		systemDowntimeWhitelistConfigs.add(systemDowntimeWhitelistConfig);
		
		doNothing().when(systemDowntimeWhitelistConfigRepositoryMock).delete(Mockito.anyInt());
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		when(boSmApprovalDowntimeWhitelistRepositoryMock.findOneByApprovalId(Mockito.anyInt())).thenReturn(boSmApprovalDowntimeWhitelist);
		when(systemDowntimeWhitelistConfigRepositoryMock.findByUserIdAndType(Mockito.anyInt(), Mockito.anyString())).thenReturn(systemDowntimeWhitelistConfigs);
		when(approvalRepositoryMock.saveAndFlush(approval)).thenReturn(Mockito.anyObject());
		
		AdhocApproval response = (AdhocApproval) workflowDowntimeAdhocWhitelistService.approveDeleteDowntimeWhitelist(approvalId, approveDeleteDowntimeAdhocWhitelistRequest, userId);
		
		System.out.println("    aprroval ID: " + response.getApprovalId());
		assertEquals((int)approvalId, response.getApprovalId());
	}
	
	@Test(expected = CommonException.class)
	public void deleteDowntimeAdhocWhitelistApprovalRequestUserNotFoundFailTest() {
		Integer approvalId = 123;
		String userId = "1";
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(null);
		workflowDowntimeAdhocWhitelistService.approveDeleteDowntimeWhitelist(approvalId, approveDeleteDowntimeAdhocWhitelistRequest, userId);
	
	}
	
	@Test(expected = CommonException.class)
	public void deleteDowntimeAdhocWhitelistApprovalApprovalInfoNotFoundFailTest() {
		Integer approvalId = 123;
		String userId = "1";
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		workflowDowntimeAdhocWhitelistService.approveDeleteDowntimeWhitelist(approvalId, approveDeleteDowntimeAdhocWhitelistRequest, userId);
	
	}
	
	@Test(expected = CommonException.class)
	public void deleteDowntimeAdhocWhitelistApprovalBoSmApprovalDowntimeWhitelistNotFoundFailTest() {
		Integer approvalId = 123;
		String userId = "1";
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		when(boSmApprovalDowntimeWhitelistRepositoryMock.findOneByApprovalId(Mockito.anyInt())).thenReturn(null);
		workflowDowntimeAdhocWhitelistService.approveDeleteDowntimeWhitelist(approvalId, approveDeleteDowntimeAdhocWhitelistRequest, userId);
	
	}
	
	@Test(expected = CommonException.class)
	public void deleteDowntimeAdhocWhitelistApprovalUserNotFoundFailTest() {
		Integer approvalId = 123;
		String userId = "1";
		
		when(userRepositoryMock.findNameById(Mockito.anyObject())).thenReturn(userName);
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		when(boSmApprovalDowntimeWhitelistRepositoryMock.findOneByApprovalId(Mockito.anyInt())).thenReturn(boSmApprovalDowntimeWhitelist);
		when(systemDowntimeWhitelistConfigRepositoryMock.findByUserIdAndType(Mockito.anyInt(), Mockito.anyString())).thenReturn(systemDowntimeWhitelistConfigs);
		workflowDowntimeAdhocWhitelistService.approveDeleteDowntimeWhitelist(approvalId, approveDeleteDowntimeAdhocWhitelistRequest, userId);
	
	}
	
}
