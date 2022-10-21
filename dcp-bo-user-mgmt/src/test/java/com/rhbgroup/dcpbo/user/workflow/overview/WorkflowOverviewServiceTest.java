package com.rhbgroup.dcpbo.user.workflow.overview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.ConfigModule;
import com.rhbgroup.dcpbo.user.config.WorkflowConfig;
import com.rhbgroup.dcpbo.user.enums.ApprovalStatus;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import com.google.gson.Gson;


import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
		WorkflowOverviewServiceTest.class,
		WorkflowOverviewService.class,
})
@EnableWebMvc
public class WorkflowOverviewServiceTest {
	@Autowired
	MockMvc mockMvc;

	String successResponse = "{\n" +
			"\"module\":[\n" +
			"{\n" +
			"\"moduleId\":\"null\",\n" +
			"\"moduleName\":\"\",\n" +
			"\"function\":[\n" +
			"{\n" +
			"\"functionId\":1,\n" +
			"\"functionName\":\"User\",\n" +
			"\"pendingCount\":\"3\",\n" +
			"\"approvedCount\":\"2\",\n" +
			"\"rejectedCount\":\"4\"\n" +
			"},\n" +
			"{\n" +
			"\"functionId\":2,\n" +
			"\"functionName\":\"User Group\",\n" +
			"\"pendingCount\":\"3\",\n" +
			"\"approvedCount\":\"2\",\n" +
			"\"rejectedCount\":\"4\"\n" +
			"},\n" +
			"{\n" +
			"\"functionId\":3,\n" +
			"\"functionName\":\"User user group\",\n" +
			"\"pendingCount\":\"3\",\n" +
			"\"approvedCount\":\"2\",\n" +
			"\"rejectedCount\":\"4\"\n" +
			"}\n" +
			"]\n" +
			"}\n" +
			"]\n" +
			"}";
//	String successResponse = "{\"module\":[{\"moduleId\":\"null\",\"moduleName\":\"\",\"function\":[{\"functionId\":1,\"functionName\":\"User\",\"pendingCount\":\"3\",\"approvedCount\":\"2\",\"rejectedCount\":\"4\"},{\"functionId\":2,\"functionName\":\"User Group\",\"pendingCount\":\"3\",\"approvedCount\":\"2\",\"rejectedCount\":\"4\"}]},{\"moduleId\":\"2\",\"moduleName\":\"User Management\",\"function\":[{\"functionId\":3,\"functionName\":\"User user group\",\"pendingCount\":\"3\",\"approvedCount\":\"2\",\"rejectedCount\":\"4\"}]}]}";

	String emptyUserGroup = "{\"module\":null}";

	@Autowired
	WorkflowOverviewService workflowOverviewService;

	@Autowired
	private WebApplicationContext context;

	@MockBean
	UserUsergroupRepository userUsergroupRepository;

	@MockBean
	UsergroupAccessRepository usergroupAccessRepository;

	@MockBean
	ApprovalRepository approvalRepository;

	@MockBean
	ConfigFunctionRepository configFunctionRepository;

	@MockBean
	ConfigModuleRepository configModuleRepository;

	@MockBean
    WorkflowConfig workflowConfig;

	private static Logger logger = LogManager.getLogger(WorkflowOverviewServiceTest.class);

	private static final String userId = "1";

