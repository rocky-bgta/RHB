package com.rhbgroup.dcpbo.user.workflow.function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
		WorkflowFunctionControllerTest.class,
		WorkflowFunctionService.class,
		WorkflowFunctionController.class
})
@EnableWebMvc
public class WorkflowFunctionControllerTest {
	@Autowired
	MockMvc mockMvc;
	
	@MockBean(name = "workflowFunctionService")
	WorkflowFunctionService workflowFunctionService;

	private static Logger logger = LogManager.getLogger(WorkflowFunctionControllerTest.class);

	private static final String functionId = "1";
	private static final String successPayload ="{\"function\":{\"functionId\":\"1\",\"functionName\":\"User\",\"workflow\":null}}";

	@Test
	public void getWorkflowFunctionControllerTest() throws Exception {

		WorkflowFunction workflowFunction = new WorkflowFunction();
		WorkflowFunctionFunction workflowFunctionFunction = new WorkflowFunctionFunction();
		workflowFunctionFunction.setFunctionId("1");
		workflowFunctionFunction.setFunctionName("User");
		workflowFunction.setFunction(workflowFunctionFunction);
		when(workflowFunctionService.getWorkflowFunctionService(Mockito.anyInt(),anyString())).thenReturn(workflowFunction);

		String url = "/bo/workflow/function/" + functionId ;
		logger.debug("    url: " +  url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.content().json(successPayload));
	}
}
