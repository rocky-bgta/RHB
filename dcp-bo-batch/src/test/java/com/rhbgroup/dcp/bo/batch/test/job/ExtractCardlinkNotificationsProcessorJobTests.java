package com.rhbgroup.dcp.bo.batch.test.job;

import java.util.Date;

import javax.sql.DataSource;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
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

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.config.ExtractCardlinkNotificationsProcessorJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchCardlinkNotificationsRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfigHSQL.class, ExtractCardlinkNotificationsProcessorJobConfiguration.class})
@ActiveProfiles("test")
public class ExtractCardlinkNotificationsProcessorJobTests extends BaseJobTest {

	private static final Logger logger = Logger.getLogger(ExtractCardlinkNotificationsProcessorJobTests.class);

	public static final String JOB_NAME = "ExtractCardlinkNotificationsProcessorJob";
	public static final String JOB_LAUNCHER_UTILS = "ExtractCardlinkNotificationsProcessorJobLauncherTestUtils";
	
	@Lazy
	@Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private BatchCardlinkNotificationsRepositoryImpl batchCardlinkNotificationsRepositoryImpl;
	
	@Autowired
	private BatchParameterRepositoryImpl batchParameterRepository;
	
	@Autowired
	DataSource dataSource;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
		
    //column [for view notification]
	private String FILE_NAME = "101020180516.txt";
	private String PROCESS_DATE = "20181009";
	private String EVENT_CODE = "50000";
	private String KEY_TYPE ="CC";
	private String SYSTEM_DATE = "20181010";
	private String SYSTEM_TIME = "1010101";
	private String CARD_NUMBER = "0004570660000114040";
	private String PAYMENT_DUE_DATE = "2018111";
	private String CARD_TYPE = "P";
	private String MINIMUM_AMOUNT = "150.00";
	private String OUTSTANDING_AMOUNT = "1200.00";
	private String STATEMENT_AMOUNT = "330.00";
	private String STATEMENT_DATE = "20181010";
	private Long NOTIFICATION_RAW_ID = 0L;
	private Long USER_ID= 3L;

    @Before
	public void setup() throws Exception {

    	//setup data source
		jdbcTemplate.setDataSource(dataSource);
		//add job process data
		batchParameterRepository.updateBatchParameter(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2018-10-10");
		
		//pre-populate raw staging data
		insertNotificationRawData();
		//pre-populate view notifications table
		insertViewNotificationData();
	}
    
    @Test
    public void testPositiveExtractCardlinkPreprocessor() throws Exception {
    	

		JobParameters jobParameters = new JobParametersBuilder()
	    		.addDate("now", new Date())
	    		.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
	    		.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, "20181010")
	    		.toJobParameters();
	        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
	        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }
    
