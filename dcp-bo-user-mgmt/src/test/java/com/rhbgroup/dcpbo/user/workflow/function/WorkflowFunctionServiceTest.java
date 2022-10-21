package com.rhbgroup.dcpbo.user.workflow.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.rhbgroup.dcpbo.user.common.model.bo.Approval;
import com.rhbgroup.dcpbo.user.common.ApprovalRepository;
import com.rhbgroup.dcpbo.user.common.ConfigFunctionRepository;
import com.rhbgroup.dcpbo.user.common.UserRepository;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
		WorkflowFunctionServiceTest.class,
		WorkflowFunctionService.class,
})
@EnableWebMvc
public class WorkflowFunctionServiceTest {
	@Autowired
	MockMvc mockMvc;



	String successResponse = "{\n" +
			"    \"function\": {\n" +
			"        \"functionId\": 2,\n" +
			"        \"functionName\": \"User\",\n" +
			"        \"workflow\": [{\n" +
			"            \"approvalId\": 1,\n" +
			"            \"description\": \"123456-Muhammad Ali\",\n" +
			"            \"actionType\": \"Add\",\n" +
			"            \"name\": \"Awie\"\n" +
			"        },{\n" +
			"            \"approvalId\": 2,\n" +
			"            \"description\": \"234567-Ultraman\",\n" +
			"            \"actionType\": \"Add\",\n" +
			"            \"name\": \"Dato Vida\"\n" +
			"        }]\n" +
			"    }\n" +
			"} ";

	String successResponseNoStatus = "{\n" +
			"    \"function\": {\n" +
			"        \"functionId\": 2,\n" +
			"        \"functionName\": \"User\",\n" +
			"        \"workflow\": [{\n" +
			"            \"approvalId\": 1,\n" +
			"            \"description\": \"123456-Muhammad Ali\",\n" +
			"            \"actionType\": \"Add\",\n" +
			"            \"name\": \"Awie\"\n" +
			"        },{\n" +
			"            \"approvalId\": 2,\n" +
			"            \"description\": \"234567-Ultraman\",\n" +
			"            \"actionType\": \"Edit\",\n" +
			"            \"name\": \"Dato Vida\"\n" +
			"        }]\n" +
			"    }\n" +
			"} ";

	String emptyUserGroup = "{\"function\":{\"functionId\":\"2\",\"functionName\":\"User\",\"workflow\":[]}}";

	@Autowired
	WorkflowFunctionService workflowFunctionService;

	@Autowired
	private WebApplicationContext context;

	@MockBean
	ApprovalRepository approvalRepository;

	@MockBean
	ConfigFunctionRepository configFunctionRepository;

	@MockBean
	UserRepository userRepository;


	private static Logger logger = LogManager.getLogger(WorkflowFunctionServiceTest.class);

	private static final String userId = "1";

	@Test
	public void getWorkflowFunctionTestSuccessEmptyApproval() throws Exception {

		Approval approval1 = new Approval();
		approval1.setId(1);
		approval1.setDescription("123456-Muhammad Ali");
		approval1.setActionType("Add");
		approval1.setCreatorId(1);
		Approval approval2 = new Approval();
		approval2.setId(2);
		approval2.setDescription("234567-Ultraman");
		approval2.setActionType("Add");
		approval2.setCreatorId(2);
		List<Approval> approvalList = new ArrayList<>();
		approvalList.add(approval1);
		approvalList.add(approval2);


		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setFunctionName("User");
		when(userRepository.findNameById(1)).thenReturn("Awie");
		when(userRepository.findNameById(2)).thenReturn("Dato Vida");
		when(approvalRepository.findByFunctionIdAndStatus(2,"A")).thenReturn(new ArrayList<>());
		when(approvalRepository.findByFunctionIdAndStatus(2,"A")).thenReturn(new ArrayList<>());
		when(configFunctionRepository.findOne(2)).thenReturn(configFunction);


		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		assertEquals(mapper.writeValueAsString(gson.fromJson(emptyUserGroup,WorkflowFunction.class)),mapper.writeValueAsString(workflowFunctionService.getWorkflowFunctionService(2,"A")));
	}

	@Test
	public void getWorkflowFunctionTestSuccess() throws Exception {

		Approval approval1 = new Approval();
		approval1.setId(1);
		approval1.setDescription("123456-Muhammad Ali");
		approval1.setActionType("Add");
		approval1.setCreatorId(1);
		Approval approval2 = new Approval();
		approval2.setId(2);
		approval2.setDescription("234567-Ultraman");
		approval2.setActionType("Add");
		approval2.setCreatorId(2);
		List<Approval> approvalList = new ArrayList<>();
		approvalList.add(approval1);
		approvalList.add(approval2);


		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setFunctionName("User");
		when(userRepository.findNameById(1)).thenReturn("Awie");
		when(userRepository.findNameById(2)).thenReturn("Dato Vida");
		when(approvalRepository.findByFunctionIdAndStatus(2,"A")).thenReturn(approvalList);
		when(configFunctionRepository.findOne(2)).thenReturn(configFunction);


		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		assertEquals(mapper.writeValueAsString(gson.fromJson(successResponse,WorkflowFunction.class)),mapper.writeValueAsString(workflowFunctionService.getWorkflowFunctionService(2,"A")));
	}

	@Test
	public void getWorkflowFunctionTestEmptyStatusSuccess() throws Exception {

		Approval approval1 = new Approval();
		approval1.setId(1);
		approval1.setDescription("123456-Muhammad Ali");
		approval1.setActionType("Add");
		approval1.setCreatorId(1);
		Approval approval2 = new Approval();
		approval2.setId(2);
		approval2.setDescription("234567-Ultraman");
		approval2.setActionType("Edit");
		approval2.setCreatorId(2);
		List<Approval> approvalList = new ArrayList<>();
		approvalList.add(approval1);
		approvalList.add(approval2);


		ConfigFunction configFunction = new ConfigFunction();
		configFunction.setFunctionName("User");
		when(userRepository.findNameById(1)).thenReturn("Awie");
		when(userRepository.findNameById(2)).thenReturn("Dato Vida");
		when(approvalRepository.findByFunctionId(2)).thenReturn(approvalList);

		when(configFunctionRepository.findOne(2)).thenReturn(configFunction);


		ObjectMapper mapper = new ObjectMapper();
		Gson gson = new Gson();
		assertEquals(mapper.writeValueAsString(gson.fromJson(successResponseNoStatus,WorkflowFunction.class)),mapper.writeValueAsString(workflowFunctionService.getWorkflowFunctionService(2,"")));
	}

}
