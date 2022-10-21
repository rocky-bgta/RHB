package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.LoadMassNotificationsJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadMassNotificationsJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class, LoadMassNotificationsJobConfiguration.class })
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class LoadMassNotificationsJobTests extends BaseJobTest {
	
	private static final Logger logger = Logger.getLogger(LoadMassNotificationsJobTests.class);
	
	private static final String WORKING_DIR = System.getProperty("user.dir");
	
	private static final String FILE_FOLDER = "dcp_mass_notification_from";
	
	private static final String ARCHIVE_FOLDER = "dcp_mass_notification_from_archive";
	
	private static final String PARAMETER_DATE = "20181206";
	
	public static final String JOB_NAME = "LoadMassNotificationsJob";
	
	public static final String JOB_LAUNCHER_UTILS = "LoadMassNotificationsJobLauncherTestUtils";
	
	private static final String BATCH_JOB_PARAMETER_JOB_BATCH_EVENT_CODE_KEY = "eventcode";	
	
	@Autowired
	private LoadMassNotificationsJobConfigProperties configProperties;
	
	@Lazy
	@Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Before
	public void beforeTestLocal() throws IOException {
		cleanupFolder();
	}
	
	/**
	 * Test positive case for LoadMassNotificationsJob with default process date & event code
	 * @throws Exception
	 */
	@Test
	public void testJobPositive() throws Exception {
		logger.info(String.format("test job positive [%s]", JOB_NAME));		
		String resourcePath = "batch/input/" + FILE_FOLDER + "/DCP_LDCPA6005T_20181206.txt";
		uploadFile(resourcePath, DateUtils.formatDateString(new Date(), configProperties.getNameDateFormat()));
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	/**
	 * Test positive case for LoadMassNotificationsJob with process date parameter
	 * @throws Exception
	 */
	@Test
	public void testJobPositiveWithProcessDate() throws Exception {
		logger.info(String.format("test job positive [%s]", JOB_NAME));		
		String resourcePath = "batch/input/" + FILE_FOLDER + "/DCP_LDCPA6005T_20181206.txt";
		uploadFile(resourcePath, PARAMETER_DATE);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, PARAMETER_DATE)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	/**
	 * Test positive case for LoadMassNotificationsJob with event code parameter
	 * @throws Exception
	 */
	@Test
	public void testJobPositiveWithEventCode() throws Exception {
		logger.info(String.format("test job negative [%s]", JOB_NAME));		
		String resourcePath = "batch/input/" + FILE_FOLDER + "/DCP_LDCPA6005T_20181206.txt";
		uploadFile(resourcePath, DateUtils.formatDateString(new Date(), configProperties.getNameDateFormat()));
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_EVENT_CODE_KEY, "90004")
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	/**
	 * Test negative case for LoadMassNotificationsJob with default process date & event code
	 * @throws Exception
	 */
	@Test
	public void testJobNegative() throws Exception {
		logger.info(String.format("test job negative [%s]", JOB_NAME));		
		String resourcePath = "batch/input/" + FILE_FOLDER + "/DCP_LDCPA6005T_20181206.txt";
		uploadFile(resourcePath, PARAMETER_DATE);
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	/**
	 * Test negative case for LoadMassNotificationsJob with process date parameter
	 * @throws Exception
	 */
	@Test
	public void testJobNegativeWithProcessDate() throws Exception {
		logger.info(String.format("test job negative [%s]", JOB_NAME));		
		String resourcePath = "batch/input/" + FILE_FOLDER + "/DCP_LDCPA6005T_20181206.txt";
		uploadFile(resourcePath, "06122018");
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, "06122018")
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	/**
	 * Test negative case for LoadMassNotificationsJob with event code parameter
	 * @throws Exception
	 */
	@Test
	public void testJobNegativeWithEventCode() throws Exception {
		logger.info(String.format("test job negative [%s]", JOB_NAME));		
		String resourcePath = "batch/input/" + FILE_FOLDER + "/DCP_LDCPA6005T_20181206.txt";
		uploadFile(resourcePath, "06122018");
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_EVENT_CODE_KEY, "06122018")
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	private void uploadFile(String filePath, String parameterDate) throws Exception {
		File testFile = getResourceFile(filePath);
		String testFileFolderPath = generateFolderPath(WORKING_DIR, "target", "batch", "input", FILE_FOLDER);
		String fileName = (testFile.getName()).replace("20181206", parameterDate);
		File inputFile = Paths.get(testFileFolderPath, fileName).toFile();
		FileUtils.copyFile(testFile, inputFile);
		
	}
	
	private void cleanupFolder() throws IOException {
		File inputFolder = new File(generateFolderPath(WORKING_DIR, "target", "batch", "input", FILE_FOLDER));
    	File archiveFolder = new File(generateFolderPath(WORKING_DIR, "target", "batch", "input", ARCHIVE_FOLDER));
    	deleteFolderIfExists(inputFolder);
    	deleteFolderIfExists(archiveFolder);
    }
	
	private void deleteFolderIfExists(File folder) throws IOException {
		if(folder.exists() && folder.isDirectory()) {
			FileUtils.deleteDirectory(folder);
    	}
	}

}
