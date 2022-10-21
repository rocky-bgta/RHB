package com.rhbgroup.dcpbo.user.workflow.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
        WorkflowDeviceControllerTest.class,
        WorkflowDeviceController.class,
        WorkflowDeviceService.class
})
@EnableWebMvc
public class WorkflowDeviceControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    WorkflowDeviceService workflowDeviceService;

    private static final Integer approvalId = 1;

    @Test
    public void getWorkflowFunctionControllerTest() throws Exception{

        WorkflowDeviceRequest workflowDeviceRequest = new WorkflowDeviceRequest();
        WorkflowDeviceResponse workflowDeviceResponse = new WorkflowDeviceResponse();

        workflowDeviceRequest.setReason("Testing Controller");
        workflowDeviceResponse.setApprovalId(approvalId);

        when(workflowDeviceService.approveDeletion(anyString(), anyInt(), anyInt())).thenReturn(workflowDeviceResponse);

        mockMvc.perform(MockMvcRequestBuilders.put("/bo/workflow/device/delete/approval/" + approvalId)
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(workflowDeviceRequest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.comparesEqualTo(approvalId)));
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
