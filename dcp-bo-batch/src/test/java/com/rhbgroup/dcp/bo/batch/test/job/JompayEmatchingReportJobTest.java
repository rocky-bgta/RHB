package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static org.junit.Assert.assertEquals;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;

import java.io.File;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.JompayEmatchingReportJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class,JompayEmatchingReportJobConfiguration.class})
@ActiveProfiles("test")
public class JompayEmatchingReportJobTest extends BaseFTPJobTest {
	static final Logger logger = Logger.getLogger(JompayEmatchingReportJobTest.class);
	
	String JOB_NAME="JompayEmatchingReportJob";
	
	@Autowired
	DataSource dataSource;
	
	@Autowired
	private FTPConfigProperties ftpConfigProperties;
	
	@Value("${job.jompayematchingreportjob.ftpfolder}")
	private String ftpTargetFolder;
	
	@Autowired
	private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("JompayEmatchingReportJobJobLauncherTestUtils")
    private JobLauncherTestUtils jobLauncherTestUtils;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	@Test
	public void testJob() throws Exception {
		logger.info("test job " +  (new Date()));
		insertVwMergeJompay();
        JobParameters jobParameters = new JobParametersBuilder()
        			.addDate("now",new Date())
        			.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
        			.addString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-08-21")
        			.toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
		logger.info("END test " +  (new Date()));
	}

	/*	simon - disable this test - causing performance slowdown	- GC OUT OF MEMORY!!!
	@Test

	//@Test
	public void testMoreThan10000DataJob() throws Exception {
		logger.info("Test More Than 10000 Data Job" +  (new Date()));
		for(int i = 0; i < 11000; i++) {
			insertVwMergeJompay();
		}
        JobParameters jobParameters = new JobParametersBuilder()
        			.addDate("now",new Date())
        			.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
        			.addString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-08-21")
        			.toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
		logger.info("END test " +  (new Date()));
	}
*/
	@Before
	public void setup() throws Exception {
		Environment environment = applicationContext.getEnvironment();
		System.out.println("=====================spring.batch.table-prefix:"
				+ environment.getProperty("spring.batch.table-prefix"));
		File ftpFolder = new File(ftpTargetFolder);
		if(!ftpFolder.exists()) {
			FileUtils.forceMkdir(ftpFolder);
		}
		logger.info("starting up sFTP Server....");
		jdbcTemplate.setDataSource(dataSource);
		//insertVwMergeJompay();
		updateBatchConfig();
		logger.info("sFTP Server started");
	}
	
	private void insertVwMergeJompay() {
		String sql = "";
		int row = 0;
		try {
			sql = "insert into vw_batch_merged_jompay_ematching "
					+ "(channel_id,channel_status,application_id,acct_ctrl1,acct_ctrl2,acct_ctrl3,account_no,debit_credit_ind,user_tran_code,amount,txn_branch,txn_date,txn_time)"
					+ " values "
					+ "('IBK','00','ST','18','458','068','123456789','D','6360',9999.00,22,'20180820','10:10:10')";
			row = jdbcTemplate.update(sql);
			logger.info(String.format("insert %s into vw_batch_merged_jompay_ematching", row));
		}catch(Exception ex) {
			logger.error(ex);
		}
	}

	private void updateBatchConfig() {
		try {
			String sql = String.format("update tbl_batch_config " + " set PARAMETER_VALUE='%s' "
					+ " where PARAMETER_KEY='batch.system.date'", "2018-08-21");
			int row = jdbcTemplate.update(sql);
			logger.info(String.format("update batch config PARAMETER_VALUE='%s'-impacted row=%s", "2018-08-21", row));
		} catch (Exception ex) {
			logger.info(
					String.format("update PARAMETER_VALUE='2018-08-21' batch config exception=%s", ex.getMessage()));
		}
	}
	 
	
	private void deleteDataRecord() {
		try {
			String sql="";
			int row=0;
			sql="delete from vw_batch_merged_jompay_ematching";
			row = jdbcTemplate.update(sql);
			logger.info(String.format("delete %s row vw_batch_merged_jompay_ematching", row));
		} catch (Exception ex) {
			logger.info(String.format("delete data exception=%s", ex.getMessage()));
		}
	}
	
	@After
	public void after() throws Exception {
		deleteDataRecord();
	}
}