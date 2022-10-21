package com.rhbgroup.dcp.bo.batch.test.step;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.PremierCustomerInfoandRMCodeTaggingDetail;
import com.rhbgroup.dcp.bo.batch.job.step.PremierCustomerInfoandRMCodeTaggingJobTruncateStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooterStepBuilder;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.JobContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.sql.DataSource;
import java.nio.file.Paths;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.*;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class PremierCustomerInfoandRMCodeTaggingStepTest {
	
	private static final Logger logger = Logger.getLogger(PremierCustomerInfoandRMCodeTaggingStepTest.class);
	private static final String JOB_NAME = "PremierCustomerInfoandRMCodeTaggingJob";

	@Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
	private String inputFolderFullPath;

	private String jobprocessdate = "2018-08-13";

	private String jobprocessdatefile = "";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private FTPConfigProperties ftpConfigProperties;

	@Autowired
	@Lazy
	private PremierCustomerInfoandRMCodeTaggingJobTruncateStagingStepBuilder premierCustomerInfoandRMCodeTaggingJobTruncateStagingStepBuilder;

	@Autowired
	@Lazy
	private PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooterStepBuilder premierCustomerInfoandRMCodeTaggingJobValidateHeaderFooterStepBuilder;

	@Autowired
	@Qualifier("PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooter.ItemReader")
	private ItemReader<PremierCustomerInfoandRMCodeTaggingDetail> itemReader;
	
	@Autowired
	@Qualifier("PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooter.ItemProcessor")
	private ItemProcessor<PremierCustomerInfoandRMCodeTaggingDetail, PremierCustomerInfoandRMCodeTaggingDetail> itemProcessor;
	
    @Autowired
    @Qualifier("PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooter.ItemWriter")
    private ItemWriter<PremierCustomerInfoandRMCodeTaggingDetail> itemWriter;

	@Autowired
	private DataSource dataSource;

	@Mock
	private StepContribution contribution;

	@Mock
	private StepContext stepContext;

	@Mock
	private JobContext jobContext;

	@Mock
	private JobExecution jobExecution;

	@Mock
	private ChunkContext chunkContext;

	@Mock
	private ExecutionContext executionContext;

	@Mock
	private StepExecution stepExecution;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	@Test
	public  void testPremierCustomerInfoandRMCodeTaggingJobTruncateStagingBuildStep() throws Exception {
		assertNotNull(premierCustomerInfoandRMCodeTaggingJobTruncateStagingStepBuilder.buildStep());
	}

	private StepExecution getStepException() throws Exception{
		StepExecution execution = MetaDataInstanceFactory.createStepExecution();

		String sourceFileNewName = "DCP_RMCODE_D-"+jobprocessdatefile+".txt";

		String jobexecutionid="999999";

		execution.getExecutionContext().putString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME);
		execution.getExecutionContext().putString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate);
		execution.getExecutionContext().putString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid);
		execution.getExecutionContext().putString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY,Paths.get(inputFolderFullPath,JOB_NAME,sourceFileNewName).toAbsolutePath().toString());

		return execution;
	}

	@Test
	public void testPremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooterWriterTest() throws Exception{
		assertNotNull(premierCustomerInfoandRMCodeTaggingJobValidateHeaderFooterStepBuilder.premierCustomerInfoandRMCodeTaggingJobWriter(stepExecution));
	}

	@Test
	public void testPremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooterProcessTest() throws Exception {

		PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = createData();

		StepScopeTestUtils.doInStepScope(stepExecution, () -> {
			itemProcessor.process(premierCustomerInfoandRMCodeTaggingDetail);
			return null;
		});
	}

	// New record
	private PremierCustomerInfoandRMCodeTaggingDetail createData() {
		PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = new PremierCustomerInfoandRMCodeTaggingDetail();
		premierCustomerInfoandRMCodeTaggingDetail.setJobExecutionId("999999");
		premierCustomerInfoandRMCodeTaggingDetail.setCifNo("90000000000999");

		return premierCustomerInfoandRMCodeTaggingDetail;
	}
}
