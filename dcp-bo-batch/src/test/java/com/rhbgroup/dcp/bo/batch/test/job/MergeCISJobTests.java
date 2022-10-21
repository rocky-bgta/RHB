package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.JobContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.config.MergeCISJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.repository.UserProfileRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.MergeCISValidatorTasklet;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class, MergeCISJobConfiguration.class })
@ActiveProfiles("test")
public class MergeCISJobTests extends BaseFTPJobTest{
	private static final Logger logger = Logger.getLogger(MergeCISJobTests.class);
	public static final String JOB_NAME="MergeCISJob";
	public static final String JOB_LAUNCHER_UTILS="MergeCISJobLauncherTestUtils";

	@Autowired
    private ApplicationContext applicationContext;
	
	@Autowired
	private DcpBatchApplicationContext dcpBatchApplicationContext;
	
	@Autowired
    private DataSource dataSource;
	
	@Lazy
	@Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	String jobprocessdate = "2018-09-04";
	
	@MockBean(name="userProfileRepositoryImpl")
	private UserProfileRepositoryImpl userProfileRepositoryImpl;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testJob() throws Exception {
		logger.info("test executing new job");
        Map<String, String> initialJobArguments = new HashMap<>();
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME);
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate);
        dcpBatchApplicationContext.setInitialJobArguments(initialJobArguments);
        when(userProfileRepositoryImpl.updateUserStatusCISNo("00000001074818", "I")).thenReturn(1);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Mock
	StepExecution stepExecution;
	
	@Mock
	StepContribution contribution;
	
	@Mock
	StepContext stepContext;
	
	@Mock
	JobContext jobContext;
	
	@Mock
	JobExecution jobExecution;
	
	@Mock
	ChunkContext chunkContext;
	
	@Autowired
	private MergeCISValidatorTasklet mergeCISValidatorTasklet;
	
	
	@Test
	public void testRerunTasklet() throws Exception {
		logger.info("test rerun tasklet with input job execution id");
		String jobexecutionid="3127";
        Map<String, String> initialJobArguments = new HashMap<>();
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME);
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate);
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid);
        dcpBatchApplicationContext.setInitialJobArguments(initialJobArguments);
        
        JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid)
				.toJobParameters();
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        Assert.assertEquals(RepeatStatus.FINISHED, mergeCISValidatorTasklet.execute(contribution, chunkContext));
	}
	
	@Autowired
	private MergeCISJobConfiguration mergeCISJobConfiguration;
	
	@Test
	public void testBuildJobWithId() throws Exception{
		logger.info("test build job with input job execution id");
		String jobexecutionid="1000";
        Map<String, String> initialJobArguments = new HashMap<>();
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME);
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate);
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid);
        dcpBatchApplicationContext.setInitialJobArguments(initialJobArguments);
        Assert.assertNotNull(mergeCISJobConfiguration.buildJob());
	}
	
    @Before
    public void setup() throws Exception{
    	logger.info("Starting up...");
        Environment environment = applicationContext.getEnvironment();
		String resourcePath = "ftp/DCP_CISDC_TO/DCPCISDC0904.txt";
		try {
			File testFile = getResourceFile(resourcePath);
			String workingDir = System.getProperty("user.dir");
			String testFileFolderPath = generateFolderPath(workingDir, "target", "DCP_CISDC_TO");
			uploadFileToFTPFolder(testFile, testFileFolderPath);
		}catch(Exception ex) {
            logger.error(ex + " File/Folder not found.");
		}
        logger.info("Started");
    }
    
    
    @After
	public void cleanup() throws Exception {
		logger.info("Cleaning up...");
		jdbcTemplate.setDataSource(dataSource);
		String deleteCIS = "delete from TBL_BATCH_STAGED_MERGE_CIS";
		String deleteBatchSuspense = "delete from TBL_BATCH_SUSPENSE ";
		jdbcTemplate.batchUpdate(deleteCIS, deleteBatchSuspense);
		logger.info("Done cleaning");
	}
}