	@Test
	public void getWorkflowOverviewTestSuccessEmptyUsergroup() throws Exception {

		List<Integer> userGroupIdList = new ArrayList<>();
		userGroupIdList.add(1);

		List<Object[]> usergroupAccessList = new ArrayList<>();
        Object[] usergroupAccess1 = new Object[2];
        Object[] usergroupAccess2 = new Object[2];
        Object[] usergroupAccess3 = new Object[2];
		usergroupAccess1[0]=1;
		usergroupAccess1[1]=1;
		usergroupAccess2[0]=1;
		usergroupAccess2[1]=2;
		usergroupAccess3[0]=2;
		usergroupAccess3[1]=3;

		usergroupAccessList.add(usergroupAccess1);
		usergroupAccessList.add(usergroupAccess2);
		usergroupAccessList.add(usergroupAccess3);
		ConfigFunction configFunction1 = new ConfigFunction();
		ConfigFunction configFunction2 = new ConfigFunction();
		ConfigFunction configFunction3 = new ConfigFunction();
		configFunction1.setFunctionName("User");
		configFunction1.setId(1);
		configFunction2.setFunctionName("User Group");
		configFunction2.setId(2);
		configFunction3.setFunctionName("User user group");
		configFunction3.setId(3);
		ConfigModule configModule = new ConfigModule();
		configModule.setModuleName("User Management");


		when(configModuleRepository.findOne(1)).thenReturn(configModule);
		when(configFunctionRepository.findOne(1)).thenReturn(configFunction1);
		when(configFunctionRepository.findOne(2)).thenReturn(configFunction2);
		when(approvalRepository.findCountByFunctionIdAndStatus(1, ApprovalStatus.PENDING_APPROVAL.getValue())).thenReturn(3);
		when(approvalRepository.findCountByFunctionIdAndStatus(1, ApprovalStatus.APPROVED.getValue())).thenReturn(2);
		when(approvalRepository.findCountByFunctionIdAndStatus(1, ApprovalStatus.REJECTED.getValue())).thenReturn(4);
		when(approvalRepository.findCountByFunctionIdAndStatus(2, ApprovalStatus.PENDING_APPROVAL.getValue())).thenReturn(3);
		when(approvalRepository.findCountByFunctionIdAndStatus(2, ApprovalStatus.APPROVED.getValue())).thenReturn(2);
		when(approvalRepository.findCountByFunctionIdAndStatus(2, ApprovalStatus.REJECTED.getValue())).thenReturn(4);
		when(usergroupAccessRepository.findByUserGroupIdList(userGroupIdList)).thenReturn(usergroupAccessList);
		when(userUsergroupRepository.findUserGroupIdListByUserId(2)).thenReturn(new ArrayList<>());

		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		assertEquals(mapper.writeValueAsString(gson.fromJson(emptyUserGroup,WorkflowOverview.class)),mapper.writeValueAsString(workflowOverviewService.getWorkflowOverviewService(2)));
	}

