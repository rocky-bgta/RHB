package com.rhbgroup.dcpbo.user.workflow.overview;

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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
		WorkflowOverviewControllerTest.class,
		WorkflowOverviewService.class,
		WorkflowOverviewController.class
})
@EnableWebMvc
public class WorkflowOverviewControllerTest {
	@Autowired
	MockMvc mockMvc;
	
	@MockBean(name = "workflowOverviewService")
	WorkflowOverviewService workflowOverviewService;

	private static Logger logger = LogManager.getLogger(WorkflowOverviewControllerTest.class);

	private static final String userId = "1";

	@Test
	public void workflowOverviewControllerTest() throws Exception {

		WorkflowOverview workflowOverview = new WorkflowOverview();
		WorkflowOverviewModule workflowOverviewModule = new WorkflowOverviewModule();
		List<WorkflowOverviewModule> workflowOverviewModuleList = new ArrayList<>();
		workflowOverviewModule.setModuleId("1");
		workflowOverviewModule.setModuleName("Create Approval");
		workflowOverviewModuleList.add(workflowOverviewModule);
		workflowOverview.setModule(workflowOverviewModuleList);
		when(workflowOverviewService.getWorkflowOverviewService(Mockito.anyInt())).thenReturn(workflowOverview);
		
		logger.debug("    userId: " + userId);
		logger.error("my object is " + workflowOverviewService);


		String url = "/bo/workflow/overview/user/" ;
		logger.debug("    url: " +  url);

		mockMvc.perform(MockMvcRequestBuilders.get(url).header("userid",userId))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
}
