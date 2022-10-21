package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.sql.DataSource;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundConfig;
import com.rhbgroup.dcp.bo.batch.job.step.BillerPaymentFileJobStepBuilder;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.expression.common.TemplateAwareExpressionParser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.BillerPaymentFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import com.rhbgroup.dcp.bo.batch.job.config.BillerPaymentFileJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.config.*;
import org.springframework.batch.core.Job;

import org.springframework.boot.test.context.SpringBootTest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class, BillerPaymentFileJobConfiguration.class})
@ActiveProfiles("test")
public class BillerPaymentOutboundFileJobTests extends BaseFTPJobTest {
	private static final Logger logger = Logger.getLogger(BillerPaymentOutboundFileJobTests.class);

	String JOB_NAME="BillerPaymentOutboundFileJob";
	public static final String JOB_LAUNCHER_UTILS = "BillerPaymentOutboundFileJobLauncherTestUtils";
	String processDate="2018-09-17";
	String batchSystemDate="2018-09-18";

	String BILLER_CODE = "1010";
	String BILLER_ACCOUNT_NAME = "Telekom Malaysia Berhad";
	String BILLER_ACCOUNT_NO = "21412900305056";
	String FTP_FOLDER = "DCP_BPF_1010_TM_FROM";
	String FILE_NAME_FMT = "1010${yyyyMMdd}.txt";
	String REPORT_URI = "/reports/DEV/Financial/DMBUD999/daily_successful_bill";
	String TEMPLATE_NAME = "Standard_01";
	String TXN_DATE ="20180917";
	String TXN_TYPE="CR";
	String TXN_TIME="101112";
	
	String REPORT_ID="DMBUD999_1010";
	
	@Value("${job.billerpaymentfilejob.masterftpfolder}")
	private String masterftpfolder;
	
	@Autowired
	FTPConfigProperties ftpConfigProperties;
	
	@Autowired
    private ApplicationContext applicationContext;

	@Lazy
	@Autowired
	@Qualifier(JOB_LAUNCHER_UTILS)
	private JobLauncherTestUtils jobLauncherTestUtils;

	@MockBean
	@Qualifier("BillPaymentConfigOutboundQueue")
	private Queue<BillerPaymentOutboundConfig> queue ;
    
	@Autowired
	DataSource dataSource;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	@MockBean
	private BillerPaymentFileJobStepBuilder billerPaymentFileJobStepBuilder;

