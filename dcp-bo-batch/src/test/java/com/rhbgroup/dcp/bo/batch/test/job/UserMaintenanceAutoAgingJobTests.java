package com.rhbgroup.dcp.bo.batch.test.job;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.job.repository.BoUserRepositoryImpl;
import freemarker.template.Configuration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.enums.UserStatus;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.job.config.UserMaintenanceAutoAgingJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.model.BoConfigGeneric;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(JMSUtils.class)
@PowerMockIgnore(value= { "javax.net.ssl.*" })
@SpringBootTest(classes= {BatchTestConfigHSQL.class, UserMaintenanceAutoAgingJobConfiguration.class})
@ActiveProfiles("test")
public class UserMaintenanceAutoAgingJobTests extends BaseJobTest {

	public static final String JOB_NAME = "UserMaintenanceAutoAgingJob";
	public static final String JOB_LAUNCHER_UTILS = "UserMaintenanceAutoAgingJobLauncherTestUtils";
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'+08:00'";
	
    @Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;

	@MockBean(name="boUserRepository")
	private BoUserRepositoryImpl boUserRepository;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

    class CustomBoConfigGenericRowMapper implements RowMapper<BoConfigGeneric> {
		@Override
		public BoConfigGeneric mapRow(ResultSet rs, int rowNum) throws SQLException {
			BoConfigGeneric boConfigGeneric = new BoConfigGeneric();
			boConfigGeneric.setId(rs.getInt("ID"));
			
			return boConfigGeneric;
		}
    }
    
    /*
     * Test to ensure the test able to pickup the DB config successfully and use them in the test
     */
    @Test
    public void testPositiveJobWithDBConfigParams() throws Exception {
		when(boUserRepository.getUserID(Mockito.anyString())).thenReturn("-1");
    	String beforeSQL1 = "DELETE FROM TBL_BO_USER WHERE USERNAME LIKE 'TESTER%' AND USER_STATUS_ID in ('A','I')";
    	String beforeSQL2 = "DELETE FROM TBL_BO_CONFIG_GENERIC WHERE CONFIG_TYPE = 'user_department' and CONFIG_CODE like 'TESTER%'";
    	String beforeSQL3 = "INSERT INTO TBL_BO_CONFIG_GENERIC (CONFIG_TYPE, CONFIG_CODE, CONFIG_DESC, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY)VALUES('user_department', 'TEST_DEPARTMENT1', 'TEST_DEPARTMENT1', NOW(), 'TESTER', NOW(), 'TESTER')";
    	jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3);
    	
    	String selectSQL = "SELECT ID FROM TBL_BO_CONFIG_GENERIC WHERE CONFIG_TYPE = 'user_department' AND CONFIG_CODE = 'TEST_DEPARTMENT1' AND CONFIG_DESC = 'TEST_DEPARTMENT1'";
    	BoConfigGeneric boConfigGeneric = jdbcTemplate.query(selectSQL, new CustomBoConfigGenericRowMapper()).get(0);
    	
