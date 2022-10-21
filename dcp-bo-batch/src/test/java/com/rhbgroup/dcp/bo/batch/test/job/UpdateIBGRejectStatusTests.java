package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.config.UpdateIBGRejectStatusJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.model.IBGRejectStatusTblTransferTxn;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBGRejectTxnRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.IBGRejectStatusTblTransferTxnRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.IBGRejectStatusValidatorTasklet;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfigHSQL.class,UpdateIBGRejectStatusJobConfiguration.class})
@ActiveProfiles("test")
public class UpdateIBGRejectStatusTests extends BaseFTPJobTest {
	
	private static final Logger logger = Logger.getLogger(UpdateIBGRejectStatusTests.class);

	String JOB_NAME="UpdateIBGRejectedStatusJob";
	String JOB_EXEC_ID="";
	String SUCCESS_RUN_WINDOW="w1";
	String FAILED_RUN_WINDOW="w2";
	String jobprocessdate = "2018-08-25";
	
	@Autowired
	DataSource dataSource;
	
	@Autowired
    private ApplicationContext applicationContext;
	
	@Autowired
	private DcpBatchApplicationContext dcpBatchApplicationContext;
	
	@Autowired
	private UpdateIBGRejectStatusJobConfiguration updateIBGRejectStatusJobConfiguration;
	
	@MockBean(name ="tblTransferTxnRepoImpl")
	private IBGRejectStatusTblTransferTxnRepositoryImpl mockTransferTxnRepoImpl;
	
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
	private IBGRejectStatusValidatorTasklet ibgRejectStatusValidatorTasklet;
	
	@Autowired
	@Qualifier("dataSourceDCP")
	private DataSource dataSourceDCP;

	@Lazy
    @Autowired
    @Qualifier("UpdateIBGRejectStatusJobLauncherTestUtils")
    private JobLauncherTestUtils jobLauncherTestUtils;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	String workingDir = System.getProperty("user.dir");
    String testFileFolderPath = generateFolderPath(workingDir, "target", "dcp_ibgrej_to");

