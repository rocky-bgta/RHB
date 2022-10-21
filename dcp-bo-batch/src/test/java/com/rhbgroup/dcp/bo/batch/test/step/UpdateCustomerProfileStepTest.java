package com.rhbgroup.dcp.bo.batch.test.step;

import java.text.SimpleDateFormat;
import java.util.*;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.model.*;
import com.rhbgroup.dcp.bo.batch.job.repository.UpdateCustomerProfileRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.UpdateCustomerProfileStepBuilder;
import freemarker.template.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
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
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        StepScopeTestExecutionListener.class,
        MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class UpdateCustomerProfileStepTest extends BaseJobTest {
    private static final String STEP_NAME = "UpdateCustomerProfile";
    private static final String STEP_NAME_STAGING = "UpdateCustomerProfileStaging";

    @Autowired
    @Qualifier(STEP_NAME + ".ItemReader")
    private ItemReader<NewOldCustomerProfile> itemReader;
    @Autowired
    @Qualifier(STEP_NAME+ ".ItemProcessor")
    private ItemProcessor<NewOldCustomerProfile, BatchSuspense> itemProcessor;
    @Autowired
    @Qualifier(STEP_NAME_STAGING+ ".ItemProcessor")
    private ItemProcessor<UpdateCustomerProfile,UpdateCustomerProfile> itemProcessorStaging;
    @Autowired
    UpdateCustomerProfileStepBuilder updateCustomerProfileStepBuilder;
    private StepExecution stepExecution;
    @MockBean(name="updateCustomerProfileRepository")
    private UpdateCustomerProfileRepositoryImpl updateCustomerProfileRepository;
    ArrayList<BatchLookup> lookups = new ArrayList<>();
    BatchLookup lookup = new BatchLookup();
    @Autowired
    DcpBatchApplicationContext dcpBatchApplicationContext;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    @After
	public void cleanup() {
		Mockito.reset(updateCustomerProfileRepository);
	}
    
    public StepExecution getStepExecution() {
        stepExecution = MetaDataInstanceFactory.createStepExecution();

        lookup.setGroup("DCP_CUSTOMER_IS_STAFF");
        lookup.setValue("Y");
        lookups.add(lookup);
        stepExecution.getJobExecution().getExecutionContext().put("batchLookup",lookups);
        return stepExecution;
    }

    // Pass with jobexecutiveid
    @Test
    public void testReader1() throws Exception {

        Map<String, String> initialArguments = new HashMap<String, String>();
        String jobExecutionId = "111";
        initialArguments.put(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobExecutionId);
        dcpBatchApplicationContext.setInitialJobArguments(initialArguments);
        itemReader.read();
    }

    // Pass null initialArguments
    @Test
    public void testReader2() throws Exception {
        Map<String, String> initialArguments = new HashMap<String, String>();
        dcpBatchApplicationContext.setInitialJobArguments(initialArguments);
        itemReader.read();
    }

    @Test
    public void testProcessor1() throws Exception {
        when(updateCustomerProfileRepository.addSuspense(Mockito.any(StepExecution.class),Mockito.any(BatchSuspense.class))).thenReturn(true);
        when(updateCustomerProfileRepository.updateIsProcessed(Mockito.anyString(),Mockito.anyString(),Mockito.anyBoolean())).thenReturn(true);
        NewOldCustomerProfile detail = new NewOldCustomerProfile();
        itemProcessor.process(detail);
    }
    @Test
    public void testProcessor2() throws Exception {
        when(updateCustomerProfileRepository.addSuspense(Mockito.any(StepExecution.class),Mockito.any(BatchSuspense.class))).thenReturn(true);
        when(updateCustomerProfileRepository.updateIsProcessed(Mockito.anyString(),Mockito.anyString(),Mockito.anyBoolean())).thenReturn(true);
        NewOldCustomerProfile detail = new NewOldCustomerProfile();
        detail.setNew_cis_no("1");
        detail.setNew_is_staff("Y");
        itemProcessor.process(detail);
    }
    @Test
    public void testProcessor3() throws Exception {
        when(updateCustomerProfileRepository.addSuspense(Mockito.any(StepExecution.class),Mockito.any(BatchSuspense.class))).thenReturn(true);
        when(updateCustomerProfileRepository.updateIsProcessed(Mockito.anyString(),Mockito.anyString(),Mockito.anyBoolean())).thenReturn(true);
        NewOldCustomerProfile detail = new NewOldCustomerProfile();
        detail.setNew_cis_no("1");
        detail.setNew_is_staff("Y");
        detail.setOld_is_staff("Y");
        detail.setNew_birth_date("2018-01-01");
        detail.setOld_birth_date("2018-01-01");
        itemProcessor.process(detail);
    }
    // fail isStaff
    @Test
    public void testProcessor4() throws Exception {
        when(updateCustomerProfileRepository.addSuspense(Mockito.any(StepExecution.class),Mockito.any(BatchSuspense.class))).thenReturn(true);
        when(updateCustomerProfileRepository.updateIsProcessed(Mockito.anyString(),Mockito.anyString(),Mockito.anyBoolean())).thenReturn(true);
        NewOldCustomerProfile detail = new NewOldCustomerProfile();
        detail.setNew_cis_no("1");
        detail.setNew_is_staff("Y");
        detail.setOld_is_staff("N");
        detail.setNew_birth_date("2018-01-01");
        detail.setOld_birth_date("2018-01-01");
        itemProcessor.process(detail);
    }
    // fail date
    @Test
    public void testProcessor5() throws Exception {
        when(updateCustomerProfileRepository.addSuspense(Mockito.any(StepExecution.class),Mockito.any(BatchSuspense.class))).thenReturn(true);
        when(updateCustomerProfileRepository.updateIsProcessed(Mockito.anyString(),Mockito.anyString(),Mockito.anyBoolean())).thenReturn(true);
        NewOldCustomerProfile detail = new NewOldCustomerProfile();
        detail.setNew_cis_no("1");
        detail.setNew_is_staff("Y");
        detail.setOld_is_staff("Y");
        detail.setNew_birth_date("2018-01-01");
        detail.setOld_birth_date("2018-01-02");
        itemProcessor.process(detail);
    }
    // fail both
    @Test
    public void testProcessor6() throws Exception {
        when(updateCustomerProfileRepository.addSuspense(Mockito.any(StepExecution.class),Mockito.any(BatchSuspense.class))).thenReturn(true);
        when(updateCustomerProfileRepository.updateIsProcessed(Mockito.anyString(),Mockito.anyString(),Mockito.anyBoolean())).thenReturn(true);
        NewOldCustomerProfile detail = new NewOldCustomerProfile();
        detail.setNew_cis_no("1");
        detail.setNew_is_staff("Y");
        detail.setOld_is_staff("N");
        detail.setNew_birth_date("2018-01-01");
        detail.setOld_birth_date("2018-01-02");
        itemProcessor.process(detail);
    }
    // fail date validation
    @Test
    public void testProcessor7() throws Exception {
        when(updateCustomerProfileRepository.addSuspense(Mockito.any(StepExecution.class),Mockito.any(BatchSuspense.class))).thenReturn(true);
        when(updateCustomerProfileRepository.updateIsProcessed(Mockito.anyString(),Mockito.anyString(),Mockito.anyBoolean())).thenReturn(true);
        NewOldCustomerProfile detail = new NewOldCustomerProfile();
        detail.setNew_cis_no("1");
        detail.setNew_is_staff("Y");
        detail.setOld_is_staff("N");
        detail.setNew_birth_date("test");
        detail.setOld_birth_date("test");
        itemProcessor.process(detail);
    }
    // fail isStaff validation
    @Test
    public void testProcessor8() throws Exception {
        when(updateCustomerProfileRepository.addSuspense(Mockito.any(StepExecution.class),Mockito.any(BatchSuspense.class))).thenReturn(true);
        when(updateCustomerProfileRepository.updateIsProcessed(Mockito.anyString(),Mockito.anyString(),Mockito.anyBoolean())).thenReturn(true);
        NewOldCustomerProfile detail = new NewOldCustomerProfile();
        detail.setNew_cis_no("1");
        detail.setNew_is_staff("N");
        detail.setOld_is_staff("N");
        detail.setNew_birth_date("2018-01-01");
        detail.setOld_birth_date("2018-01-02");
        itemProcessor.process(detail);
    }
    // fail isStaff group validation
    @Test
    public void testProcessor9() throws Exception {
        when(updateCustomerProfileRepository.addSuspense(Mockito.any(StepExecution.class),Mockito.any(BatchSuspense.class))).thenReturn(true);
        when(updateCustomerProfileRepository.updateIsProcessed(Mockito.anyString(),Mockito.anyString(),Mockito.anyBoolean())).thenReturn(true);
        NewOldCustomerProfile detail = new NewOldCustomerProfile();
        detail.setNew_cis_no("1");
        detail.setNew_is_staff("N");
        detail.setOld_is_staff("N");
        detail.setNew_birth_date("2018-01-01");
        detail.setOld_birth_date("2018-01-02");
        lookup.setGroup("test");
        lookup.setValue("Y");
        lookups.add(lookup);
        stepExecution.getJobExecution().getExecutionContext().put("batchLookup",lookups);
        itemProcessor.process(detail);
    }

    @Test
    public void testProcessorStaging() throws Exception {

        UpdateCustomerProfile detail = new UpdateCustomerProfile();
        itemProcessorStaging.process(detail);
        detail.setBirthDate("1111-11-111");
        itemProcessorStaging.process(detail);
        detail.setBirthDate("1111-11-1111");
        itemProcessorStaging.process(detail);

    }
}