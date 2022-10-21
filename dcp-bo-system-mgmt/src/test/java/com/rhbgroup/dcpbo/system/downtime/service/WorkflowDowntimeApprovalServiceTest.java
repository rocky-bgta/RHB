package com.rhbgroup.dcpbo.system.downtime.service;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Timestamp;
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
import com.rhbgroup.dcpbo.system.downtime.repository.ApprovalRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoDowntimeAdhocTypeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BoSmApprovalDowntimeRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.SystemDowntimeConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.UserRepository;
import com.rhbgroup.dcpbo.system.downtime.service.impl.WorkflowDowntimeServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.util.Util;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;
import com.rhbgroup.dcpbo.system.model.Approval;
import com.rhbgroup.dcpbo.system.model.BoSmApprovalDowntime;
import com.rhbgroup.dcpbo.system.model.User;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {WorkflowDowntimeApprovalServiceTest.class, WorkflowDowntimeServiceImpl.class})
public class WorkflowDowntimeApprovalServiceTest {
	
	@Autowired
	WorkflowDowntimeService workflowDowntimeService;
	
	@MockBean
	private SystemDowntimeConfigRepository systemDowntimeConfigRepositoryMock;
	
	@MockBean
	ApprovalRepository approvalRepositoryMock;
	
	@MockBean
	BoSmApprovalDowntimeRepository boSmApprovalDowntimeRepositoryMock;
	
	@MockBean
    BoDowntimeAdhocTypeRepository boDowntimeAdhocTypeRepositoryMock; 

	@MockBean
	UserRepository userRepositoryMock;
	
	@MockBean
    AdditionalDataHolder additionalDataHolder;
	
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

		int approvalId = 123;
		System.out.println("    approvalId: " + approvalId);

		Integer userId = 456789;
		System.out.println("    userId: " + userId);
		
		Approval approval = new Approval();
		approval.setActionType("A");
		approval.setCreatedBy("dcpbo2");
		approval.setCreatedTime(new Timestamp(System.currentTimeMillis() - 100000));
		approval.setUpdatedBy("dcpbo2");
		approval.setUpdatedTime(new Timestamp(System.currentTimeMillis()));
		approval.setReason("This is the reason.");
		approval.setCreatorId(234);
		approval.setStatus("A");
		when(approvalRepositoryMock.findOne(Mockito.anyInt())).thenReturn(approval);
		
		List<BoSmApprovalDowntime> boSmApprovalDowntimeList = new LinkedList<BoSmApprovalDowntime>();
		addToList(boSmApprovalDowntimeList, "WorkflowDowntimeApprovalServiceTest-After.json", "A");
		addToList(boSmApprovalDowntimeList, "WorkflowDowntimeApprovalServiceTest-Before.json", "B");
		when(boSmApprovalDowntimeRepositoryMock.findByApprovalId(Mockito.anyInt())).thenReturn(boSmApprovalDowntimeList);
		
		User user = new User();
		user.setName("DCP BackOffice User 2");
		when(userRepositoryMock.findById(Mockito.anyInt())).thenReturn(user);

		ResponseEntity<BoData> responseEntity = workflowDowntimeService.getApproval(approvalId, userId);
		System.out.println("    responseEntity: " + responseEntity);
	}

	private void addToList(List<BoSmApprovalDowntime> boSmApprovalDowntimeList, String filename, String state) throws IOException {
		BoSmApprovalDowntime boSmApprovalDowntime = new BoSmApprovalDowntime();
		boSmApprovalDowntime.setState(state);

		String jsonStr = Util.loadJsonResourceFile(getClass(), filename);
		boSmApprovalDowntime.setPayload(jsonStr);
		
		boSmApprovalDowntimeList.add(boSmApprovalDowntime);
	}
	
}
