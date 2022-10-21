package com.rhbgroup.dcp.bo.batch.test.step;

import java.io.File;
import java.util.*;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.*;
import com.rhbgroup.dcp.bo.batch.job.repository.BranchCodeUpdateRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.BranchCodeUpdateStagingStepBuilder;
import freemarker.template.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineCallbackHandler;
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

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        StepScopeTestExecutionListener.class,
        MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class BranchCodeUpdateStepTest extends BaseJobTest {
    private static final String STEP_NAME = "BranchCodeUpdate";
    private static final String STEP_NAME_STAGING = "BranchCodeUpdateStaging";
    @Autowired
    @Qualifier(STEP_NAME_STAGING + ".ItemReader")
    private FlatFileItemReader<BranchCodeUpdate> itemReaderStaging;
    @Autowired
    @Qualifier(STEP_NAME + ".ItemReader")
    private ItemReader<BranchCodeUpdate> itemReader;
    @Autowired
    @Qualifier(STEP_NAME+ ".ItemProcessor")
    private ItemProcessor<BranchCodeUpdate, BatchSuspense> itemProcessor;
    @Autowired
    @Qualifier(STEP_NAME_STAGING + ".ItemWriter")
    private ItemWriter<BranchCodeUpdate> itemWriter;
    @Autowired
    BranchCodeUpdateStagingStepBuilder branchCodeUpdateStagingStepBuilder;
    private StepExecution stepExecution;
    @Autowired
    DcpBatchApplicationContext dcpBatchApplicationContext;
    @MockBean(name="branchCodeUpdateRepository")
    private BranchCodeUpdateRepositoryImpl branchCodeUpdateRepository;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    @After
	public void cleanup() {
		Mockito.reset(branchCodeUpdateRepository);
	}
    
    public StepExecution getStepExection() {

        String file =  "target/test-classes/ftp/DCP_BRCODE_TO/DCP_BranchCode_070918.txt";
        stepExecution = MetaDataInstanceFactory.createStepExecution();
        stepExecution.getJobExecution().getExecutionContext().putString(
                BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY,
                file);
        return stepExecution;
    }

    @Test
    public void testReaderStaging() throws Exception {
        itemReaderStaging.open(new ExecutionContext());
        itemReaderStaging.read();
    }

    // pass with jobexecutionid
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
    public void testProcessor() throws Exception {
        BranchCodeUpdate detail =null;
        itemProcessor.process(detail);
        detail = new BranchCodeUpdate();
        itemProcessor.process(detail);
        detail.setRhbBranchCode("1");
        detail.setBnmBranchCode("1");
        detail.setCtrl3("1");
        detail.setBnm("1");
        itemProcessor.process(detail);
        detail.setRhbBranchCode("2");
        detail.setBnmBranchCode("2");
        itemProcessor.process(detail);
        detail.setCtrl3("1");
        detail.setBnm("1");
        detail.setRhbBranchCode("2");
        detail.setBnmBranchCode("1");
        itemProcessor.process(detail);
        detail.setRhbBranchCode("");
        itemProcessor.process(detail);
    }

    @Test
    public void testWriterStaging1() throws Exception{
        List<BranchCodeUpdate> test = new ArrayList<>();
        test.add(branchCodeUpdateStagingStepBuilder.checker);
        branchCodeUpdateStagingStepBuilder.checker.setHeaderIsExist(true);
        branchCodeUpdateStagingStepBuilder.checker.setFooterIsExist(true);
        branchCodeUpdateStagingStepBuilder.checker.setFooterRecordCount(30);
        expectedEx.expect(BatchException.class);
        itemWriter.write(test);
    }
    @Test
    public void testWriterStaging2() throws Exception{
        List<BranchCodeUpdate> test = new ArrayList<>();
        test.add(branchCodeUpdateStagingStepBuilder.checker);
        branchCodeUpdateStagingStepBuilder.checker.setHeaderIsExist(false);
        branchCodeUpdateStagingStepBuilder.checker.setFooterIsExist(true);
        expectedEx.expect(BatchException.class);
        itemWriter.write(test);
    }
    @Test
    public void testWriterStaging3() throws Exception{
        List<BranchCodeUpdate> test = new ArrayList<>();
        test.add(branchCodeUpdateStagingStepBuilder.checker);
        branchCodeUpdateStagingStepBuilder.checker.setHeaderIsExist(true);
        branchCodeUpdateStagingStepBuilder.checker.setFooterIsExist(false);
        expectedEx.expect(BatchException.class);
        itemWriter.write(test);

    }
}