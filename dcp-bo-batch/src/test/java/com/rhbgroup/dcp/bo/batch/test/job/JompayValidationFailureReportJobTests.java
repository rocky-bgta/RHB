package com.rhbgroup.dcp.bo.batch.test.job;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.AssertFile;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General;
import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.JompayValidationFailureReportJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.JompayFailureValidationExtractionJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfigHSQL.class, JompayValidationFailureReportJobConfiguration.class})
@ActiveProfiles("test")
public class JompayValidationFailureReportJobTests extends BaseFTPJobTest {
	
	public static final String JOB_NAME = "JompayValidationFailureReportJob";
	public static final String JOB_LAUNCHER_UTILS = "JompayValidationFailureReportJobLauncherTestUtils";
	
	@Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private JompayFailureValidationExtractionJobConfigProperties jobConfigProperties;
	
	@Autowired
	private FTPIBKConfigProperties ftpConfigProperties;
	
	@Autowired
	private BatchParameterRepositoryImpl batchParameterRepository;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	/*
	 * Test to ensure JOB able to execute using the DB batch system date
	 */
	@Test
    public void testPositiveJobWithDBConfigParams() throws Exception {
		BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
		
		String beforeSQL1 = "DELETE FROM VW_BATCH_JOMPAY_FAILURE_VALIDATION WHERE BILLER_CODE = '4600' AND PAYMENT_CHANNEL = '3'";
		String beforeSQL2 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Timeout','2018-08-06 11:11:10')";
		String beforeSQL3 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Network Issue','2018-08-06 22:22:20')";
		String beforeSQL4 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Not Sufficient Money','2018-08-13 11:11:30')";
		String beforeSQL5 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Unexpected Issues','2018-08-13 22:22:40')";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
		
		// Use Monday
		batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-08-13");
		
		Date currentDate = new Date();
		String currentDateStr = DateUtils.formatDateString(currentDate, General.COMMON_DATE_DATA_FORMAT);
		String outputFilePath = String.format("target/dcp_nbps_channel_from/IBKUD041_%s.txt", currentDateStr);
		File outputFile = new File(outputFilePath);
		if(outputFile.exists() && outputFile.isFile()) {
			outputFile.delete();
		}
		
		FTPUtils.createFTPFolderIfNotExists(jobConfigProperties.getFtpFolder(), ftpConfigProperties);
		
        JobParameters jobParameters = new JobParametersBuilder()
    		.addDate("now", new Date())
    		.addString("jobname", JOB_NAME)
    		.toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        
        String templateFilePath = getResourceFile("ftp/dcp_nbps_channel_from/IBKUD041_template.txt").getAbsolutePath();
        File templateFile = new File(templateFilePath);
        
        File expectedFile = null;
        try {
            String content = FileUtils.readFileToString(templateFile, Charset.defaultCharset());
            content = content.replace("[REPLACE_DATE]", currentDateStr);
            String workingDir = System.getProperty("user.dir");
    		String expectedFilePath = generateFolderPath(workingDir, "target", "dcp_nbps_channel_from", "IBKUD041_expected.txt");
    		expectedFile = new File(expectedFilePath);
    		if(expectedFile.exists() && expectedFile.isFile()) {
    			expectedFile.delete();
    		}
            FileUtils.writeStringToFile(expectedFile, content, Charset.defaultCharset());
         } catch (IOException e) {
        	 e.printStackTrace();
         }
        
        AssertFile.assertFileEquals(expectedFile, outputFile);
        
        String afterSql1 = "DELETE FROM VW_BATCH_JOMPAY_FAILURE_VALIDATION WHERE BILLER_CODE = '4600' AND PAYMENT_CHANNEL = '3'";
        jdbcTemplate.batchUpdate(afterSql1);
        batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
    }
	
