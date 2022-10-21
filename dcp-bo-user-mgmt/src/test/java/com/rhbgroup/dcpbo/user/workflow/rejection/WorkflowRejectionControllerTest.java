package com.rhbgroup.dcpbo.user.workflow.rejection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
        WorkflowRejectionControllerTest.class,
        WorkflowRejectionService.class,
        WorkflowRejectionController.class
})
@EnableWebMvc
public class WorkflowRejectionControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean(name = "workflowRejectionService")
    WorkflowRejectionService workflowRejectionService;

    private static final Integer approvalId = 1;

    private static final String requestBody ="{\n" +
            "    \"rejectReason\": \"Incorrect request.\"\n" +
            "}";

    private static final String responseBody = "{\n" +
            "    \"approvalId\": \"1\"\n}";

    @Test
    public void workflowOverviewControllerTest() throws Exception {
        ObjectMapper mapper =  new ObjectMapper();

        WorkflowRejection workflowRejection = new WorkflowRejection();
        workflowRejection.setApprovalId("1");
        RejectReasonRequestBody rejectReasonRequestBody = new RejectReasonRequestBody();
        rejectReasonRequestBody.setRejectReason("Incorrect request.");

        when(workflowRejectionService.putWorkflowRejectionService(eq(approvalId),any(), any())).thenReturn(workflowRejection);


        String url = "/bo/workflow/rejection/" + approvalId;

            mockMvc.perform(MockMvcRequestBuilders.put(url).content(requestBody).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.content().json(mapper.writeValueAsString(workflowRejection)));
}
}