	@Test
	public void testJob() throws Exception {
        String resourcePathSuccess = "ftp/dcp_ibgrej_to/DCP_IBGSta_W1_250818.txt";
        File testFileSuccess = getResourceFile(resourcePathSuccess);
        uploadFileToFTPFolder(testFileSuccess, testFileFolderPath);
        String tellerId="123456";
        String traceId="5887";
        int userId=1;
        String txnStatus="FAIL";
        String txnDate="20180825";

        IBGRejectStatusTblTransferTxn transferTxn = new IBGRejectStatusTblTransferTxn();
        transferTxn.setTellerId(tellerId);
        transferTxn.setTraceId(traceId);
        transferTxn.setUserId(userId);
        when(mockTransferTxnRepoImpl.getUserId(txnDate, tellerId , traceId)).thenReturn(transferTxn);
        when(mockTransferTxnRepoImpl.updateTxnStatus(txnStatus, txnDate, tellerId , traceId)).thenReturn(1);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, SUCCESS_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobW1Postcut() throws Exception {
        String resourcePathSuccess = "ftp/dcp_ibgrej_to/DCP_IBGSta_W1_110219.txt";
        File testFileSuccess = getResourceFile(resourcePathSuccess);
        uploadFileToFTPFolder(testFileSuccess, testFileFolderPath);
        jobprocessdate = "2019-02-11";
        String tellerId="808783";
        String traceId="1802";
        int userId=1;
        String txnStatus="FAIL";
        String txnDate="20190210";

        IBGRejectStatusTblTransferTxn transferTxn = new IBGRejectStatusTblTransferTxn();
        transferTxn.setTellerId(tellerId);
        transferTxn.setTraceId(traceId);
        transferTxn.setUserId(userId);
        when(mockTransferTxnRepoImpl.getUserId(txnDate, tellerId , traceId)).thenReturn(transferTxn);
        when(mockTransferTxnRepoImpl.updateTxnStatus(txnStatus, txnDate, tellerId , traceId)).thenReturn(1);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, SUCCESS_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobW3Postcut() throws Exception {
        String resourcePathSuccess = "ftp/dcp_ibgrej_to/DCP_IBGSta_W3_110219.txt";
        File testFileSuccess = getResourceFile(resourcePathSuccess);
        uploadFileToFTPFolder(testFileSuccess, testFileFolderPath);
        jobprocessdate = "2019-02-11";
        String tellerId="808792";
        String traceId="1805";
        int userId=1;
        String txnStatus="FAIL";
        String txnDate="20190210";

        IBGRejectStatusTblTransferTxn transferTxn = new IBGRejectStatusTblTransferTxn();
        transferTxn.setTellerId(tellerId);
        transferTxn.setTraceId(traceId);
        transferTxn.setUserId(userId);
        when(mockTransferTxnRepoImpl.getUserId(txnDate, tellerId , traceId)).thenReturn(transferTxn);
        when(mockTransferTxnRepoImpl.updateTxnStatus(txnStatus, txnDate, tellerId , traceId)).thenReturn(1);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, "W3")
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobW4Postcut() throws Exception {
        String resourcePathSuccess = "ftp/dcp_ibgrej_to/DCP_IBGSta_W4_110219.txt";
        File testFileSuccess = getResourceFile(resourcePathSuccess);
        uploadFileToFTPFolder(testFileSuccess, testFileFolderPath);
        jobprocessdate = "2019-02-11";
        String tellerId="808794";
        String traceId="1811";
        int userId=1;
        String txnStatus="FAIL";
        String txnDate="20190210";

        IBGRejectStatusTblTransferTxn transferTxn = new IBGRejectStatusTblTransferTxn();
        transferTxn.setTellerId(tellerId);
        transferTxn.setTraceId(traceId);
        transferTxn.setUserId(userId);
        when(mockTransferTxnRepoImpl.getUserId(txnDate, tellerId , traceId)).thenReturn(transferTxn);
        when(mockTransferTxnRepoImpl.updateTxnStatus(txnStatus, txnDate, tellerId , traceId)).thenReturn(1);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, "W4")
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobW5Postcut() throws Exception {
        String resourcePathSuccess = "ftp/dcp_ibgrej_to/DCP_IBGSta_W5_110219.txt";
        File testFileSuccess = getResourceFile(resourcePathSuccess);
        uploadFileToFTPFolder(testFileSuccess, testFileFolderPath);
        jobprocessdate = "2019-02-11";
        String tellerId="808796";
        String traceId="1836";
        int userId=1;
        String txnStatus="FAIL";
        String txnDate="20190210";

        IBGRejectStatusTblTransferTxn transferTxn = new IBGRejectStatusTblTransferTxn();
        transferTxn.setTellerId(tellerId);
        transferTxn.setTraceId(traceId);
        transferTxn.setUserId(userId);
        when(mockTransferTxnRepoImpl.getUserId(txnDate, tellerId , traceId)).thenReturn(transferTxn);
        when(mockTransferTxnRepoImpl.updateTxnStatus(txnStatus, txnDate, tellerId , traceId)).thenReturn(1);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, "W5")
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}

	@Test
	public void testFailedJob() throws Exception {
        String resourcePathFailed = "ftp/dcp_ibgrej_to/DCP_IBGSta_W2_250818.txt";
        File testFileFailed = getResourceFile(resourcePathFailed);
        uploadFileToFTPFolder(testFileFailed, testFileFolderPath);
        
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, FAILED_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testBuildJobId() throws Exception{
		String jobexecutionid="9999";
        Map<String, String> initialJobArguments = new HashMap<>();
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME);
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate);
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid);
		dcpBatchApplicationContext.setInitialJobArguments(initialJobArguments);
		Assert.assertNotNull(updateIBGRejectStatusJobConfiguration.buildJob());
	}
	
	@Test
	public void testRerunTaskId() throws Exception{
		String jobexecutionid="999999";
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_IBG_REJECT_STATUS_RUN_WINDOW_PARAM, SUCCESS_RUN_WINDOW)
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.addString(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY, jobexecutionid)
				.toJobParameters();
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(chunkContext.getStepContext().getStepExecution()).thenReturn(stepExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution()).thenReturn(jobExecution);
        when(chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()).thenReturn(jobParameters);
        Assert.assertEquals(RepeatStatus.FINISHED, ibgRejectStatusValidatorTasklet.execute(contribution, chunkContext));
	}
	
    @Before
    public void setup() throws Exception {
        Environment environment = applicationContext.getEnvironment();
        System.out.println("=====================spring.batch.table-prefix:"+environment.getProperty("spring.batch.table-prefix"));
        jdbcTemplate.setDataSource(dataSource);
        insertRejectCode();
    }

    @After
    public void after() throws Exception {
    	deleteRejectCode();        
    }
    
	private void insertRejectCode() {
		String sql1 = "insert into TBL_BATCH_IBG_REJECT_CODE " + "(REJECT_CODE, REJECT_DESCRIPTION) " + "values "
				+ "('R03','REJECT TEST DESC')";
		String sql2 = "insert into TBL_BATCH_IBG_REJECT_CODE " + "(REJECT_CODE, REJECT_DESCRIPTION) " + "values "
				+ "('R04','INVALID ACCT NUMBER')";
		int [] row = jdbcTemplate.batchUpdate(sql1,sql2);
		logger.info(String.format("insert %s row into TBL_BATCH_IBG_REJECT_CODE", row.length));
	}
	
	private void deleteRejectCode() {
		int row = 0;
		String sql = "delete from TBL_BATCH_IBG_REJECT_CODE" ;
		row = jdbcTemplate.update(sql);
		logger.info(String.format("delete %s row into TBL_BATCH_IBG_REJECT_CODE", row));
	}
}
