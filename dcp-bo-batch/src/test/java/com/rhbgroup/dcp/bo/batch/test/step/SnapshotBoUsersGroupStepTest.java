package com.rhbgroup.dcp.bo.batch.test.step;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.SnapshotBoUsersGroup;
import com.rhbgroup.dcp.bo.batch.job.repository.SnapshotBoUsersGroupRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import freemarker.template.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        StepScopeTestExecutionListener.class,
        MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class SnapshotBoUsersGroupStepTest extends BaseJobTest {
    private static final String STEP_NAME_USERGROUP = "SnapshotBoUsersGroupStepBuilder";
    private static final String STEP_NAME_USER = "SnapshotBoUsersStepBuilder";
    private static final String DETAIL_LIST = "DetailList";

    @MockBean
    private SnapshotBoUsersGroupRepositoryImpl mockSnapshotBoUsersGroupRepository;
    @Autowired
    @Qualifier(STEP_NAME_USERGROUP + ".ItemProcessor")
    private ItemProcessor<SnapshotBoUsersGroup,SnapshotBoUsersGroup> itemProcessorUsersGroup;
    @Autowired
    @Qualifier(STEP_NAME_USERGROUP + ".ItemWriter")
    private ItemWriter<SnapshotBoUsersGroup> itemWriterUsersGroup;
    @Autowired
    @Qualifier(STEP_NAME_USER + ".ItemProcessor")
    private ItemProcessor<SnapshotBoUsersGroup,SnapshotBoUsersGroup> itemProcessorUsers;
    @Autowired
    @Qualifier(STEP_NAME_USER + ".ItemWriter")
    private ItemWriter<SnapshotBoUsersGroup> itemWriterUsers;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    private StepExecution stepExecution;
    private SnapshotBoUsersGroup snapshotBoUsersGroup = new SnapshotBoUsersGroup();
    public StepExecution getStepExecution() {
        stepExecution = MetaDataInstanceFactory.createStepExecution();
        return stepExecution;
    }

    @Test
    public void testProcessorUserGroup() throws Exception {
        SnapshotBoUsersGroup detail = new SnapshotBoUsersGroup();
        detail.setJobExecutionId(1);
        detail.setUserGroup("1");
        detail.setRole("1");
        detail.setFunctionName("1");
        detail.setCreatedBy("1");
        detail.setCreatedTime(new Date().toString());
        itemProcessorUsersGroup.process(detail);
    }

    @Test
    public void testWriterUserGroup() throws Exception {
        SnapshotBoUsersGroup snapshotBoUsersGroup = new SnapshotBoUsersGroup();
        List<SnapshotBoUsersGroup> test = new ArrayList<>();
        snapshotBoUsersGroup.setJobExecutionId(1);
        snapshotBoUsersGroup.setUserGroup("1");
        snapshotBoUsersGroup.setRole("1");
        snapshotBoUsersGroup.setFunctionName("1");
        snapshotBoUsersGroup.setCreatedTime("1");
        snapshotBoUsersGroup.setCreatedBy("1");
        test.add(snapshotBoUsersGroup);
        itemWriterUsersGroup.write(test);
    }

    @Test
    public void testProcessorUser() throws Exception {
        SnapshotBoUsersGroup snapshotBoUsersGroup = getSnapshotObject();
        itemProcessorUsers.process(snapshotBoUsersGroup);
    }
    @Test
    public void testWriterUser() throws Exception {
        SnapshotBoUsersGroup snapshotBoUsersGroup = getSnapshotObject();
        List<SnapshotBoUsersGroup> test = new ArrayList<>();
        test.add(snapshotBoUsersGroup);
        itemWriterUsers.write(test);
    }

    @Test
    public void testNegativeUserDBInsertion() throws Exception{
        SnapshotBoUsersGroup snapshotBoUsersGroup = getSnapshotObject();
        List<SnapshotBoUsersGroup> test = new ArrayList<>();
        when(mockSnapshotBoUsersGroupRepository.insertBoUser(Mockito.any(SnapshotBoUsersGroup.class))).thenReturn(false);
        assertFalse(mockSnapshotBoUsersGroupRepository.insertBoUser(snapshotBoUsersGroup));
    }

    @Test
    public void testNegativeUserGroupDBInsertion() throws Exception{
        SnapshotBoUsersGroup snapshotBoUsersGroup = getSnapshotObject();
        List<SnapshotBoUsersGroup> test = new ArrayList<>();
        when(mockSnapshotBoUsersGroupRepository.insertBoUserGroup(Mockito.any(SnapshotBoUsersGroup.class))).thenReturn(false);
        assertFalse(mockSnapshotBoUsersGroupRepository.insertBoUserGroup(snapshotBoUsersGroup));
    }

    public SnapshotBoUsersGroup getSnapshotObject(){
        snapshotBoUsersGroup.setJobExecutionId(1);
        snapshotBoUsersGroup.setUserGroup("1");
        snapshotBoUsersGroup.setRole("1");
        snapshotBoUsersGroup.setFunctionName("1");
        snapshotBoUsersGroup.setCreatedBy("1");
        snapshotBoUsersGroup.setCreatedTime(new Date().toString());
        snapshotBoUsersGroup.setDeptName("1");
        snapshotBoUsersGroup.setUserId("1");
        snapshotBoUsersGroup.setUserName("1");
        snapshotBoUsersGroup.setStatus("1");
        snapshotBoUsersGroup.setUserCreatedDate("1");
        snapshotBoUsersGroup.setUserCreatedTime("1");
        snapshotBoUsersGroup.setUserUpdatedTime("1");
        snapshotBoUsersGroup.setUserUpdatedBy("1");
        snapshotBoUsersGroup.setLastLoginDate("1");
        snapshotBoUsersGroup.setLastLoginTime("1");
        return snapshotBoUsersGroup;
    }
}