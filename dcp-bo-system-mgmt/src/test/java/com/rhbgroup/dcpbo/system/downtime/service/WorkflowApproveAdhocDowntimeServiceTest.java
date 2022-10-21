package com.rhbgroup.dcpbo.system.downtime.service;

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.common.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.common.exception.ConfigErrorInterface;
import com.rhbgroup.dcpbo.common.exception.ErrorResponse;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.ApprovalDowntimeAdhoc;
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoDowntimeAdhocTypeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoSmApprovalDowntimeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.SystemDowntimeConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.service.impl.WorkflowDowntimeServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.util.Util;
import com.rhbgroup.dcpbo.system.downtime.vo.ApproveDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;
import com.rhbgroup.dcpbo.system.model.Approval;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntime;
import com.rhbgroup.dcpbo.system.model.SystemDowntimeConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {WorkflowApproveAdhocDowntimeServiceTest.class, WorkflowDowntimeServiceImpl.class})
public class WorkflowApproveAdhocDowntimeServiceTest {
	
	@Autowired
	WorkflowDowntimeService workflowDowntimeService;
	
	@MockBean
	ApprovalRepository approvalRepositoryMock;
	
	@MockBean
	BoSmApprovalDowntimeRepository boSmApprovalDowntimeRepositoryMock;

	@MockBean
	SystemDowntimeConfigRepository systemDowntimeConfigRepositoryMock;
	
	@MockBean
    BoDowntimeAdhocTypeRepository boDowntimeAdhocTypeRepositoryMock; 

	@MockBean
	UserRepository userRepositoryMock;

    @MockBean
    AdditionalDataHolder additionalDataHolderMock;
	
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
	
	@Test
	public void serviceTest() throws Exception {
		
		System.out.println("serviceTest()");
		String adhocType = "DuitNow_QR";
		String adhocTypeName = "DuitNow QR";

		int userId = 789;
		String userName = "Mohd Ikhwan Haris";
		when(userRepositoryMock.findNameById(userId)).thenReturn(userName);
		
		int creatorId = 456;
		String creatorName = "Aisyah Nabilah";
		when(userRepositoryMock.findNameById(creatorId)).thenReturn(creatorName);

		int approvalId = 123;
		System.out.println("    approvalId: " + approvalId);
		
		Approval approval = new Approval();
		approval.setId(approvalId);
		approval.setActionType("Add");
		approval.setCreatorId(creatorId);
		approval.setCreatedBy(creatorName);
		approval.setCreatedTime(Util.toTimestamp("2019-04-01T09:00:00+08:00"));
		approval.setDescription("This is the description");
		approval.setFunctionId(89);
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);

		String payload = Util.loadJsonResourceFile(getClass(), "WorkflowDowntimeAdhocApprovalServiceTest.json");
		System.out.println("    payload: " + payload);
		
		BoSmApprovalDowntime boSmApprovalDowntime = new BoSmApprovalDowntime();
		boSmApprovalDowntime.setApprovalId(approvalId);
		boSmApprovalDowntime.setPayload(payload);
		System.out.println("    boSmApprovalDowntime: " + boSmApprovalDowntime);

		List<BoSmApprovalDowntime> boSmApprovalDowntimeList = new LinkedList<BoSmApprovalDowntime>();
		boSmApprovalDowntimeList.add(boSmApprovalDowntime);
		System.out.println("    boSmApprovalDowntimeList: " + boSmApprovalDowntimeList);
		when(boSmApprovalDowntimeRepositoryMock.findByApprovalId(Mockito.anyInt())).thenReturn(boSmApprovalDowntimeList);
		
		SystemDowntimeConfig systemDowntimeConfig = new SystemDowntimeConfig();
		systemDowntimeConfig.setStartTime(Util.toTimestamp("2019-04-07T01:00:00+08:00"));
		systemDowntimeConfig.setEndTime(Util.toTimestamp("2019-04-07T02:00:00+08:00"));
		systemDowntimeConfig.setIsActive("1");

		List<SystemDowntimeConfig> systemDowntimeConfigList = new LinkedList<SystemDowntimeConfig>();
		systemDowntimeConfigList.add(systemDowntimeConfig);
		when(systemDowntimeConfigRepositoryMock.findByStartTimeEndTimeAndAdhocType(Mockito.any(), Mockito.any(), Mockito.anyObject())).thenReturn(systemDowntimeConfigList);

		HashMap<String, Object> additionalDataMap = new HashMap<String, Object>();
		HashMap<String, Object> additionalSubDataMap = new HashMap<String, Object>();
		additionalSubDataMap.put("id", approvalId);
		additionalSubDataMap.put("name", "This is the downtime name");
		additionalSubDataMap.put("startTime", systemDowntimeConfig.getStartTime());
		additionalSubDataMap.put("endTime", systemDowntimeConfig.getEndTime());
		additionalSubDataMap.put("isPushNotification", false);
		additionalSubDataMap.put("pushDate", null);
		additionalSubDataMap.put("type", "ADHOC");
		additionalSubDataMap.put("adhocType", "Add");
		additionalDataMap.put("after", additionalSubDataMap);
		when(additionalDataHolderMock.getMap()).thenReturn(additionalDataMap);
		
		ApproveDowntimeAdhocRequestVo request = new ApproveDowntimeAdhocRequestVo();
		request.setReason("This is the reason.");
		
		ResponseEntity<BoData> responseEntity = workflowDowntimeService.workflowApproveAdhocDowntime(approvalId, request, "" + userId);
		System.out.println("    responseEntity: " + responseEntity);
	}
	
}