    @Test
    public void testExtractCardlinkPreprocessorWithoutProcessDateKey() throws Exception {
    	

		JobParameters jobParameters = new JobParametersBuilder()
	    		.addDate("now", new Date())
	    		.addString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, JOB_NAME)
	    		.toJobParameters();
	        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
	        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }


    @After
    public void cleanUp() throws Exception {
    	deleteViewNotificationTable();
    	deleteBatchStagedNotificationTable();
    	deleteNotificationRawDataBasedOnJobId();
    }

    private void insertNotificationRawData() {
    	logger.info("insert into TBL_BATCH_STAGED_NOTIFICATION_RAW..");
		try {
			String insertSQL = String.format("insert into TBL_BATCH_STAGED_NOTIFICATION_RAW (JOB_EXECUTION_ID,FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,DATA_1,DATA_2,DATA_3,DATA_4,DATA_5,DATA_6,DATA_7,DATA_8,DATA_9,DATA_10,IS_PROCESSED,CREATED_TIME,CREATED_BY,UPDATED_TIME,UPDATED_BY) " +
											 " values ('%d','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')"
					,0 , FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE,"1","2","3","4","5","6","7","8","9","10","0","2018-10-09 10:10:10","admin","2018-10-09 10:10:10","admin");
			String insertSQL2 = String.format("insert into TBL_BATCH_STAGED_NOTIFICATION_RAW (JOB_EXECUTION_ID,FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,DATA_1,DATA_2,DATA_3,DATA_4,DATA_5,DATA_6,DATA_7,DATA_8,DATA_9,DATA_10,IS_PROCESSED,CREATED_TIME,CREATED_BY,UPDATED_TIME,UPDATED_BY) " +
					 " values ('%d','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')"
					,0 , FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE,"1","2","3","4","5","6","7","8","9","10","0","2018-10-09 10:10:10","admin","2018-10-09 10:10:10","admin");
			
			String insertSQL3 = String.format("insert into TBL_BATCH_STAGED_NOTIFICATION_RAW (JOB_EXECUTION_ID,FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,DATA_1,DATA_2,DATA_3,DATA_4,DATA_5,DATA_6,DATA_7,DATA_8,DATA_9,DATA_10,IS_PROCESSED,CREATED_TIME,CREATED_BY,UPDATED_TIME,UPDATED_BY) " +
					 " values ('%d','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')"
					 ,0 , FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE,"1","2","3","4","5","6","7","8","9","10","0","2018-10-09 10:10:10","admin","2018-10-09 10:10:10","admin");
			
			int []rows = jdbcTemplate.batchUpdate(insertSQL,insertSQL2,insertSQL3);
			logger.info("added row="+rows.length);
		}catch(Exception ex) {
			logger.info("insert into TBL_BATCH_STAGED_NOTIFICATION_RAW exception:"  + ex.getMessage());
		}
    }
    
    private void insertViewNotificationData() {
    	logger.info("insert into vw_batch_cardlink_notification..");
		try {
			String insertSQL = String.format("insert into vw_batch_cardlink_notification (FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,SYSTEM_DATE,SYSTEM_TIME,CARD_NUMBER,PAYMENT_DUE_DATE,CARD_TYPE,MINIMUM_AMOUNT,OUTSTANDING_AMOUNT,STATEMENT_AMOUNT,STATEMENT_DATE,NOTIFICATION_RAW_ID,USER_ID) " +
											 " values ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%d','%d')"
					, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, SYSTEM_DATE, SYSTEM_TIME, CARD_NUMBER, PAYMENT_DUE_DATE, CARD_TYPE, MINIMUM_AMOUNT, OUTSTANDING_AMOUNT, STATEMENT_AMOUNT, STATEMENT_DATE, 1, USER_ID);

			String insertSQL2 = String.format("insert into vw_batch_cardlink_notification (FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,SYSTEM_DATE,SYSTEM_TIME,CARD_NUMBER,PAYMENT_DUE_DATE,CARD_TYPE,MINIMUM_AMOUNT,OUTSTANDING_AMOUNT,STATEMENT_AMOUNT,STATEMENT_DATE,NOTIFICATION_RAW_ID,USER_ID) " +
					 " values ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%d','%d')"
					, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, SYSTEM_DATE, SYSTEM_TIME, "0004570660000114041", PAYMENT_DUE_DATE, "S", MINIMUM_AMOUNT, "3000.00", "400.00", STATEMENT_DATE, 2, USER_ID);
			
			String insertSQL3 = String.format("insert into vw_batch_cardlink_notification (FILE_NAME,PROCESS_DATE,EVENT_CODE,KEY_TYPE,SYSTEM_DATE,SYSTEM_TIME,CARD_NUMBER,PAYMENT_DUE_DATE,CARD_TYPE,MINIMUM_AMOUNT,OUTSTANDING_AMOUNT,STATEMENT_AMOUNT,STATEMENT_DATE,NOTIFICATION_RAW_ID,USER_ID) " +
					 " values ('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%d','%d')"
					, FILE_NAME, PROCESS_DATE, EVENT_CODE, KEY_TYPE, SYSTEM_DATE, SYSTEM_TIME, "0004570660000114042", PAYMENT_DUE_DATE, "S", MINIMUM_AMOUNT, "50000.00", "500.00", STATEMENT_DATE, 3, USER_ID);
			
			int []rows = jdbcTemplate.batchUpdate(insertSQL, insertSQL2, insertSQL3);
			logger.info("added row="+rows.length);
		}catch(Exception ex) {
			logger.info("insert into vw_batch_cardlink_notification exception:"  + ex.getMessage());
		}
    }
	
	private void deleteViewNotificationTable() {
		try {
			int row = 0;
			String deleteSQL1 = "delete from vw_batch_cardlink_notification ";
			row = jdbcTemplate.update(deleteSQL1);
			logger.info(String.format("delete %s row from vw_batch_cardlink_notification", row));

		} catch (Exception ex) {
			logger.info(String.format("delete vw_batch_cardlink_notification exception=%s", ex.getMessage()));
		}
	}
	
	private void deleteBatchStagedNotificationTable() {
		try {
			int row = 0;
			String deleteSQL1 = "delete from TBL_BATCH_STAGED_NOTIFICATION ";
			row = jdbcTemplate.update(deleteSQL1);
			logger.info(String.format("delete %s row from TBL_BATCH_STAGED_NOTIFICATION", row));
		} catch (Exception ex) {
			logger.info(String.format("delete TBL_BATCH_STAGED_NOTIFICATION exception=%s", ex.getMessage()));
		}
	}
	
	private void deleteNotificationRawDataBasedOnJobId() {
		try {
			int row = 0;
			String deleteSQL1 = "delete from TBL_BATCH_STAGED_NOTIFICATION_RAW where JOB_EXECUTION_ID IN (0)";
			row = jdbcTemplate.update(deleteSQL1);
			logger.info(String.format("delete %s row from TBL_BATCH_STAGED_NOTIFICATION_RAW", row));
		} catch (Exception ex) {
			logger.info(String.format("delete TBL_BATCH_STAGED_NOTIFICATION_RAW exception=%s", ex.getMessage()));
		}		
	}
	
}