    	Date currentDate = new Date();
    	String diff1Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -1), DATE_FORMAT);
    	String diff29Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -29), DATE_FORMAT);
    	String diff30Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -30), DATE_FORMAT);
    	String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), DATE_FORMAT);
    	String diff89Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -89), DATE_FORMAT);
    	String diff90Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -90), DATE_FORMAT);
    	String diff91Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -91), DATE_FORMAT);
    	
    	String insertSQL1 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER1', 'tester1@rhbgroup.com', 'TESTER1', %d, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		boConfigGeneric.getId(), UserStatus.ACTIVE.getStatus(), diff1Days);
    	String insertSQL2 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER2', 'tester2@rhbgroup.com', 'TESTER2', %d, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		boConfigGeneric.getId(), UserStatus.ACTIVE.getStatus(), diff29Days);
    	String insertSQL3 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER3', 'tester3@rhbgroup.com', 'TESTER3', %d, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		boConfigGeneric.getId(), UserStatus.ACTIVE.getStatus(), diff30Days);
    	String insertSQL4 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER4', 'tester4@rhbgroup.com', 'TESTER4', %d, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		boConfigGeneric.getId(), UserStatus.ACTIVE.getStatus(), diff31Days);
    	String insertSQL5 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER5', 'tester5@rhbgroup.com', 'TESTER5', %d, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
        	boConfigGeneric.getId(), UserStatus.INACTIVE.getStatus(), diff89Days);
    	String insertSQL6 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER6', 'tester6@rhbgroup.com', 'TESTER6', %d, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		boConfigGeneric.getId(), UserStatus.INACTIVE.getStatus(), diff90Days);
    	String insertSQL7 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER7', 'tester7@rhbgroup.com', 'TESTER7', %d, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		boConfigGeneric.getId(), UserStatus.INACTIVE.getStatus(), diff91Days);
    	String insertSQL8 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER8', 'tester8@rhbgroup.com', 'TESTER8', %d, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		boConfigGeneric.getId(), UserStatus.ACTIVE.getStatus(), diff91Days);
    	jdbcTemplate.batchUpdate(insertSQL1, insertSQL2, insertSQL3, insertSQL4, insertSQL5, insertSQL6, insertSQL7, insertSQL8);
    	
    	PowerMockito.mockStatic(JMSUtils.class);
    	
        JobParameters jobParameters = new JobParametersBuilder()
    		.addDate("now", new Date())
    		.addString("jobname", JOB_NAME)
    		.toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
        
        String hostIp = InetAddress.getLocalHost().getHostAddress();

        /*
        PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(String.format("\\{\"eventCode\":\"20019\",\"userId\":-1,\"username\":\"User Maintenance Auto Aging \\(LDCPD5003B\\)\",\"department\":\"BO Batch & Report\",\"statusCode\":\"\",\"statusDescription\":\"Success\",\"timestamp\":\".*\",\"ip\":\"%s\",\"request\":\\{\"tableBatchAutoAging\":\\{\"userId\":\"4\",\"name\":\"TESTER4\",\"email\":\"tester4@rhbgroup.com\",\"department\":\"%d\",\"current_user_status\":\"A\",\"new_user_status\":\"I\",\"last_login_time\":\"%s\"\\}\\},\"response\":\\{\"status\":\"Status successfully updated\"\\},\"additionalData\":\\{\"staffId\":\"4\"\\}\\}",
				hostIp, boConfigGeneric.getId(), DateUtils.convertDateFormat(diff31Days, DATE_FORMAT, JSON_DATE_FORMAT).replace("+", "\\+"))),
    		Mockito.any());
        
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(String.format("\\{\"eventCode\":\"20019\",\"userId\":-1,\"username\":\"User Maintenance Auto Aging \\(LDCPD5003B\\)\",\"department\":\"BO Batch & Report\",\"statusCode\":\"\",\"statusDescription\":\"Success\",\"timestamp\":\".*\",\"ip\":\"%s\",\"request\":\\{\"tableBatchAutoAging\":\\{\"userId\":\"7\",\"name\":\"TESTER7\",\"email\":\"tester7@rhbgroup.com\",\"department\":\"%d\",\"current_user_status\":\"I\",\"new_user_status\":\"D\",\"last_login_time\":\"%s\"\\}}\\,\"response\":\\{\"status\":\"Status successfully updated\"\\},\"additionalData\":\\{\"staffId\":\"7\"\\}\\}",
				hostIp, boConfigGeneric.getId(), DateUtils.convertDateFormat(diff91Days, DATE_FORMAT, JSON_DATE_FORMAT).replace("+", "\\+"))),
    		Mockito.any());
		
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(String.format("\\{\"eventCode\":\"20019\",\"userId\":-1,\"username\":\"User Maintenance Auto Aging \\(LDCPD5003B\\)\",\"department\":\"BO Batch & Report\",\"statusCode\":\"\",\"statusDescription\":\"Success\",\"timestamp\":\".*\",\"ip\":\"%s\",\"request\":\\{\"tableBatchAutoAging\":\\{\"userId\":\"8\",\"name\":\"TESTER8\",\"email\":\"tester8@rhbgroup.com\",\"department\":\"%d\",\"current_user_status\":\"A\",\"new_user_status\":\"D\",\"last_login_time\":\"%s\"\\}},\"response\":\\{\"status\":\"Status successfully updated\"\\},\"additionalData\":\\{\"staffId\":\"8\"\\}\\}", 
				hostIp, boConfigGeneric.getId(), DateUtils.convertDateFormat(diff91Days, DATE_FORMAT, JSON_DATE_FORMAT).replace("+", "\\+"))),
    		Mockito.any());
		*/
		checkTblBatchUserMaintAutoAgingStatus("TESTER4", "I", 1);
		checkTblBatchUserMaintAutoAgingStatus("TESTER7", "D", 1);
		checkTblBatchUserMaintAutoAgingStatus("TESTER8", "D", 1);
		
		checkTblBoUserStatus("TESTER1","A");
		checkTblBoUserStatus("TESTER2","A");
		checkTblBoUserStatus("TESTER3","A");
		//checkTblBoUserStatus("TESTER4","I");
		checkTblBoUserStatus("TESTER5","I");
		checkTblBoUserStatus("TESTER6","I");
		//checkTblBoUserStatus("TESTER7","D");
		//checkTblBoUserStatus("TESTER8","D");
		
        String afterSQL1 = beforeSQL1;
        String afterSQL2 = beforeSQL2;
        jdbcTemplate.batchUpdate(afterSQL1, afterSQL2);
    }
    
    private void checkTblBatchUserMaintAutoAgingStatus(String username, String newStatus, int isProcessed) {
    	String selectSQL = String.format("SELECT COUNT(*) COUNTER FROM TBL_BATCH_USER_MAINT_AUTO_AGING WHERE NAME = '%s' AND NEW_USER_STATUS_ID = '%s' AND IS_PROCESSED = %d", username, newStatus, isProcessed);
		List<Map<String, Object>> results = jdbcTemplate.queryForList(selectSQL);
        long counter = (long)results.get(0).get("COUNTER");
		assertEquals(0, counter);
    }
    
    private void checkTblBoUserStatus(String username, String userStatus) {
    	String selectSQL = String.format("SELECT COUNT(*) COUNTER FROM TBL_BO_USER WHERE USERNAME='%s' AND USER_STATUS_ID='%s'", username, userStatus);
    	List<Map<String, Object>> results = jdbcTemplate.queryForList(selectSQL);
        long counter = (long)results.get(0).get("COUNTER");
		assertEquals(1, counter);
    }
}