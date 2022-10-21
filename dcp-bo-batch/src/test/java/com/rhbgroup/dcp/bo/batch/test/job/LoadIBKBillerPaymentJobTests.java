package com.rhbgroup.dcp.bo.batch.test.job;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.LoadIBKBillerPaymentJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class, LoadIBKBillerPaymentJobConfiguration.class })
@ActiveProfiles("test")
public class LoadIBKBillerPaymentJobTests extends BaseFTPJobTest {

	public static final String JOB_NAME = "LoadIBKBillerPaymentJob";
	public static final String JOB_LAUNCHER_UTILS = "LoadIBKBillerPaymentJobLauncherTestUtils";
	
	@Autowired
	@Qualifier(JOB_LAUNCHER_UTILS)
	@Lazy
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private FTPIBKConfigProperties ftpConfigProperties;

	@Autowired
	private BatchParameterRepositoryImpl batchParameterRepository;

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
	//@Test
	public void testPositiveJobWithDBConfigParams() throws Exception {
		BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
		
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623.txt", billerCode, billerCode));
		
		String workingDir = System.getProperty("user.dir");
		// Create in target for easy removal
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		
		// Need to prefix with working directory because we might not have permission in other folder level
		uploadFileToFTPFolder(file, fileFolderPath);
		
		String beforeSQL1 = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE = 0";
		String beforeSQL2 = String.format("DELETE FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE BILLER_CODE = '%s'", billerCode);
		String beforeSQL3 = String.format("INSERT INTO TBL_BATCH_BILLER_PAYMENT_CONFIG (BILLER_CODE, TEMPLATE_NAME, IBK_FTP_FOLDER, FTP_FOLDER, FILE_NAME_FORMAT, REPORT_UNIT_URI, STATUS, IS_REQUIRED_TO_EXECUTE, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES(%s, 'Standard_01', '" + FTPUtils.convertFTPForWindows(fileFolderPath) + "', 'DCP_BPF_%s_Utest_FROM', '%s${yyyyMMdd}.txt', '/reports/DEV/Financial/DMBUD%s/daily_successful_bill', 'A' ,1, NOW(), 'admin', NOW(), 'admin')", billerCode, billerCode, billerCode, billerCode);
		String beforeSQL4 = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'UTest'";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4);
		batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-06-24");
		
		JobParameters jobParameters = new JobParametersBuilder()
			.addDate("now", new Date())
			.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
			.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());
		
		String afterSQL1 = "SELECT COUNT(*) FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'UTest' GROUP BY BILLER_ACCOUNT_NAME";
		List<Map<String, Object>> results = jdbcTemplate.queryForList(afterSQL1);
		long counter = (long)results.get(0).get("C1");
		assertEquals(30000, counter);

