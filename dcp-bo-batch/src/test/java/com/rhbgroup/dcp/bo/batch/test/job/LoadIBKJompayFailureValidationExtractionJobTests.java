package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.config.LoadIBKJompayFailureValidationExtractionJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class, LoadIBKJompayFailureValidationExtractionJobConfiguration.class })
@ActiveProfiles("test")
public class LoadIBKJompayFailureValidationExtractionJobTests extends BaseFTPJobTest {

	public static final String JOB_NAME = "LoadIBKJompayFailureValidationExtractionJob";
	public static final String JOB_LAUNCHER_UTILS = "LoadIBKJompayFailureValidationExtractionJobLauncherTestUtils";
	
	@Autowired
	@Qualifier(JOB_LAUNCHER_UTILS)
	@Lazy
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private BatchParameterRepositoryImpl batchParameterRepository;
	
	@Autowired
	private FTPConfigProperties ftpConfigProperties;
	
	@Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
    private String inputFolder;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Before
	public void beforeLocalTest() throws IOException {
		setCustomFTP(ftpConfigProperties);
		super.beforeFTPTest();
	}
	
	@After
	public void afterLocalTest() throws Exception {
		setCustomFTP(null);
		super.afterFTPTest();
	}
	
	/*
	 * Test to test using batch system date from the DB and calculate the processing date from it and use it on the JOB execution
	 */
	@Test
	public void testPositiveJobWithDBConfigParams() throws Exception {
		BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
		
		File file = getResourceFile("ftp/nbps_channel_to/IBKUD041_20180612.txt");
		
		String workingDir = System.getProperty("user.dir");
		// Create in target for easy removal
		String fileFolderPath = generateFolderPath(workingDir, "target", "nbps_channel_to");
		
		// Need to prefix with working directory because we might not have permission in other folder level
		uploadFileToFTPFolder(file, fileFolderPath);
		
		File targetFolder = Paths.get(inputFolder, JOB_NAME).toFile();
		createTargetFolderIfNotExists(targetFolder);
		
		
		batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-06-12");
		
		JobParameters jobParameters = new JobParametersBuilder()
			.addDate("now", new Date())
			.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
			.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());
		
		String selectSQL1 = "SELECT COUNT(*) FROM TBL_BATCH_STAGED_JOMPAY_FAILURE_TXN WHERE FILE_NAME = 'IBKUD041_20180612.txt'";
		List<Map<String, Object>> results = jdbcTemplate.queryForList(selectSQL1);
		long counter = (long)results.get(0).get("C1");
		assertEquals(2, counter);
		
