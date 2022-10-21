
package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.STATUS_SUCCESS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.STATUS_FAILED;


import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.sql.DataSource;

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

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.job.config.LoadEMUnitTrustJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadEMUnitTrustJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class, LoadEMUnitTrustJobConfiguration.class })
@ActiveProfiles("test")
public class LoadEMUnitTrustJobTest extends BaseFTPJobTest {
	private static final Logger logger = Logger.getLogger(LoadEMUnitTrustJobTest.class);
	public static final String JOB_NAME="LoadEMUnitTrustJob";
	public static final String JOB_LAUNCHER_UTILS="LoadEMUnitTrustJobLauncherUtils";
	private static final String FTP_FOLDER="dcp_em_ut_from";

	@Autowired
	LoadEMUnitTrustJobConfigProperties configProp;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	DataSource dataSource;
	
	@Lazy
	@Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
	private JobLauncherTestUtils jobLauncherTestUtils;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testCompleteJob() throws Exception {
		updateBatchConfigValid("2018-11-05");
		String utCustFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_20181104.txt";
		String utCustRelFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_rel_20181104.txt";
		String utAccountFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_20181104.txt";
		String utAccountHldFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_hldg_20181104.txt";
		String utFundFile = "ftp/" + FTP_FOLDER + "/em_dcp_fund_20181104.txt";
		uploadTestFile(utCustFile, utCustRelFile, utAccountFile, utAccountHldFile,utFundFile);
		JobParameters jobParameters = new JobParametersBuilder().addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals(getLatestJobStatus(), STATUS_SUCCESS);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testProcessedDate()throws Exception {
		String jobprocessdate = "20181104";
		String utCustFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_20181104.txt";
		String utCustRelFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_rel_20181104.txt";
		String utAccountFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_20181104.txt";
		String utAccountHldFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_hldg_20181104.txt";
		String utFundFile = "ftp/" + FTP_FOLDER + "/em_dcp_fund_20181104.txt";
		uploadTestFile(utCustFile, utCustRelFile, utAccountFile, utAccountHldFile,utFundFile);
		
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, jobprocessdate)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

	}
	