	@Test
	public void getWorkflowOverviewTestSuccess() throws Exception {
		List<Object[]> objects;

        List<Integer> userGroupIdList = new ArrayList<>();
        userGroupIdList.add(1);
        List<Object[]> usergroupAccessList = new ArrayList<>();
        Object[] usergroupAccess1 = new Object[10];
        Object[] usergroupAccess2 = new Object[10];
        Object[] usergroupAccess3 = new Object[10];
        usergroupAccess1[0]=1;
        usergroupAccess1[1]=1;
        usergroupAccess2[0]=1;
        usergroupAccess2[1]=2;
        usergroupAccess3[0]=2;
        usergroupAccess3[1]=3;
        usergroupAccessList.add(usergroupAccess1);
        usergroupAccessList.add(usergroupAccess2);
        usergroupAccessList.add(usergroupAccess3);

		ConfigFunction configFunction1 = new ConfigFunction();
		ConfigFunction configFunction2 = new ConfigFunction();
		ConfigFunction configFunction3 = new ConfigFunction();
		configFunction1.setFunctionName("User");
		configFunction1.setId(1);
		configFunction2.setFunctionName("User Group");
		configFunction2.setId(2);
		configFunction3.setFunctionName("User user group");
		configFunction3.setId(3);
		ConfigModule configModule = new ConfigModule();
		configModule.setModuleName("User Management");
		ConfigModule configModule2 = new ConfigModule();
		configModule2.setModuleName("User Management");
		Object[] object1p = new Object[2];
		object1p[0] = "P";
		object1p[1] = "3";
		Object[] object1a = new Object[2];
		object1a[0] = "A";
		object1a[1] = "2";
		Object[] object1r = new Object[2];
		object1r[0] = "R";
		object1r[1] = "4";
		List<Object[]> objects1 =  new ArrayList<>();
		objects1.add(object1p);
		objects1.add(object1a);
		objects1.add(object1r);
		Object[] object2p = new Object[2];
		object2p[0] = "P";
		object2p[1] = "3";
		Object[] object2a = new Object[2];
		object2a[0] = "A";
		object2a[1] = "2";
		Object[] object2r = new Object[2];
		object2r[0] = "R";
		object2r[1] = "4";
		List<Object[]> objects2 =  new ArrayList<>();
		objects1.add(object1p);
		objects1.add(object1a);
		objects1.add(object1r);
		objects2.add(object2p);
		objects2.add(object2a);
		objects2.add(object2r);
		Object[] object3p = new Object[2];
		object3p[0] = "P";
		object3p[1] = "3";
		Object[] object3a = new Object[2];
		object3a[0] = "A";
		object3a[1] = "2";
		Object[] object3r = new Object[2];
		object3r[0] = "R";
		object3r[1] = "4";
		List<Object[]> objects3 =  new ArrayList<>();
		objects3.add(object3p);
		objects3.add(object3a);
		objects3.add(object3r);

		when(approvalRepository.findCountByFunctionIdAndStatusNew(1)).thenReturn(objects1);
		when(approvalRepository.findCountByFunctionIdAndStatusNew(2)).thenReturn(objects2);
		when(approvalRepository.findCountByFunctionIdAndStatusNew(3)).thenReturn(objects3);
		when(configModuleRepository.findOne(1)).thenReturn(configModule);
		when(configModuleRepository.findOne(2)).thenReturn(configModule2);
		when(configFunctionRepository.findOne(1)).thenReturn(configFunction1);
		when(configFunctionRepository.findOne(2)).thenReturn(configFunction2);
		when(configFunctionRepository.findOne(3)).thenReturn(configFunction3);
		when(approvalRepository.findCountByFunctionIdAndStatus(1, ApprovalStatus.PENDING_APPROVAL.getValue())).thenReturn(3);
		when(approvalRepository.findCountByFunctionIdAndStatus(1, ApprovalStatus.APPROVED.getValue())).thenReturn(2);
		when(approvalRepository.findCountByFunctionIdAndStatus(1, ApprovalStatus.REJECTED.getValue())).thenReturn(4);
		when(approvalRepository.findCountByFunctionIdAndStatus(2, ApprovalStatus.PENDING_APPROVAL.getValue())).thenReturn(3);
		when(approvalRepository.findCountByFunctionIdAndStatus(2, ApprovalStatus.APPROVED.getValue())).thenReturn(2);
		when(approvalRepository.findCountByFunctionIdAndStatus(2, ApprovalStatus.REJECTED.getValue())).thenReturn(4);
		when(approvalRepository.findCountByFunctionIdAndStatus(3, ApprovalStatus.PENDING_APPROVAL.getValue())).thenReturn(3);
		when(approvalRepository.findCountByFunctionIdAndStatus(3, ApprovalStatus.APPROVED.getValue())).thenReturn(2);
		when(approvalRepository.findCountByFunctionIdAndStatus(3, ApprovalStatus.REJECTED.getValue())).thenReturn(4);
		when(usergroupAccessRepository.findByUserGroupIdList(userGroupIdList)).thenReturn(usergroupAccessList);
 		when(userUsergroupRepository.findUserGroupIdListByUserId(2)).thenReturn(userGroupIdList);

		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		assertEquals(mapper.writeValueAsString(gson.fromJson(successResponse,WorkflowOverview.class)),mapper.writeValueAsString(workflowOverviewService.getWorkflowOverviewService(2)));
	}
}
