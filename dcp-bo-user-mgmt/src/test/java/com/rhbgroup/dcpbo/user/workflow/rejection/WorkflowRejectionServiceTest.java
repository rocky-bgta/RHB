package com.rhbgroup.dcpbo.user.workflow.rejection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.model.bo.Approval;
import com.rhbgroup.dcpbo.user.common.ApprovalRepository;
import com.rhbgroup.dcpbo.user.common.BoAuditEventConfigRepository;
import com.rhbgroup.dcpbo.user.enums.ApprovalStatus;
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
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
        WorkflowRejectionServiceTest.class,
        WorkflowRejectionService.class,
        AdditionalDataHolder.class,
        CommonException.class
})
@EnableWebMvc
public class WorkflowRejectionServiceTest {
    @Autowired
    MockMvc mockMvc;

    String successResponse = "{\n" +
            "    \"approvalId\": 1\n" +
            "}";

    String emptyUserGroup = "{\"module\":null}";

    String rejectReason = "Incorrect request.";

    RejectReasonRequestBody rejectReasonRequestBody = new RejectReasonRequestBody();

    @Autowired
    WorkflowRejectionService workflowRejectionService;

    @MockBean
    ApprovalRepository approvalRepository;

    @MockBean
    BoAuditEventConfigRepository boAuditEventConfigRepository;


    private static Logger logger = LogManager.getLogger(WorkflowRejectionControllerTest.class);

    private static final String userId = "1";

    @Test
    public void putWorkflowRejectionTestSuccess() throws Exception {
        Approval approval = new Approval();
        List<Object[]> objects = new ArrayList<>();
        objects.add(new String[]{"1", "M", "Description"});

        rejectReasonRequestBody.setRejectReason(rejectReason);
        when(approvalRepository.updateStatusByIdOutput(eq(ApprovalStatus.REJECTED.getValue()),eq(1), eq(rejectReasonRequestBody.getRejectReason()), any(Timestamp.class), any())).thenReturn(objects);

        ObjectMapper mapper = new ObjectMapper();
        Gson gson = new Gson();
        assertEquals(mapper.writeValueAsString(gson.fromJson(successResponse,WorkflowRejection.class)),
                mapper.writeValueAsString(workflowRejectionService.putWorkflowRejectionService(1,rejectReasonRequestBody, any())));
    }

    @Test(expected = CommonException.class)
    public void getWorkflowOverviewTestFail() throws Exception {
        Approval approval = new Approval();
        rejectReasonRequestBody.setRejectReason(rejectReason);
        when(approvalRepository.updateStatusById(ApprovalStatus.REJECTED.getValue(),1, rejectReason)).thenReturn(0);

        ObjectMapper mapper = new ObjectMapper();
        Gson gson = new Gson();
        assertEquals(mapper.writeValueAsString(gson.fromJson(emptyUserGroup,WorkflowRejection.class)),
                mapper.writeValueAsString(workflowRejectionService.putWorkflowRejectionService(1,rejectReasonRequestBody, any())));
    }
}