		String afterSQL2 = String.format("DELETE FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE BILLER_CODE = '%s'", billerCode);
		String afterSQL3 = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'UTest'";
		String afterSQL4 = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE = 1";
		jdbcTemplate.batchUpdate(afterSQL2, afterSQL3, afterSQL4);
		batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
	}

	/*
	 * Test using multiple valid billers and the records inserted should consist of both
	 */
	//@Test
	public void testPositiveJobWithMultipleBillers() throws Exception {
		BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);

		String billerCode = "9999";
		String billerCode2 = "9997";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623.txt", billerCode, billerCode));
		File file2 = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623.txt", billerCode2, billerCode2));

		String workingDir = System.getProperty("user.dir");
		// Create in target for easy removal
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		String fileFolderPath2 = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode2);

		// Need to prefix with working directory because we might not have permission in other folder level
		uploadFileToFTPFolder(file, fileFolderPath);
		uploadFileToFTPFolder(file2, fileFolderPath2);

		String beforeSQL1 = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE = 0";
		String beforeSQL2 = String.format("DELETE FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE BILLER_CODE IN ('%s','%s')", billerCode, billerCode2);
		String beforeSQL3 = String.format("INSERT INTO TBL_BATCH_BILLER_PAYMENT_CONFIG (BILLER_CODE, TEMPLATE_NAME, IBK_FTP_FOLDER, FTP_FOLDER, FILE_NAME_FORMAT, REPORT_UNIT_URI, STATUS, IS_REQUIRED_TO_EXECUTE, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES(%s, 'Standard_01', '" + FTPUtils.convertFTPForWindows(fileFolderPath) + "', 'DCP_BPF_%s_Utest_FROM', '%s${yyyyMMdd}.txt', '/reports/DEV/Financial/DMBUD%s/daily_successful_bill', 'A', 1, NOW(), 'admin', NOW(), 'admin')", billerCode, billerCode, billerCode, billerCode);
		String beforeSQL4 = String.format("INSERT INTO TBL_BATCH_BILLER_PAYMENT_CONFIG (BILLER_CODE, TEMPLATE_NAME, IBK_FTP_FOLDER, FTP_FOLDER, FILE_NAME_FORMAT, REPORT_UNIT_URI, STATUS, IS_REQUIRED_TO_EXECUTE, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES(%s, 'Standard_01', '" + FTPUtils.convertFTPForWindows(fileFolderPath2) + "', 'DCP_BPF_%s_Utest_FROM', '%s${yyyyMMdd}.txt', '/reports/DEV/Financial/DMBUD%s/daily_successful_bill', 'A', 1, NOW(), 'admin', NOW(), 'admin')", billerCode2, billerCode2, billerCode2, billerCode2);
		String beforeSQL5 = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'UTest'";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
		batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-06-24");

		JobParameters jobParameters = new JobParametersBuilder()
			.addDate("now", new Date())
			.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
			.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());

		String afterSQL1 = "SELECT COUNT(*) FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'UTest' GROUP BY BILLER_ACCOUNT_NAME";
		List<Map<String, Object>> results = jdbcTemplate.queryForList(afterSQL1);
		long counter = (long)results.get(0).get("C1");
		assertEquals(60000, counter);
		
		String afterSQL2 = String.format("DELETE FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE BILLER_CODE IN ('%s','%s')", billerCode, billerCode2);
		String afterSQL3 = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'UTest'";
		String afterSQL4 = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE = 1";
		jdbcTemplate.batchUpdate(afterSQL2, afterSQL3, afterSQL4);
		batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
	}
	
	/*
	 * Test to use the external JOB parameter processing date instead taking from the DB
	 */
	//@Test
	public void testPositiveJobWithExternalJobParams() throws Exception {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623.txt", billerCode, billerCode));
		
		String workingDir = System.getProperty("user.dir");
		// Create in target for easy removal
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		
		// Need to prefix with working directory because we might not have permission in other folder level
		uploadFileToFTPFolder(file, fileFolderPath);
		
		String beforeSQL1 = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE = 0";
		String beforeSQL2 = String.format("DELETE FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE BILLER_CODE = '%s'", billerCode);
		String beforeSQL3 = String.format("INSERT INTO TBL_BATCH_BILLER_PAYMENT_CONFIG (BILLER_CODE, TEMPLATE_NAME, IBK_FTP_FOLDER, FTP_FOLDER, FILE_NAME_FORMAT, REPORT_UNIT_URI, STATUS, IS_REQUIRED_TO_EXECUTE, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES('%s', 'Standard_01', '" + FTPUtils.convertFTPForWindows(fileFolderPath) + "', 'DCP_BPF_%s_Utest_FROM', '%s${yyyyMMdd}.txt', '/reports/DEV/Financial/DMBUD%s/daily_successful_bill', 'A', 1, NOW(), 'admin', NOW(), 'admin')", billerCode, billerCode, billerCode, billerCode);
		String beforeSQL4 = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'UTest'";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4);
		
		JobParameters jobParameters = new JobParametersBuilder()
			.addDate("now", new Date())
			.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
			.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, "20180623") // yyyyMMdd
			.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());
		
		String afterSQL1 = "SELECT COUNT(*) COUNTER FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'UTest' GROUP BY BILLER_ACCOUNT_NAME";
		List<Map<String, Object>> results = jdbcTemplate.queryForList(afterSQL1);
		long counter = (long)results.get(0).get("COUNTER");
        assertEquals(30000, counter);
		
		String afterSQL2 = beforeSQL2;
		String afterSQL3 = beforeSQL4;
		String afterSQL4 = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE = 1";
		jdbcTemplate.batchUpdate(afterSQL2, afterSQL3, afterSQL4);
	}
	
	/*
	 * Test to ensure if some FTP file not found, it shall still proceed to the next biller code and finish the JOB in COMPLETED status
	 */
	//@Test
	public void testPositiveJobWithSomeFTPFileNotFound() throws Exception {
		String billerCode = "9999";
		String billerCodeFileNotFound = "9998";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623.txt", billerCode, billerCode));
		
		String workingDir = System.getProperty("user.dir");
		// Create in target for easy removal
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		
		// Need to prefix with working directory because we might not have permission in other folder level
		uploadFileToFTPFolder(file, fileFolderPath);
		
		String beforeSQL1 = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE = 0";
		String beforeSQL2 = String.format("DELETE FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE BILLER_CODE IN ('%s','%s')", billerCode, billerCodeFileNotFound);
		// Insert the invalid first so that system pickup it first
		String beforeSQL3 = String.format("INSERT INTO TBL_BATCH_BILLER_PAYMENT_CONFIG (BILLER_CODE, TEMPLATE_NAME, IBK_FTP_FOLDER, FTP_FOLDER, FILE_NAME_FORMAT, REPORT_UNIT_URI, STATUS, IS_REQUIRED_TO_EXECUTE, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES(%s, 'Standard_01', '" + FTPUtils.convertFTPForWindows(fileFolderPath) + "', 'DCP_BPF_%s_Utest_FROM', '%s${yyyyMMdd}.txt', '/reports/DEV/Financial/DMBUD%s/daily_successful_bill', 'A', 1, NOW(), 'admin', NOW(), 'admin')", billerCodeFileNotFound, billerCodeFileNotFound, billerCodeFileNotFound, billerCodeFileNotFound);
		String beforeSQL4 = String.format("INSERT INTO TBL_BATCH_BILLER_PAYMENT_CONFIG (BILLER_CODE, TEMPLATE_NAME, IBK_FTP_FOLDER, FTP_FOLDER, FILE_NAME_FORMAT, REPORT_UNIT_URI, STATUS, IS_REQUIRED_TO_EXECUTE, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES(%s, 'Standard_01', '" + FTPUtils.convertFTPForWindows(fileFolderPath) + "', 'DCP_BPF_%s_Utest_FROM', '%s${yyyyMMdd}.txt', '/reports/DEV/Financial/DMBUD%s/daily_successful_bill', 'A', 1, NOW(), 'admin', NOW(), 'admin')", billerCode, billerCode, billerCode, billerCode);
		String beforeSQL5 = "DELETE FROM TBL_BATCH_STAGED_IBK_PAYMENT_TXN WHERE BILLER_ACCOUNT_NAME = 'UTest'";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
		
		JobParameters jobParameters = new JobParametersBuilder()
			.addDate("now", new Date())
			.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
			.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, "20180623") // yyyyMMdd
			.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		assertEquals(JOB_COMPLETED, jobExecution.getExitStatus().getExitCode());
		
		String afterSQL2 = beforeSQL2;
		String afterSQL3 = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE = 1";
		jdbcTemplate.batchUpdate(afterSQL2, afterSQL3);
	}
}