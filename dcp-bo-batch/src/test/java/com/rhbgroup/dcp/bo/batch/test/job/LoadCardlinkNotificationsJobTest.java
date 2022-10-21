package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.config.LoadCardlinkNotificationsJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class, LoadCardlinkNotificationsJobConfiguration.class })
@ActiveProfiles("test")
public class LoadCardlinkNotificationsJobTest extends BaseFTPJobTest {
	public static final String JOB_NAME = "LoadCardlinkNotificationsJob";
	public static final String JOB_LAUNCHER_UTILS="LoadCardlinkNotificationsJobLauncherTestUtils";
	private static final Logger logger = Logger.getLogger(LoadCardlinkNotificationsJobTest.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
    private DataSource dataSource;
	
	@Autowired
	private DcpBatchApplicationContext dcpBatchApplicationContext;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Lazy
	@Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Test
	public void testProcessDate() throws Exception{
		logger.info("test executing new job with process date input param");
		String jobprocessdate="20181026";
        Map<String, String> initialJobArguments = new HashMap<>();
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME);
        initialJobArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate);
        dcpBatchApplicationContext.setInitialJobArguments(initialJobArguments);

		String resourcePath = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20181026.txt";
		uploadFile(resourcePath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY,jobprocessdate)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobPositive() throws Exception {
		logger.info("test job positive");
		String batchSystemDate="2018-10-27";
		updateBatchSystemDate(batchSystemDate);
		String resourcePath = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20181026.txt";
		uploadFile(resourcePath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	/**
	 * DCP_LDCPD6002T_20190108.txt format not same as per spec
	 * @throws Exception
	 */
	@Test
	public void testJobPositiveRemovePadding() throws Exception {
		logger.info("test job positive remove padding");
		String batchSystemDate="2019-01-09";
		updateBatchSystemDate(batchSystemDate);
		String resourcePath = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20190108.txt";
		uploadFile(resourcePath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobWithExtraLine() throws Exception {
		logger.info("test job positive with extra line");
		String batchSystemDate="2019-01-10";
		updateBatchSystemDate(batchSystemDate);
		String resourcePath = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20190109.txt";
		uploadFile(resourcePath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobInvalidHash() throws Exception {
		logger.info("test job invalid hash");
		String batchSystemDate="2018-10-26";
		updateBatchSystemDate(batchSystemDate);
		String invalidHashPath = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20181025.txt";
		uploadFile(invalidHashPath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobInvalidCount() throws Exception {
		logger.info("test job invalid count");
		String batchSystemDate="2018-10-25";
		updateBatchSystemDate(batchSystemDate);
		String invalidCountPath = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20181024.txt";
		uploadFile(invalidCountPath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobInvalidEvent() throws Exception {
		logger.info("test job invalid event code");
		String batchSystemDate="2018-10-24";
		updateBatchSystemDate(batchSystemDate);
		String invalidEventPath = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20181023.txt";
		uploadFile(invalidEventPath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobInvalidOutstandingAmt() throws Exception {
		logger.info("test job invalid outstanding amout");
		String batchSystemDate="2018-10-23";
		updateBatchSystemDate(batchSystemDate);
		String invalidOutAmtPath = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20181022.txt";
		uploadFile(invalidOutAmtPath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobInvalidCC() throws Exception {
		logger.info("test job invalid credit card number");
		String batchSystemDate="2018-10-22";
		updateBatchSystemDate(batchSystemDate);
		String invalidCCPath = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20181021.txt";
		uploadFile(invalidCCPath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobFileExisted() throws Exception {
		logger.info("test job file was loaded before");
		String batchSystemDate="2018-10-21";
		updateBatchSystemDate(batchSystemDate);
		String invalidFileLoaded = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20181020.txt";
		uploadFile(invalidFileLoaded);
		insertData();
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobInvalidDueDate() throws Exception {
		logger.info("test job file invalie payment due date");
		String batchSystemDate="2018-11-09";
		updateBatchSystemDate(batchSystemDate);
		String invalidDueDatePath="ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20181108.txt";
		uploadFile(invalidDueDatePath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testJobPositiveNoDetails() throws Exception {
		logger.info("test job positive no details");
		String batchSystemDate="2019-02-20";
		updateBatchSystemDate(batchSystemDate);
		String resourcePath = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20190219.txt";
		uploadFile(resourcePath);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	private void uploadFile(String filePath) throws Exception {
		File testFile = getResourceFile(filePath);
		String workingDir = System.getProperty("user.dir");
		String testFileFolderPath = generateFolderPath(workingDir, "target", "uat_dcp_cardlink_notification_from");
		uploadFileToFTPFolder(testFile, testFileFolderPath);
	}
	
	@Before
	public void setup() throws Exception{
    	logger.info("Starting up...");

		try {
			String workingDir = System.getProperty("user.dir");
			String downloadFolderPath = generateFolderPath(workingDir, "target","batch","input",JOB_NAME);
			File downloadFolder = new File(downloadFolderPath);
			if(!downloadFolder.exists()) {
				FileUtils.forceMkdir(downloadFolder);
			}
			jdbcTemplate.setDataSource(dataSource);
		}catch(Exception ex) {
            logger.error(ex + " File/Folder not found.");
		}
        logger.info("Started");
	}
	
	@After
	public void cleanup() throws Exception{
		deleteTables();
	}
	
	private void insertData() {
		String sql = "insert into TBL_BATCH_STAGED_NOTIFICATION_RAW "+
				"(job_execution_id,file_name,process_date,event_code,key_type,is_processed,created_time,created_by,updated_time,updated_by) " +
				" VALUES " +
				"(999,'DCP_LDCPD6002T_20181020.txt','20151212','55000','CC',0,now(), 'admin',now(),'admin')";
		int row = jdbcTemplate.update(sql);
		logger.info("cleanup table TBL_BATCH_STAGED_NOTIFICATION_RAW row="+row);
	}
	
	private void deleteTables() {
		String sql = "DELETE FROM TBL_BATCH_STAGED_NOTIFICATION_RAW ";
		int row = jdbcTemplate.update(sql);
		logger.info("cleanup table TBL_BATCH_STAGED_NOTIFICATION_RAW row="+row);
	}
	
	private void updateBatchSystemDate(String batchSystemDate) {
		String sql = "UPDATE TBL_BATCH_CONFIG set PARAMETER_VALUE='" + batchSystemDate + "'"
				+ " WHERE PARAMETER_KEY='batch.system.date' ";
		int row = jdbcTemplate.update(sql);
		logger.info(String.format("update batch.system.date=%s, row=%s", batchSystemDate, row));
	}
}