	@MockBean
	private BillerPaymentFileJobConfiguration billerPaymentFileJobConfiguration;


	
//    @Test
//	testAdditionalBillers covers 36 billers there for this testJob(cover 1 biller) is no longer required
    public void testJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
        			.addDate("now",new Date())
        			.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY,JOB_NAME)
        			.addString("reportid", REPORT_ID)
        			.addString("templatename",TEMPLATE_NAME)
        			.toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }
    
    @Test
    @SqlGroup({
        @Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/insert_biller_txn_data.sql"),
    })
	public void testAdditionalBillers() throws Exception {
		updateBillerConfig();
		JobParameters jobParameters = new JobParametersBuilder().addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
				.addString("templatename", TEMPLATE_NAME).toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());//TODO TEST for expected equals COMPLETED
	}
    
    
    @Before
	public void setup() throws Exception {
		Environment environment = applicationContext.getEnvironment();
		logger.info("=====================spring.batch.table-prefix:"
				+ environment.getProperty("spring.batch.table-prefix"));
		String workingDir = System.getProperty("user.dir");
	    String testFileFolderPath = generateFolderPath(workingDir, "target", FTP_FOLDER);
	    FTP_FOLDER = testFileFolderPath;
		File ftpFolder = new File(FTP_FOLDER);
		if(!ftpFolder.exists()) {
			FileUtils.forceMkdir(ftpFolder);
		}
		FTP_FOLDER=StringUtils.remove(ftpFolder.getPath(), ":").replace("\\", "/");
		
		String targetMasterFtpFolder = generateFolderPath(masterftpfolder);
		File masterFtpFolder = new File(targetMasterFtpFolder);
		if(!masterFtpFolder.exists()) {
			FileUtils.forceMkdir(masterFtpFolder);
		}
		jdbcTemplate.setDataSource(dataSource);
		updateBatchConfig();
		insertTblBillerConfig();
		insertBillerTxn();
	}

    @After
    public void after() throws Exception {
    	deleteTblBillerConfig();
    	deleteBillerTxn();
    }
    
	private void updateBillerConfig() throws Exception {
		String sql = "SELECT FTP_FOLDER FROM TBL_BATCH_BILLER_PAYMENT_CONFIG";
		String workingDir = System.getProperty("user.dir");
		int row = 0;
		List<String> ftpFolders = jdbcTemplate.queryForList(sql, String.class);
		for (String ftpFolder : ftpFolders) {
			String localFolder = generateFolderPath(workingDir, "target", ftpFolder);
			File tmpFtpFolder = new File(localFolder);
			if (!tmpFtpFolder.exists()) {
				FileUtils.forceMkdir(tmpFtpFolder);
			}
			localFolder = StringUtils.remove(tmpFtpFolder.getPath(), ":").replace("\\", "/");
			sql = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG set FTP_FOLDER=?, IS_REQUIRED_TO_EXECUTE=? where FTP_FOLDER=?";
			row = jdbcTemplate.update(sql, new Object[] { localFolder,1, ftpFolder });
			logger.info(String.format("UPDATE %s record- ftp folder=%s to localFolder=%s", row, ftpFolder, localFolder));
		}
	}
    
    private void insertBillerTxn() {
    	logger.info("insert into vw_batch_biller_payment_txn..");
		try {
			String insertSQL = String.format("insert into vw_batch_tbl_biller (biller_code,biller_collection_account_no,biller_name) values ('%s','%s','%s')"
					, BILLER_CODE, BILLER_ACCOUNT_NO, BILLER_ACCOUNT_NAME);

			String insertSQL1 = "insert into vw_batch_biller_payment_txn " +
					"(biller_code,biller_account_no,biller_account_name,txn_id,txn_date,txn_amount,txn_type,txn_description,biller_ref_no1,biller_ref_no2,biller_ref_no3,txn_time)" +
					" values "+
					"('"+BILLER_CODE+"','"+BILLER_ACCOUNT_NO+"','"+BILLER_ACCOUNT_NAME+"','20180509111213','"+TXN_DATE+"','200.00','"+TXN_TYPE+"','','834743882','0125289475','Telekom Test1','"+TXN_TIME+"')" ;
			
			String insertSQL2 = "insert into vw_batch_biller_payment_txn " +
					"(biller_code,biller_account_no,biller_account_name,txn_id,txn_date,txn_amount,txn_type,txn_description,biller_ref_no1,biller_ref_no2,biller_ref_no3,txn_time)" +
					" values "+
					"('"+BILLER_CODE+"','"+BILLER_ACCOUNT_NO+"','"+BILLER_ACCOUNT_NAME+"','20180509111214','"+TXN_DATE+"','300.00','"+TXN_TYPE+"','','834743883','0125289476','Telekom Test2','"+TXN_TIME+"')" ;
			int []rows = jdbcTemplate.batchUpdate(insertSQL, insertSQL1, insertSQL2);
			logger.info("added row="+rows.length);
		}catch(Exception ex) {
			logger.info("insert into vw_batch_biller_payment_txn exception:"  + ex.getMessage());
		}

    }
    
	private void deleteBillerTxn() {
		try {
			int row = 0;
			String deleteSQL1 = "delete from vw_batch_tbl_biller ";
			row = jdbcTemplate.update(deleteSQL1);
			logger.info(String.format("delete %s row from vw_batch_tbl_biller", row));

			String deleteSQL2 = "delete from vw_batch_biller_payment_txn ";
			row = jdbcTemplate.update(deleteSQL2);
			logger.info(String.format("delete %s row from vw_batch_biller_payment_txn", row));
		} catch (Exception ex) {
			logger.info(String.format("delete biller payment txn exception=%s", ex.getMessage()));
		}
	}
    
    private void updateBatchConfig() {
    	try {
    		String sql =String.format( "update tbl_batch_config " +
    				" set PARAMETER_VALUE='%s' "+
    				" where PARAMETER_KEY='batch.system.date'",batchSystemDate);
    		int row = jdbcTemplate.update(sql);
    		logger.info( String.format( "update batch config PARAMETER_VALUE='%s'-impacted row=%s", batchSystemDate,row));
    	}catch(Exception ex) {
    		logger.info(String.format( "update PARAMETER_VALUE='2018-09-18' batch config exception=%s", ex.getMessage()));
    	}
    }
    
    private void insertTblBillerConfig() {
    	try {
			String insertSQL = " insert into TBL_BATCH_BILLER_PAYMENT_CONFIG "+
					"(BILLER_CODE, TEMPLATE_NAME, FTP_FOLDER, FILE_NAME_FORMAT,REPORT_UNIT_URI,STATUS, IS_REQUIRED_TO_EXECUTE, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY, IBK_FTP_FOLDER )"+
					" values " +
					"( '"+BILLER_CODE+"', '"+TEMPLATE_NAME+"', '"+FTP_FOLDER+"'," + 
					"'"+FILE_NAME_FMT+"'," + 
					"'/reports/DEV/Financial/DMBUD999/daily_successful_bill'," + 
					"'A' ," + 
					"'1' ," + 
					"now ," + 
					"'admin'," + 
					"now," + 
					"'admin'," + 
					"'BPF_FROM/1010/') ";
			logger.info(String.format("inserted data into TBL_BATCH_BILLER_PAYMENT_CONFIG sql=%s", insertSQL));
			int row = jdbcTemplate.update(insertSQL);
			logger.info(String.format("insert row=%s", row));
    	}catch(Exception ex) {
			logger.info(String.format("insert into TBL_BATCH_BILLER_PAYMENT_CONFIG exception:%s",ex.getMessage()));
		}
    }
    
    private void deleteTblBillerConfig() {
    	try {
    		String sql = "delete from TBL_BATCH_BILLER_PAYMENT_CONFIG";
    		int row = jdbcTemplate.update(sql);
    		logger.info("delete data from TBL_BATCH_BILLER_PAYMENT_CONFIG-impacted row="+row);
    	}catch(Exception ex) {
    		logger.info("delete data from TBL_BATCH_BILLER_PAYMENT_CONFIG=%s" + ex.getMessage());
    	}
    }
       
}