		String afterSQL1 = "DELETE FROM TBL_BATCH_STAGED_JOMPAY_FAILURE_TXN WHERE FILE_NAME = 'IBKUD041_20180612.txt'";
		jdbcTemplate.queryForList(afterSQL1);
		batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
	}
	
	/*
	 * Test to use the external JOB parameter processing date instead taking from the DB
	 */
	@Test
	public void testPositiveJobWithExternalJobParams() throws Exception {
		BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
		
		File file = getResourceFile("ftp/nbps_channel_to/IBKUD041_20180612.txt");
		
		String workingDir = System.getProperty("user.dir");
		// Create in target for easy removal
		String fileFolderPath = generateFolderPath(workingDir, "target", "nbps_channel_to");
		
		// Need to prefix with working directory because we might not have permission in other folder level
		uploadFileToFTPFolder(file, fileFolderPath);
		
		File targetFolder = Paths.get(inputFolder, JOB_NAME).toFile();
		createTargetFolderIfNotExists(targetFolder);
		
		
		batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-06-13");
		
		JobParameters jobParameters = new JobParametersBuilder()
			.addDate("now", new Date())
			.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
			.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, "20180612") // yyyyMMdd
			.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());
		
		String selectSQL1 = "SELECT COUNT(*) FROM TBL_BATCH_STAGED_JOMPAY_FAILURE_TXN WHERE FILE_NAME = 'IBKUD041_20180612.txt'";
		List<Map<String, Object>> results = jdbcTemplate.queryForList(selectSQL1);
		long counter = (long)results.get(0).get("C1");
		assertEquals(2, counter);
		
		String afterSQL1 = "DELETE FROM TBL_BATCH_STAGED_JOMPAY_FAILURE_TXN WHERE FILE_NAME = 'IBKUD041_20180612.txt'";
		jdbcTemplate.queryForList(afterSQL1);
		batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
	}
	
	/*
	 * Test to use the external JOB parameter processing date instead taking from the DB
	 * inbound file only contains header, no detail transaction
	 */	
	@Test
	public void testPositiveJobWithEmptyDetail() throws Exception {
		BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
		
		File file = getResourceFile("ftp/nbps_channel_to/IBKUD041_20180613.txt");
		
		String workingDir = System.getProperty("user.dir");
		// Create in target for easy removal
		String fileFolderPath = generateFolderPath(workingDir, "target", "nbps_channel_to");
		
		// Need to prefix with working directory because we might not have permission in other folder level
		uploadFileToFTPFolder(file, fileFolderPath);
		
		File targetFolder = Paths.get(inputFolder, JOB_NAME).toFile();
		createTargetFolderIfNotExists(targetFolder);
		
		
		batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-06-13");
		
		JobParameters jobParameters = new JobParametersBuilder()
			.addDate("now", new Date())
			.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
			.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());
		
		String selectSQL1 = "SELECT COUNT(*) FROM TBL_BATCH_STAGED_JOMPAY_FAILURE_TXN WHERE FILE_NAME = 'IBKUD041_20180613.txt'";
		List<Map<String, Object>> results = jdbcTemplate.queryForList(selectSQL1);
		long counter = (long)results.get(0).get("C1");
		assertEquals(0, counter);
		
		String afterSQL1 = "DELETE FROM TBL_BATCH_STAGED_JOMPAY_FAILURE_TXN WHERE FILE_NAME = 'IBKUD041_20180613.txt'";
		jdbcTemplate.queryForList(afterSQL1);
		batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
	}
	/*
	 * Test to ensure in case invalid file contents found, the correct row still insert to the DB but the job will end with FAILED exit status
	 */
	@Test
	public void testNegativeJobWithInvalidFTPFileContents() throws Exception {
		BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
		
		File file = getResourceFile("ftp/nbps_channel_to/IBKUD041_20180612_invalid_fields.txt");
		File newFile = tempFolder.newFile("IBKUD041_20180612.txt");
		FileUtils.copyFile(file, newFile);
		
		String workingDir = System.getProperty("user.dir");
		// Create in target for easy removal
		String fileFolderPath = generateFolderPath(workingDir, "target", "nbps_channel_to");
		
		// Need to prefix with working directory because we might not have permission in other folder level
		uploadFileToFTPFolder(newFile, fileFolderPath);
		
		File targetFolder = Paths.get(inputFolder, JOB_NAME).toFile();
		createTargetFolderIfNotExists(targetFolder);
		
		batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-06-12");
		
		JobParameters jobParameters = new JobParametersBuilder()
			.addDate("now", new Date())
			.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
			.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		assertEquals(JOB_FAILED, jobExecution.getExitStatus().getExitCode());
		
		String selectSQL1 = "SELECT COUNT(*) FROM TBL_BATCH_STAGED_JOMPAY_FAILURE_TXN WHERE FILE_NAME = 'IBKUD041_20180612.txt'";
		List<Map<String, Object>> results = jdbcTemplate.queryForList(selectSQL1);
		long counter = (long)results.get(0).get("C1");
		assertEquals(1, counter);
		
		String afterSQL1 = "DELETE FROM TBL_BATCH_STAGED_JOMPAY_FAILURE_TXN WHERE FILE_NAME = 'IBKUD041_20180612.txt'";
		jdbcTemplate.queryForList(afterSQL1);
		batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
	}
	
	private void createTargetFolderIfNotExists(File targetFolder) {
		if (!targetFolder.exists()) {
        	targetFolder.mkdirs();
        }
	}
}