	@Test
	public void testTargetDataSet() throws Exception {
		insertSuccessTargetSet();
		updateBatchConfigValid("2019-01-20");
		String utCustFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_20190119.txt";
		String utCustRelFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_rel_20190119.txt";
		String utAccountFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_20190119.txt";
		String utAccountHldFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_hldg_20190119.txt";
		String utFundFile = "ftp/" + FTP_FOLDER + "/em_dcp_fund_20190119.txt";
		uploadTestFile(utCustFile, utCustRelFile, utAccountFile, utAccountHldFile,utFundFile);
		JobParameters jobParameters = new JobParametersBuilder().addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals(getLatestJobStatus(), STATUS_SUCCESS);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	
	private void insertSuccessTargetSet() {
		jdbcTemplate.setDataSource(dataSource);
		String sql="INSERT INTO TBL_BATCH_UT_JOB_STATUS_CONTROL  "+ 
			"(JOB_EXECUTION_ID,BATCH_PROCESS_DATE,BATCH_END_DATETIME,TARGET_DATASET,STATUS,CREATED_BY,CREATED_TIME,UPDATED_BY,UPDATED_TIME)" +
			" VALUES " +
			"(?,?,?,?,?,?,?,?,?)";
		int row = jdbcTemplate.update(sql, new Object[] {1,new Date(),new Date()
				,1,1,JOB_NAME,new Date()
				,JOB_NAME,new Date()});
		logger.info(String.format("Add %s record into TBL_BATCH_UT_JOB_STATUS_CONTROL", row));
	}
	
	@Test
	public void testInvalidHeader() throws Exception {
		updateBatchConfigValid("2018-11-06");
		String utCustFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_20181105.txt";
		String utCustRelFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_rel_20181105.txt";
		String utAccountFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_20181105.txt";
		String utAccountHldFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_hldg_20181105.txt";
		String utFundFile = "ftp/" + FTP_FOLDER + "/em_dcp_fund_20181105.txt";
		uploadTestFile(utCustFile, utCustRelFile, utAccountFile, utAccountHldFile,utFundFile);		
		JobParameters jobParameters = new JobParametersBuilder().addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testInvalidHeaderPrefix() throws Exception {
		updateBatchConfigValid("2018-11-06");
		String utCustFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_20181106.txt";
		String utCustRelFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_rel_20181106.txt";
		String utAccountFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_20181106.txt";
		String utAccountHldFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_hldg_20181106.txt";
		String utFundFile = "ftp/" + FTP_FOLDER + "/em_dcp_fund_20181106.txt";
		uploadTestFile(utCustFile, utCustRelFile, utAccountFile, utAccountHldFile,utFundFile);	
		JobParameters jobParameters = new JobParametersBuilder().addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testInvalidRowCount() throws Exception {
		updateBatchConfigValid("2018-11-07");
		String utCustFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_20181107.txt";
		String utCustRelFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_rel_20181107.txt";
		String utAccountFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_20181107.txt";
		String utAccountHldFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_hldg_20181107.txt";
		String utFundFile = "ftp/" + FTP_FOLDER + "/em_dcp_fund_20181107.txt";
		uploadTestFile(utCustFile, utCustRelFile, utAccountFile, utAccountHldFile,utFundFile);			
		JobParameters jobParameters = new JobParametersBuilder().addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testInvalidAccountLastDate() throws Exception {
		updateBatchConfigValid("2018-11-09");
		String utCustFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_20181108.txt";
		String utCustRelFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_rel_20181108.txt";
		String utAccountFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_20181108.txt";
		String utAccountHldFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_hldg_20181108.txt";
		String utFundFile = "ftp/" + FTP_FOLDER + "/em_dcp_fund_20181108.txt";
		uploadTestFile(utCustFile, utCustRelFile, utAccountFile, utAccountHldFile,utFundFile);			
		JobParameters jobParameters = new JobParametersBuilder().addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals(getLatestJobStatus(), STATUS_FAILED);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testInvalidAccountHldUnit() throws Exception {
		updateBatchConfigValid("2018-11-09");
		String utCustFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_20181109.txt";
		String utCustRelFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_rel_20181109.txt";
		String utAccountFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_20181109.txt";
		String utAccountHldFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_hldg_20181109.txt";
		String utFundFile = "ftp/" + FTP_FOLDER + "/em_dcp_fund_20181109.txt";
		uploadTestFile(utCustFile, utCustRelFile, utAccountFile, utAccountHldFile,utFundFile);			
		JobParameters jobParameters = new JobParametersBuilder().addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals(getLatestJobStatus(), STATUS_FAILED);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Test
	public void testInvalidFundPrice() throws Exception {
		updateBatchConfigValid("2018-11-10");
		String utCustFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_20181110.txt";
		String utCustRelFile = "ftp/" + FTP_FOLDER + "/em_dcp_cis_rel_20181110.txt";
		String utAccountFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_20181110.txt";
		String utAccountHldFile = "ftp/" + FTP_FOLDER + "/em_dcp_acct_hldg_20181110.txt";
		String utFundFile = "ftp/" + FTP_FOLDER + "/em_dcp_fund_20181110.txt";
		uploadTestFile(utCustFile, utCustRelFile, utAccountFile, utAccountHldFile,utFundFile);			
		JobParameters jobParameters = new JobParametersBuilder().addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals(getLatestJobStatus(), STATUS_FAILED);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
	}
	
	
	@Before
	public void startup() throws IOException {
		logger.info("Starting up unit test");
		jdbcTemplate.setDataSource(dataSource);
		insertMaxKeyTargetSet();
		insertAccountBatchInfo();
		String workingDir = System.getProperty("user.dir");
		String downloadFolderPath = generateFolderPath(workingDir, "target","batch","input",JOB_NAME);
		File downloadFolder = new File(downloadFolderPath);
		if(!downloadFolder.exists()) {
			FileUtils.forceMkdir(downloadFolder);
		}
	}
	
	@After
	public void cleanup() {
		logger.info("cleaning up after unit test");
		deleteData();
	}
	
	private void uploadTestFile(String ...resourceFilePaths) {
		try {
			for(String resourceFilePath: resourceFilePaths ) {
				File testFile = getResourceFile(resourceFilePath);
				String workingDir = System.getProperty("user.dir");
				String testFileFolderPath = generateFolderPath(workingDir, "target", FTP_FOLDER );
				uploadFileToFTPFolder(testFile, testFileFolderPath);
				logger.info("uploaded file " + resourceFilePath);
			}
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
    
	private int getLatestJobStatus() {
		String sql = "SELECT TOP 1 STATUS FROM TBL_BATCH_UT_JOB_STATUS_CONTROL order by ID DESC";
		return jdbcTemplate.queryForObject(sql, Integer.class);
	}
	
	private void insertAccountBatchInfo() {
		String sql="INSERT INTO TBL_ACCOUNT_BATCH_INFO (ACCOUNT_TYPE,START_TIME,END_TIME,TARGET_DATASET,UPDATED_BY,UPDATED_TIME)"
				+" VALUES "
				+"(?,?,?,?,?,?)";
		int row=jdbcTemplate.update(sql, new Object[] { configProp.getUtBatchAccountInfoKey()
				, new Date()
				, new Date()
				, "1"
				,JOB_NAME
				,new Date()});
	}
	
	private void insertMaxKeyTargetSet() {
		String sql="INSERT INTO TBL_BATCH_CONFIG "
				+"(ID, PARAMETER_KEY, PARAMETER_VALUE,CREATED_TIME , CREATED_BY, UPDATED_TIME, UPDATED_BY )"
				+" VALUES "
				+"(?,?,?,?, ?, ?, ?)";
		jdbcTemplate.update(sql, new Object[] {3, configProp.getMaxTargetSetKey(), 2
				, new Date(), configProp.getBatchCode(), new Date(),configProp.getBatchCode()});
	}
	
	private void deleteData() {
		String sqlAccountInfo="DELETE FROM TBL_ACCOUNT_BATCH_INFO";
		jdbcTemplate.update(sqlAccountInfo);

		String sql=" DELETE FROM TBL_BATCH_CONFIG where PARAMETER_KEY=? ";
		jdbcTemplate.update(sql, new Object[] {configProp.getMaxTargetSetKey()});
		
		String sqlUtJobStatus="DELETE FROM TBL_BATCH_UT_JOB_STATUS_CONTROL";
		jdbcTemplate.update(sqlUtJobStatus);
		
		String delCust1=" DELETE FROM TBL_UT_CUSTOMER_1";
		String delCust2=" DELETE FROM TBL_UT_CUSTOMER_2";
		jdbcTemplate.batchUpdate(delCust1,delCust2);
		
		String delCustRel1=" DELETE FROM TBL_UT_CUSTOMER_REL_1";
		String delCustRel2=" DELETE FROM TBL_UT_CUSTOMER_REL_2";
		jdbcTemplate.batchUpdate(delCustRel1,delCustRel2);
		
		String delAccount1=" DELETE FROM TBL_UT_ACCOUNT_1";
		String delAccount2=" DELETE FROM TBL_UT_ACCOUNT_2";
		jdbcTemplate.batchUpdate(delAccount1,delAccount2);
		
		String delAccountHld1=" DELETE FROM TBL_UT_ACCOUNT_HOLDING_1";
		String delAccountHld2=" DELETE FROM TBL_UT_ACCOUNT_HOLDING_2";
		jdbcTemplate.batchUpdate(delAccountHld1,delAccountHld2);
		
		String delFund1=" DELETE FROM TBL_UT_FUND_MASTER_1";
		String delFund2=" DELETE FROM TBL_UT_FUND_MASTER_2";
		jdbcTemplate.batchUpdate(delFund1,delFund2);
	}
}