	/*
	 * Test to ensure JOB able to execute using the DB batch system date even thought it is not exactly Monday
	 */
	@Test
    public void testPositiveJobWithNonMondayBatchSystemDate() throws Exception {
		BatchParameter currentBatchParameter = batchParameterRepository.getBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
		
		String beforeSQL1 = "DELETE FROM VW_BATCH_JOMPAY_FAILURE_VALIDATION WHERE BILLER_CODE = '4600' AND PAYMENT_CHANNEL = '3'";
		String beforeSQL2 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Timeout','2018-08-06 11:11:10')";
		String beforeSQL3 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Network Issue','2018-08-06 22:22:20')";
		String beforeSQL4 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Not Sufficient Money','2018-08-13 11:11:30')";
		String beforeSQL5 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Unexpected Issues','2018-08-13 22:22:40')";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
		
		// Use Wednesday instead of Monday
		batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-08-15");
		
		Date currentDate = new Date();
		String currentDateStr = DateUtils.formatDateString(currentDate, General.COMMON_DATE_DATA_FORMAT);
		String outputFilePath = String.format("target/dcp_nbps_channel_from/IBKUD041_%s.txt", currentDateStr);
		File outputFile = new File(outputFilePath);
		if(outputFile.exists() && outputFile.isFile()) {
			outputFile.delete();
		}
		
		FTPUtils.createFTPFolderIfNotExists(jobConfigProperties.getFtpFolder(), ftpConfigProperties);
		
        JobParameters jobParameters = new JobParametersBuilder()
    		.addDate("now", new Date())
    		.addString("jobname", JOB_NAME)
    		.toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        
        String templateFilePath = getResourceFile("ftp/dcp_nbps_channel_from/IBKUD041_template.txt").getAbsolutePath();
        File templateFile = new File(templateFilePath);
        
        File expectedFile = null;
        try {
            String content = FileUtils.readFileToString(templateFile, Charset.defaultCharset());
            content = content.replace("[REPLACE_DATE]", currentDateStr);
            String workingDir = System.getProperty("user.dir");
    		String expectedFilePath = generateFolderPath(workingDir, "target", "dcp_nbps_channel_from", "IBKUD041_expected.txt");
    		expectedFile = new File(expectedFilePath);
    		if(expectedFile.exists() && expectedFile.isFile()) {
    			expectedFile.delete();
    		}
            FileUtils.writeStringToFile(expectedFile, content, Charset.defaultCharset());
         } catch (IOException e) {
        	 e.printStackTrace();
         }
        
        AssertFile.assertFileEquals(expectedFile, outputFile);
        
        String afterSql1 = "DELETE FROM VW_BATCH_JOMPAY_FAILURE_VALIDATION WHERE BILLER_CODE = '4600' AND PAYMENT_CHANNEL = '3'";
        jdbcTemplate.batchUpdate(afterSql1);
        batchParameterRepository.updateBatchParameter(currentBatchParameter.getName(), currentBatchParameter.getValue());
    }
	
	/*
	 * Test to ensure JOB able to execute using the JOB parameter FROM and TO date range which valid in Monday to Sunday
	 */
	@Test
    public void testPositiveJobWithExternalJobParams() throws Exception {
		
		String beforeSQL1 = "DELETE FROM VW_BATCH_JOMPAY_FAILURE_VALIDATION WHERE BILLER_CODE = '4600' AND PAYMENT_CHANNEL = '3'";
		String beforeSQL2 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Timeout','2018-08-06 11:11:10')";
		String beforeSQL3 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Network Issue','2018-08-06 22:22:20')";
		String beforeSQL4 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Not Sufficient Money','2018-08-13 11:11:30')";
		String beforeSQL5 = "INSERT INTO VW_BATCH_JOMPAY_FAILURE_VALIDATION(BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME) VALUES('4600','3','Unexpected Issues','2018-08-13 22:22:40')";
		jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3, beforeSQL4, beforeSQL5);
		
		Date currentDate = new Date();
		String currentDateStr = DateUtils.formatDateString(currentDate, General.COMMON_DATE_DATA_FORMAT);
		String outputFilePath = String.format("target/dcp_nbps_channel_from/IBKUD041_%s.txt", currentDateStr);
		File outputFile = new File(outputFilePath);
		if(outputFile.exists() && outputFile.isFile()) {
			outputFile.delete();
		}
		
		FTPUtils.createFTPFolderIfNotExists(jobConfigProperties.getFtpFolder(), ftpConfigProperties);
		
        JobParameters jobParameters = new JobParametersBuilder()
    		.addDate("now", new Date())
    		.addString("jobname", JOB_NAME)
    		.addString("jobprocessfromdatetodate", "(20180806,20180812)")
    		.toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        
        String templateFilePath = getResourceFile("ftp/dcp_nbps_channel_from/IBKUD041_template.txt").getAbsolutePath();
        File templateFile = new File(templateFilePath);
        
        File expectedFile = null;
        try {
            String content = FileUtils.readFileToString(templateFile, Charset.defaultCharset());
            content = content.replace("[REPLACE_DATE]", currentDateStr);
            String workingDir = System.getProperty("user.dir");
    		String expectedFilePath = generateFolderPath(workingDir, "target", "dcp_nbps_channel_from", "IBKUD041_expected.txt");
    		expectedFile = new File(expectedFilePath);
    		if(expectedFile.exists() && expectedFile.isFile()) {
    			expectedFile.delete();
    		}
            FileUtils.writeStringToFile(expectedFile, content, Charset.defaultCharset());
         } catch (IOException e) {
        	 e.printStackTrace();
         }
        
        AssertFile.assertFileEquals(expectedFile, outputFile);
        
        String afterSql1 = "DELETE FROM VW_BATCH_JOMPAY_FAILURE_VALIDATION WHERE BILLER_CODE = '4600' AND PAYMENT_CHANNEL = '3'";
        jdbcTemplate.batchUpdate(afterSql1);
    }
}
