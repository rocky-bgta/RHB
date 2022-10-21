package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.config.LoadIBKJompayEmatchJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class, LoadIBKJompayEmatchJobConfiguration.class })
@ActiveProfiles("test")
public class LoadIBKJompayEmatchJobTest extends BaseFTPJobTest {
	
	private static final Logger logger = Logger.getLogger(LoadIBKJompayEmatchJobTest.class);
	public static final String JOB_NAME="LoadIBKJompayEmatchJob";
	public static final String JOB_LAUNCHER_UTILS="LoadIBKJompayEmatchJobLauncherUtils";

	@Autowired
	LoadIBKJompayEmatchJobConfiguration loadIBKJompay;
	
	
	@Lazy
	@Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
	private JobLauncherTestUtils jobLauncherTestUtils;
	

    @Autowired
    DcpBatchApplicationContext dcpBatchApplicationContext;
    
	@Autowired
	JdbcTemplate jdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	String jobProcessDate="";
    private Map<String,String> initialArguments;

	@Test
	public void testValidJob() throws Exception {
		logger.info("test valid job");
		String batchSystemDate="2018-10-06";
		String validResourcePath = "ftp/ibk_ematch_to/ibk_nbps_20181005.txt";
		updateBatchConfigValid(batchSystemDate);
		uploadTestFile(validResourcePath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testInvalidCount() throws Exception {
		logger.info("test wrong total count");
		String batchSystemDate="2018-10-07";
		String invalidCountResourcePath = "ftp/ibk_ematch_to/ibk_nbps_20181006.txt";
		updateBatchConfigValid(batchSystemDate);
		uploadTestFile(invalidCountResourcePath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testInvalidDebit() throws Exception {
		logger.info("test wrong total debit");
		String batchSystemDate="2018-10-08";
		String invalidDebitResourcePath = "ftp/ibk_ematch_to/ibk_nbps_20181007.txt";
		updateBatchConfigValid(batchSystemDate);
		uploadTestFile(invalidDebitResourcePath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testInvalidHeader() throws Exception {
		logger.info("test invalid header");
		String batchSystemDate="2018-10-09";
		String invalidDebitResourcePath = "ftp/ibk_ematch_to/ibk_nbps_20181008.txt";
		updateBatchConfigValid(batchSystemDate);
		uploadTestFile(invalidDebitResourcePath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testWarning() throws Exception {
		logger.info("test waring data field");
		String batchSystemDate="2018-10-10";
		String invalidDebitResourcePath = "ftp/ibk_ematch_to/ibk_nbps_20181009.txt";
		updateBatchConfigValid(batchSystemDate);
		uploadTestFile(invalidDebitResourcePath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testInvalidNumber() throws Exception {
		logger.info("test waring data field");
		String batchSystemDate="2018-10-11";
		String invalidDebitResourcePath = "ftp/ibk_ematch_to/ibk_nbps_20181010.txt";
		updateBatchConfigValid(batchSystemDate);
		uploadTestFile(invalidDebitResourcePath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testDummy() {
		assertEquals(1, 1);
	}

	@Before
	public void setup() {
		logger.info("setting up before test");
		try {
			String workingDir = System.getProperty("user.dir");
			String downloadFolderPath = generateFolderPath(workingDir, "target","batch","input",JOB_NAME);
			File downloadFolder = new File(downloadFolderPath);
			if(!downloadFolder.exists()) {
				FileUtils.forceMkdir(downloadFolder);
			}
			 // Initialize dcpBatchApplicationContext for partial failure reprocessing
	        initialArguments=new HashMap<String,String>();
	        if(!jobProcessDate.isEmpty())
	            initialArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY,jobProcessDate);
	        dcpBatchApplicationContext.setInitialJobArguments(initialArguments);
		}catch(Exception ex) {
            logger.error(ex + " File/Folder not found.");
		}
        logger.info("Started");

	}
	
	private void uploadTestFile(String resourceFilePath) {
		try {
			File testFile = getResourceFile(resourceFilePath);
			String workingDir = System.getProperty("user.dir");
			String testFileFolderPath = generateFolderPath(workingDir, "target", "ibk_ematch_to");
			uploadFileToFTPFolder(testFile, testFileFolderPath);
		} catch (Exception ex) {
            logger.error(ex + " File/Folder not found.");

		}
	}
	
    private void updateBatchConfigValid(String batchSystemDate) {
    	try {
    		String sql =String.format( "update tbl_batch_config " +
    				" set PARAMETER_VALUE='%s' "+
    				" where PARAMETER_KEY='batch.system.date'",batchSystemDate);
    		int row = jdbcTemplate.update(sql);
    		logger.info( String.format( "update batch config PARAMETER_VALUE='%s'-impacted row=%s", batchSystemDate,row));
    	}catch(Exception ex) {
    		logger.info(String.format( "update PARAMETER_VALUE batch config exception=%s", ex.getMessage()));
    	}
    }
    
	@After
	public void cleanup() {
		logger.info("cleaning up after test");
	}
}
