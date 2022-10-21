package com.rhbgroup.dcp.bo.batch.test.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.naming.NamingException;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jms.InvalidDestinationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.enums.UserStatus;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.model.JMSConfig;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JMSUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.UserMaintenanceAutoAgingJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUserMaintAutoAging;
import com.rhbgroup.dcp.bo.batch.job.model.BoConfigGeneric;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchSuspenseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUserMaintAutoAgingRespositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BoUserRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(JMSUtils.class)
@PowerMockIgnore(value= { "javax.net.ssl.*" })
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class UpdateUserAccountStatusStepTest extends BaseJobTest {

	private static final String STEP_NAME = "UpdateUserAccountStatusStep";

	private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private static final String JOB_ID = "LDCPD5003B";
	
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss:SSS'+08:00'";
	
	private static final String EXPECTED_NOTIFICATION_JSON_TEMPLATE = "{\"eventCode\":\"%s\",\"userId\":%d,\"username\":\"%s\",\"department\":\"%s\",\"statusCode\":\"%s\",\"statusDescription\":\"%s\",\"timestamp\":\".*\",\"ip\":\"%s\",\"request\":{\"tableBatchAutoAging\":{\"userId\":\"%s\",\"name\":\"%s\",\"email\":\"%s\",\"department\":\"%s\",\"current_user_status\":\"%s\",\"new_user_status\":\"%s\",\"last_login_time\":\"%s\"}},\"response\":{%s},\"additionalData\":{\"staffId\":\"%s\"}}";
	private static final String JSON_SUCCESS_RESPONSE = "\"status\":\"Status successfully updated\"";
	private static final String JSON_ERROR_RESPONSE = "\"error\":{\"suspense_type\":\"EXCEPTION\",\"suspense_message\":\"%s\"}";
	private static final String AUDIT_MESSAGE_SUCCESS_STATUS_CODE = "";
	private static final String AUDIT_MESSAGE_UNSUCCESS_STATUS_CODE = "80000";
	private static final String AUDIT_MESSAGE_SUCCESS_DESCRIPTION = "Success";
	private static final String AUDIT_MESSAGE_UNSUCCESS_DESCRIPTION = "Error";
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchUserMaintAutoAging> itemReader;
	
	@Autowired
    @Qualifier(STEP_NAME + ".ItemProcessor")
    private ItemProcessor<BatchUserMaintAutoAging, BatchUserMaintAutoAging> itemProcessor;
    
	@Autowired
    @Qualifier(STEP_NAME + ".ItemWriter")
    private ItemWriter<BatchUserMaintAutoAging> itemWriter;
	
	@Autowired
	private UserMaintenanceAutoAgingJobConfigProperties jobConfigProperties;
	
	@MockBean
	private BatchSuspenseRepositoryImpl batchSuspenseRepository;
	
	@MockBean
	private BatchUserMaintAutoAgingRespositoryImpl batchUserMaintAutoAgingRespository;
	
	@MockBean
	private BoUserRepositoryImpl boUserRepository;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	private StepExecution stepExecution;
	
	public StepExecution getStepExection() {
		Map<String, Object> jobParamMap = new HashMap<>();
		Map<String, Object> executionContextMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_DAYS_TO_INACTIVE_USER, 30);
		executionContextMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_DB_DAYS_TO_DELETE_USER, 90);
		stepExecution = createStepExecution(STEP_NAME, jobParamMap, executionContextMap);
		
		return stepExecution;
	}
	
	@Before
	public void beforeLocalTest() {
		super.beforeTest();
		Mockito.reset(batchSuspenseRepository);
		Mockito.reset(batchUserMaintAutoAgingRespository);
		Mockito.reset(boUserRepository);
	}
	
	@After
	public void afterLocalTest() {
		stepExecution.getJobExecution().getExecutionContext().remove(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID);
	}
	
	class CustomBoConfigGenericRowMapper implements RowMapper<BoConfigGeneric> {
		@Override
		public BoConfigGeneric mapRow(ResultSet rs, int rowNum) throws SQLException {
			BoConfigGeneric boConfigGeneric = new BoConfigGeneric();
			boConfigGeneric.setId(rs.getInt("ID"));
			boConfigGeneric.setConfigCode(rs.getString("CONFIG_CODE"));
			
			return boConfigGeneric;
		}
    }
	
	/*
	 * Test to ensure reader able to read the records from DB and generate the objects accordingly
	 */
	@Test
	public void testPositiveReader() throws Exception {
		String beforeSQL1 = "DELETE FROM TBL_BO_USER WHERE USERNAME LIKE 'TESTER%' AND USER_STATUS_ID in ('A','I')";
    	String beforeSQL2 = "DELETE FROM TBL_BO_CONFIG_GENERIC WHERE CONFIG_TYPE = 'user_department' and CONFIG_CODE like 'TESTER%'";
    	String beforeSQL3 = "INSERT INTO TBL_BO_CONFIG_GENERIC (CONFIG_TYPE, CONFIG_CODE, CONFIG_DESC, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY)VALUES('user_department', '9999', 'FINANCE', NOW(), 'TESTER', NOW(), 'TESTER')";
    	jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3);
    	
    	String selectSQL = "SELECT ID, CONFIG_CODE FROM TBL_BO_CONFIG_GENERIC WHERE CONFIG_TYPE = 'user_department' AND CONFIG_CODE = '9999' AND CONFIG_DESC = 'FINANCE'";
    	BoConfigGeneric boConfigGeneric = jdbcTemplate.query(selectSQL, new CustomBoConfigGenericRowMapper()).get(0);
    	
    	Date currentDate = new Date();
    	String diff1Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -1), TIME_FORMAT);
    	String diff29Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -29), TIME_FORMAT);
    	String diff30Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -30), TIME_FORMAT);
    	String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), TIME_FORMAT);
    	String diff89Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -89), TIME_FORMAT);
    	String diff90Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -90), TIME_FORMAT);
    	String diff91Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -91), TIME_FORMAT);
    	int departmentId = boConfigGeneric.getId();
    	String department = boConfigGeneric.getConfigCode();
    	
    	String insertSQL1 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER1', 'tester1@rhbgroup.com', 'TESTER1', %s, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		departmentId, UserStatus.ACTIVE.getStatus(), diff1Days);
    	String insertSQL2 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER2', 'tester2@rhbgroup.com', 'TESTER2', %s, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		departmentId, UserStatus.ACTIVE.getStatus(), diff29Days);
    	String insertSQL3 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER3', 'tester3@rhbgroup.com', 'TESTER3', %s, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		departmentId, UserStatus.ACTIVE.getStatus(), diff30Days);
    	String insertSQL4 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER4', 'tester4@rhbgroup.com', 'TESTER4', %s, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		departmentId, UserStatus.ACTIVE.getStatus(), diff31Days);
    	String insertSQL5 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER5', 'tester5@rhbgroup.com', 'TESTER5', %s, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
        	departmentId, UserStatus.INACTIVE.getStatus(), diff89Days);
    	String insertSQL6 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER6', 'tester6@rhbgroup.com', 'TESTER6', %s, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		departmentId, UserStatus.INACTIVE.getStatus(), diff90Days);
    	String insertSQL7 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER7', 'tester7@rhbgroup.com', 'TESTER7', %s, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		departmentId, UserStatus.INACTIVE.getStatus(), diff91Days);
    	String insertSQL8 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER8', 'tester8@rhbgroup.com', 'TESTER8', %s, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		departmentId, UserStatus.ACTIVE.getStatus(), diff91Days);
    	jdbcTemplate.batchUpdate(insertSQL1, insertSQL2, insertSQL3, insertSQL4, insertSQL5, insertSQL6, insertSQL7, insertSQL8);
    	
    	List<BatchUserMaintAutoAging> results = 
    		StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchUserMaintAutoAging>>() {
				public List<BatchUserMaintAutoAging> call() throws Exception {
					BatchUserMaintAutoAging batchUserMaintAutoAging;
					List<BatchUserMaintAutoAging> batchUserMaintAutoAgings = new ArrayList<>();
					while((batchUserMaintAutoAging = itemReader.read()) != null) {
						batchUserMaintAutoAgings.add(batchUserMaintAutoAging);
					}
					return batchUserMaintAutoAgings;
				}
		});
    	
    	assertEquals(5, results.size());
    	BatchUserMaintAutoAging expecteBatchUserMaintAutoAging1 = createBatchUserMaintAutoAging(
   			0L, 4, "TESTER4", "TESTER4", "tester4@rhbgroup.com", department, "A", null, DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
    	BatchUserMaintAutoAging expecteBatchUserMaintAutoAging2 = createBatchUserMaintAutoAging(
   			0L, 5, "TESTER5", "TESTER5", "tester5@rhbgroup.com", department, "I", null, DateUtils.getDateFromString(diff89Days, TIME_FORMAT), 89, false, departmentId);
    	BatchUserMaintAutoAging expecteBatchUserMaintAutoAging3 = createBatchUserMaintAutoAging(
    		0L, 6, "TESTER6", "TESTER6", "tester6@rhbgroup.com", department, "I", null, DateUtils.getDateFromString(diff90Days, TIME_FORMAT), 90, false, departmentId);
    	BatchUserMaintAutoAging expecteBatchUserMaintAutoAging4 = createBatchUserMaintAutoAging(
    		0L, 7, "TESTER7", "TESTER7", "tester7@rhbgroup.com", department, "I", null, DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
    	BatchUserMaintAutoAging expecteBatchUserMaintAutoAging5 = createBatchUserMaintAutoAging(
    		0L, 8, "TESTER8", "TESTER8", "tester8@rhbgroup.com", department, "A", null, DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		
    	compareBatchUserMaintAutoAging(expecteBatchUserMaintAutoAging1, results.get(0));
    	compareBatchUserMaintAutoAging(expecteBatchUserMaintAutoAging2, results.get(1));
    	compareBatchUserMaintAutoAging(expecteBatchUserMaintAutoAging3, results.get(2));
    	compareBatchUserMaintAutoAging(expecteBatchUserMaintAutoAging4, results.get(3));
    	compareBatchUserMaintAutoAging(expecteBatchUserMaintAutoAging5, results.get(4));
    	
    	String afterSQL1 = beforeSQL1;
        String afterSQL2 = beforeSQL2;
        jdbcTemplate.batchUpdate(afterSQL1, afterSQL2);
	}
	
	/*
	 * Test to ensure that in case there is no matching record found by the reader, it shall not break the process
	 */
	@Test
	public void testPositiveReaderNoMatchRecord() throws Exception {
		String beforeSQL1 = "DELETE FROM TBL_BO_USER WHERE USERNAME LIKE 'TESTER%' AND USER_STATUS_ID in ('A','I')";
    	String beforeSQL2 = "DELETE FROM TBL_BO_CONFIG_GENERIC WHERE CONFIG_TYPE = 'user_department' and CONFIG_CODE like 'TESTER%'";
    	String beforeSQL3 = "INSERT INTO TBL_BO_CONFIG_GENERIC (CONFIG_TYPE, CONFIG_CODE, CONFIG_DESC, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY)VALUES('user_department', '9999', '9999', NOW(), 'TESTER', NOW(), 'TESTER')";
    	jdbcTemplate.batchUpdate(beforeSQL1, beforeSQL2, beforeSQL3);
    	
    	String selectSQL = "SELECT ID, CONFIG_CODE FROM TBL_BO_CONFIG_GENERIC WHERE CONFIG_TYPE = 'user_department' AND CONFIG_CODE = '9999' AND CONFIG_DESC = '9999'";
    	BoConfigGeneric boConfigGeneric = jdbcTemplate.query(selectSQL, new CustomBoConfigGenericRowMapper()).get(0);
    	
    	Date currentDate = new Date();
    	
    	String diff1Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -1), TIME_FORMAT);
    	String diff29Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -29), TIME_FORMAT);
    	String diff30Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -30), TIME_FORMAT);
    	int departmentId = boConfigGeneric.getId();
    	
    	String insertSQL1 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER1', 'tester1@rhbgroup.com', 'TESTER1', %d, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		departmentId, UserStatus.ACTIVE.getStatus(), diff1Days);
    	String insertSQL2 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER2', 'tester2@rhbgroup.com', 'TESTER2', %d, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		departmentId, UserStatus.ACTIVE.getStatus(), diff29Days);
    	String insertSQL3 = String.format("INSERT INTO TBL_BO_USER (USERNAME, EMAIL, NAME, USER_DEPARTMENT_ID, USER_STATUS_ID, LAST_LOGIN_TIME, FAILED_LOGIN_COUNT, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY) VALUES ('TESTER3', 'tester3@rhbgroup.com', 'TESTER3', %d, '%s', '%s', 0, NOW(), 'TESTER', NOW(), 'TESTER')", 
    		departmentId, UserStatus.ACTIVE.getStatus(), diff30Days);
    	jdbcTemplate.batchUpdate(insertSQL1, insertSQL2, insertSQL3);
    	
    	List<BatchUserMaintAutoAging> results = 
    		StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchUserMaintAutoAging>>() {
				public List<BatchUserMaintAutoAging> call() throws Exception {
					BatchUserMaintAutoAging batchUserMaintAutoAging;
					List<BatchUserMaintAutoAging> batchUserMaintAutoAgings = new ArrayList<>();
					while((batchUserMaintAutoAging = itemReader.read()) != null) {
						batchUserMaintAutoAgings.add(batchUserMaintAutoAging);
					}
					return batchUserMaintAutoAgings;
				}
		});
    	
    	assertEquals(0, results.size());
    	
    	String afterSQL1 = beforeSQL1;
        String afterSQL2 = beforeSQL2;
        jdbcTemplate.batchUpdate(afterSQL1, afterSQL2);
	}
	
	/*
	 * Test to ensure processor able to handle different type of object correctly
	 */
	@Test
	public void testPositiveProcessor() throws Exception {
		Date currentDate = new Date();
		String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), TIME_FORMAT);
		String diff91Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -91), TIME_FORMAT);
		String diff89Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -89), TIME_FORMAT);
		int departmentId = 9999;
		String department = "FINANCE";
		
		// Scenario: Active to Inactive
		BatchUserMaintAutoAging batchUserMaintAutoAging1 = createBatchUserMaintAutoAging(
	   			0L, 4, "TESTER4", "TESTER4", "tester4@rhbgroup.com", department, "A", null, DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
		BatchUserMaintAutoAging expecteBatchUserMaintAutoAging1 = createBatchUserMaintAutoAging(
	   			0L, 4, "TESTER4", "TESTER4", "tester4@rhbgroup.com", department, "A", "I", DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
		assertEquals(expecteBatchUserMaintAutoAging1, itemProcessor.process(batchUserMaintAutoAging1));
		
		// Scenario: Inactive to Delete
		BatchUserMaintAutoAging batchUserMaintAutoAging2 = createBatchUserMaintAutoAging(
	    	0L, 7, "TESTER7", "TESTER7", "tester7@rhbgroup.com", department, "I", null, DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		BatchUserMaintAutoAging expecteBatchUserMaintAutoAging2 = createBatchUserMaintAutoAging(
			0L, 7, "TESTER7", "TESTER7", "tester7@rhbgroup.com", department, "I", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		assertEquals(expecteBatchUserMaintAutoAging2, itemProcessor.process(batchUserMaintAutoAging2));
		
		// Scenario: Active to Delete, this happened in case the batch job not run for certain time
		BatchUserMaintAutoAging batchUserMaintAutoAging3 = createBatchUserMaintAutoAging(
    		0L, 8, "TESTER8", "TESTER8", "tester8@rhbgroup.com", department, "A", null, DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		BatchUserMaintAutoAging expecteBatchUserMaintAutoAging3 = createBatchUserMaintAutoAging(
	    	0L, 8, "TESTER8", "TESTER8", "tester8@rhbgroup.com", department, "A", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		assertEquals(expecteBatchUserMaintAutoAging3, itemProcessor.process(batchUserMaintAutoAging3));
		
		// Scenario: Inactive but dont need to be Delete
		BatchUserMaintAutoAging batchUserMaintAutoAging4 = createBatchUserMaintAutoAging(
	   		0L, 5, "TESTER5", "TESTER5", "tester5@rhbgroup.com", department, "I", null, DateUtils.getDateFromString(diff89Days, TIME_FORMAT), 89, false, departmentId);
		assertEquals(null, itemProcessor.process(batchUserMaintAutoAging4));
	}
	
	/*
	 * Test to ensure writer able to process everything like normal, such as insert DB records, update DB records, and send JMS
	 */
	@Test
	public void testPositiveWriter() throws Exception {
		Date currentDate = new Date();
		String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), TIME_FORMAT);
		String diff91Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -91), TIME_FORMAT);
		int departmentId = 9999;
		String department = "FINANCE";
		
		BatchUserMaintAutoAging batchUserMaintAutoAging1 = createBatchUserMaintAutoAging(
	   		1L, 4, "TESTER4", "TESTER4", "tester4@rhbgroup.com", department, "A", "I", DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
		BatchUserMaintAutoAging batchUserMaintAutoAging2 = createBatchUserMaintAutoAging(
		   	1L, 7, "TESTER7", "TESTER7", "tester7@rhbgroup.com", department, "I", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		BatchUserMaintAutoAging batchUserMaintAutoAging3 = createBatchUserMaintAutoAging(
	    	1L, 8, "TESTER8", "TESTER8", "tester8@rhbgroup.com", department, "A", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		List<BatchUserMaintAutoAging> batchUserMaintAutoAgings = new ArrayList<>();
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging1);
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging2);
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging3);
		
		PowerMockito.mockStatic(JMSUtils.class);
		
		when(boUserRepository.updateUserStatus(Mockito.<BatchUserMaintAutoAging>any(), Mockito.<String>any())).thenReturn(1);
		
		itemWriter.write(batchUserMaintAutoAgings);
		
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging1);
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging2);
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging3);
		verify(boUserRepository, times(1)).updateUserStatus(batchUserMaintAutoAging1, JOB_ID);
		verify(boUserRepository, times(1)).updateUserStatus(batchUserMaintAutoAging2, JOB_ID);
		verify(boUserRepository, times(1)).updateUserStatus(batchUserMaintAutoAging3, JOB_ID);
		verify(batchUserMaintAutoAgingRespository, times(1)).updateIsProcessed(batchUserMaintAutoAging1);
		verify(batchUserMaintAutoAgingRespository, times(1)).updateIsProcessed(batchUserMaintAutoAging2);
		verify(batchUserMaintAutoAgingRespository, times(1)).updateIsProcessed(batchUserMaintAutoAging3);
		
		String expectedJson1 = generateExpectedJSON(batchUserMaintAutoAging1, true, null);
		String expectedJson2 = generateExpectedJSON(batchUserMaintAutoAging2, true, null);
		String expectedJson3 = generateExpectedJSON(batchUserMaintAutoAging3, true, null);
		/*
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(expectedJson1),
    		Mockito.any());
        
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(expectedJson2),
    		Mockito.any());
		
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(expectedJson3),
    		Mockito.any());
		*/
		String expectedJobExecutionId = "1";
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID), expectedJobExecutionId);
	}
	
	/*
	 * Test to ensure if DB service is down during insert, we still send the JMS message but with failed status
	 */
	@Test
	public void testNegativeWriterDBServiceDownWhenInsertBatchUserMaintAutoAging() throws Exception {
		Date currentDate = new Date();
		String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), TIME_FORMAT);
		String diff91Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -91), TIME_FORMAT);
		int departmentId = 9999;
		String department = "FINANCE";
		
		BatchUserMaintAutoAging batchUserMaintAutoAging1 = createBatchUserMaintAutoAging(
	   		1L, 4, "TESTER4", "TESTER4", "tester4@rhbgroup.com", department, "A", "I", DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
		BatchUserMaintAutoAging batchUserMaintAutoAging2 = createBatchUserMaintAutoAging(
		   	1L, 7, "TESTER7", "TESTER7", "tester7@rhbgroup.com", department, "I", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		BatchUserMaintAutoAging batchUserMaintAutoAging3 = createBatchUserMaintAutoAging(
	    	1L, 8, "TESTER8", "TESTER8", "tester8@rhbgroup.com", department, "A", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		List<BatchUserMaintAutoAging> batchUserMaintAutoAgings = new ArrayList<>();
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging1);
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging2);
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging3);
		
		PowerMockito.mockStatic(JMSUtils.class);
		
		BatchException dbException = new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE);
		when(batchUserMaintAutoAgingRespository.addBatchUserMaintAutoAgingToDB(Mockito.<BatchUserMaintAutoAging>any())).thenThrow(dbException);
		
		itemWriter.write(batchUserMaintAutoAgings);
		verify(batchSuspenseRepository, times(6)).addBatchSuspenseToDB(Mockito.<BatchSuspense>any());
		
		String expectedJson1 = generateExpectedJSON(batchUserMaintAutoAging1, false, "80002:General DB Exception");
		String expectedJson2 = generateExpectedJSON(batchUserMaintAutoAging2, false, "80002:General DB Exception");
		String expectedJson3 = generateExpectedJSON(batchUserMaintAutoAging3, false, "80002:General DB Exception");
		/*
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(expectedJson1),
    		Mockito.any());
        
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(expectedJson2),
    		Mockito.any());
		
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(expectedJson3),
    		Mockito.any());
		*/
		String expectedJobExecutionId = "1";
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID), expectedJobExecutionId);
	}
	
	/*
	 * Test to ensure if DB service is down during update, we still send the JMS message but with failed status
	 */
	@Test
	public void testNegativeWriterDBServiceDownWhenUpdateBOUser() throws Exception {
		Date currentDate = new Date();
		String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), TIME_FORMAT);
		String diff91Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -91), TIME_FORMAT);
		int departmentId= 9998;
		String department = "FINANCE";
		
		BatchUserMaintAutoAging batchUserMaintAutoAging1 = createBatchUserMaintAutoAging(
	   		1L, 4, "TESTER4", "TESTER4", "tester4@rhbgroup.com", department, "A", "I", DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
		BatchUserMaintAutoAging batchUserMaintAutoAging2 = createBatchUserMaintAutoAging(
		   	1L, 7, "TESTER7", "TESTER7", "tester7@rhbgroup.com", department, "I", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		BatchUserMaintAutoAging batchUserMaintAutoAging3 = createBatchUserMaintAutoAging(
	    	1L, 8, "TESTER8", "TESTER8", "tester8@rhbgroup.com", department, "A", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		List<BatchUserMaintAutoAging> batchUserMaintAutoAgings = new ArrayList<>();
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging1);
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging2);
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging3);
		
		PowerMockito.mockStatic(JMSUtils.class);
		
		SQLException sqlException = new SQLException("Connection timeout");
		BatchException dbException = new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, sqlException);
		when(boUserRepository.updateUserStatus(Mockito.<BatchUserMaintAutoAging>any(), Mockito.any())).thenThrow(dbException);
		
		itemWriter.write(batchUserMaintAutoAgings);
		
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging1);
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging2);
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging3);
		verify(batchSuspenseRepository, times(6)).addBatchSuspenseToDB(Mockito.<BatchSuspense>any());
		
		String expectedJson1 = generateExpectedJSON(batchUserMaintAutoAging1, false, "Connection timeout");
		String expectedJson2 = generateExpectedJSON(batchUserMaintAutoAging2, false, "Connection timeout");
		String expectedJson3 = generateExpectedJSON(batchUserMaintAutoAging3, false, "Connection timeout");
		/*
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(expectedJson1),
    		Mockito.any());
        
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(expectedJson2),
    		Mockito.any());
		
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(expectedJson3),
    		Mockito.any());
		*/
		String expectedJobExecutionId = "1";
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID), expectedJobExecutionId);
	}
	
	/*
	 * Test to ensure if DB service is down during update the IsProcessed, we still send the JMS message but with successful status since we had updated the TBL_BO_USER without issue
	 */
	@Test
	public void testNegativeWriterDBServiceDownWhenUpdateBatchUserMaintAutoAging() throws Exception {
		Date currentDate = new Date();
		String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), TIME_FORMAT);
		String diff91Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -91), TIME_FORMAT);
		int departmentId= 9998;
		String department = "FINANCE";
		
		BatchUserMaintAutoAging batchUserMaintAutoAging1 = createBatchUserMaintAutoAging(
	   		1L, 4, "TESTER4", "TESTER4", "tester4@rhbgroup.com", department, "A", "I", DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
		BatchUserMaintAutoAging batchUserMaintAutoAging2 = createBatchUserMaintAutoAging(
		   	1L, 7, "TESTER7", "TESTER7", "tester7@rhbgroup.com", department, "I", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		BatchUserMaintAutoAging batchUserMaintAutoAging3 = createBatchUserMaintAutoAging(
	    	1L, 8, "TESTER8", "TESTER8", "tester8@rhbgroup.com", department, "A", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		List<BatchUserMaintAutoAging> batchUserMaintAutoAgings = new ArrayList<>();
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging1);
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging2);
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging3);
		
		PowerMockito.mockStatic(JMSUtils.class);
		
		when(boUserRepository.updateUserStatus(Mockito.<BatchUserMaintAutoAging>any(), Mockito.<String>any())).thenReturn(1);
		
		BatchException dbException = new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE);
		when(batchUserMaintAutoAgingRespository.updateIsProcessed(Mockito.<BatchUserMaintAutoAging>any())).thenThrow(dbException);
		
		itemWriter.write(batchUserMaintAutoAgings);
		
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging1);
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging2);
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging3);
		verify(boUserRepository, times(1)).updateUserStatus(batchUserMaintAutoAging1, JOB_ID);
		verify(boUserRepository, times(1)).updateUserStatus(batchUserMaintAutoAging2, JOB_ID);
		verify(boUserRepository, times(1)).updateUserStatus(batchUserMaintAutoAging3, JOB_ID);
		verify(batchSuspenseRepository, times(6)).addBatchSuspenseToDB(Mockito.<BatchSuspense>any());
		
		String expectedJson1 = generateExpectedJSON(batchUserMaintAutoAging1, true, null);
		String expectedJson2 = generateExpectedJSON(batchUserMaintAutoAging2, true, null);
		String expectedJson3 = generateExpectedJSON(batchUserMaintAutoAging3, true, null);
		/*
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(expectedJson1),
    		Mockito.any());
        
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(expectedJson2),
    		Mockito.any());
		
		PowerMockito.verifyStatic(JMSUtils.class, VerificationModeFactory.times(1));
		JMSUtils.sendMessageToJMS(
			Mockito.matches(expectedJson3),
    		Mockito.any());
		*/
		String expectedJobExecutionId = "1";
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID), expectedJobExecutionId);
	}
	
	/*
	 * Test to ensure when JMS service is down, we shall still update the TBL_BATCH_SUSPENSE table
	 */
	@Test
	public void testNegativeWriterJMSServiceDown() throws Exception {
		Date currentDate = new Date();
		String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), TIME_FORMAT);
		String diff91Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -91), TIME_FORMAT);
		int departmentId= 9998;
		String department = "FINANCE";
		
		BatchUserMaintAutoAging batchUserMaintAutoAging1 = createBatchUserMaintAutoAging(
	   		1L, 4, "TESTER4", "TESTER4", "tester4@rhbgroup.com", department, "A", "I", DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
		BatchUserMaintAutoAging batchUserMaintAutoAging2 = createBatchUserMaintAutoAging(
		   	1L, 7, "TESTER7", "TESTER7", "tester7@rhbgroup.com", department, "I", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		BatchUserMaintAutoAging batchUserMaintAutoAging3 = createBatchUserMaintAutoAging(
	    	1L, 8, "TESTER8", "TESTER8", "tester8@rhbgroup.com", department, "A", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		List<BatchUserMaintAutoAging> batchUserMaintAutoAgings = new ArrayList<>();
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging1);
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging2);
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging3);
		
		PowerMockito.mockStatic(JMSUtils.class);
		
		when(boUserRepository.updateUserStatus(Mockito.<BatchUserMaintAutoAging>any(), Mockito.<String>any())).thenReturn(1);
		
		PowerMockito.when(JMSUtils.class, "sendMessageToJMS", Mockito.anyString(), Mockito.<JMSConfig>any()).thenThrow(InvalidDestinationException.class);
		
		itemWriter.write(batchUserMaintAutoAgings);
		
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging1);
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging2);
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging3);
		verify(boUserRepository, times(1)).updateUserStatus(batchUserMaintAutoAging1, JOB_ID);
		verify(boUserRepository, times(1)).updateUserStatus(batchUserMaintAutoAging2, JOB_ID);
		verify(boUserRepository, times(1)).updateUserStatus(batchUserMaintAutoAging3, JOB_ID);
		verify(batchUserMaintAutoAgingRespository, times(1)).updateIsProcessed(batchUserMaintAutoAging1);
		verify(batchUserMaintAutoAgingRespository, times(1)).updateIsProcessed(batchUserMaintAutoAging2);
		verify(batchUserMaintAutoAgingRespository, times(1)).updateIsProcessed(batchUserMaintAutoAging3);
		verify(batchSuspenseRepository, times(3)).addBatchSuspenseToDB(Mockito.<BatchSuspense>any());
		
		verify(mockAppender, times(5)).doAppend((LoggingEvent)captorLoggingEvent.capture());
    	LoggingEvent loggingEvent1 = (LoggingEvent)captorLoggingEvent.getAllValues().get(2);
    	LoggingEvent loggingEvent2 = (LoggingEvent)captorLoggingEvent.getAllValues().get(3);
    	LoggingEvent loggingEvent3 = (LoggingEvent)captorLoggingEvent.getAllValues().get(4);
        //Check log level
        assertEquals(Level.ERROR, loggingEvent1.getLevel());
        assertEquals(Level.ERROR, loggingEvent2.getLevel());
        assertEquals(Level.ERROR, loggingEvent3.getLevel());
        
        String lastLoginTime31DaysBefore = DateUtils.getDateFromString(diff31Days, TIME_FORMAT).toString();
        String lastLoginTime91DaysBefore = DateUtils.getDateFromString(diff91Days, TIME_FORMAT).toString();
        
        //Check the message being logged
        assertTrue(loggingEvent1.getRenderedMessage().contains(String.format("Error happened while pushing JSON notification to JMS queue for BatchUserMaintAutoAging [BatchUserMaintAutoAging(jobExecutionId=1, userId=4, userName=TESTER4, name=TESTER4, email=tester4@rhbgroup.com, userDepartmentId=9998, department=FINANCE, currentUserStatus=A, newUserStatus=I, lastLoginTime=%s, lastLoginTimeDayDiff=31, isProcessed=false, createdTime=null, updatedTime=null)]", lastLoginTime31DaysBefore)));
        assertTrue(loggingEvent2.getRenderedMessage().contains(String.format("Error happened while pushing JSON notification to JMS queue for BatchUserMaintAutoAging [BatchUserMaintAutoAging(jobExecutionId=1, userId=7, userName=TESTER7, name=TESTER7, email=tester7@rhbgroup.com, userDepartmentId=9998, department=FINANCE, currentUserStatus=I, newUserStatus=D, lastLoginTime=%s, lastLoginTimeDayDiff=91, isProcessed=false, createdTime=null, updatedTime=null)]", lastLoginTime91DaysBefore)));
        assertTrue(loggingEvent3.getRenderedMessage().contains(String.format("Error happened while pushing JSON notification to JMS queue for BatchUserMaintAutoAging [BatchUserMaintAutoAging(jobExecutionId=1, userId=8, userName=TESTER8, name=TESTER8, email=tester8@rhbgroup.com, userDepartmentId=9998, department=FINANCE, currentUserStatus=A, newUserStatus=D, lastLoginTime=%s, lastLoginTimeDayDiff=91, isProcessed=false, createdTime=null, updatedTime=null)]", lastLoginTime91DaysBefore)));
		
		String expectedJobExecutionId = "1";
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID), expectedJobExecutionId);
	}
	
	/*
	 * Test to ensure when JMS service is down, we shall still update the TBL_BATCH_SUSPENSE table
	 * NOTES: No longer valid since we let Spring to help close the resource after job end
	 */
//	@Test
	public void testNegativeWriterFailedStopJMSService() throws Exception {
		Date currentDate = new Date();
		String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), TIME_FORMAT);
		String diff91Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -91), TIME_FORMAT);
		int departmentId= 9998;
		String department = "FINANCE";
		
		BatchUserMaintAutoAging batchUserMaintAutoAging1 = createBatchUserMaintAutoAging(
	   		1L, 4, "TESTER4", "TESTER4", "tester4@rhbgroup.com", department, "A", "I", DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
		BatchUserMaintAutoAging batchUserMaintAutoAging2 = createBatchUserMaintAutoAging(
		   	1L, 7, "TESTER7", "TESTER7", "tester7@rhbgroup.com", department, "I", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		BatchUserMaintAutoAging batchUserMaintAutoAging3 = createBatchUserMaintAutoAging(
	    	1L, 8, "TESTER8", "TESTER8", "tester8@rhbgroup.com", department, "A", "D", DateUtils.getDateFromString(diff91Days, TIME_FORMAT), 91, false, departmentId);
		List<BatchUserMaintAutoAging> batchUserMaintAutoAgings = new ArrayList<>();
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging1);
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging2);
		batchUserMaintAutoAgings.add(batchUserMaintAutoAging3);
		
		PowerMockito.mockStatic(JMSUtils.class);
		
		when(boUserRepository.updateUserStatus(Mockito.<BatchUserMaintAutoAging>any(), Mockito.<String>any())).thenReturn(1);
		
//		PowerMockito.when(JMSUtils.class, "shutdownJMS", Mockito.<JMSConfig>any()).thenThrow(NamingException.class);
		
		itemWriter.write(batchUserMaintAutoAgings);
		
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging1);
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging2);
		verify(batchUserMaintAutoAgingRespository, times(1)).addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging3);
		verify(boUserRepository, times(1)).updateUserStatus(batchUserMaintAutoAging1, JOB_ID);
		verify(boUserRepository, times(1)).updateUserStatus(batchUserMaintAutoAging2, JOB_ID);
		verify(boUserRepository, times(1)).updateUserStatus(batchUserMaintAutoAging3, JOB_ID);
		verify(batchUserMaintAutoAgingRespository, times(1)).updateIsProcessed(batchUserMaintAutoAging1);
		verify(batchUserMaintAutoAgingRespository, times(1)).updateIsProcessed(batchUserMaintAutoAging2);
		verify(batchUserMaintAutoAgingRespository, times(1)).updateIsProcessed(batchUserMaintAutoAging3);
		
		verify(mockAppender, times(2)).doAppend((LoggingEvent)captorLoggingEvent.capture());
    	LoggingEvent loggingEvent = (LoggingEvent)captorLoggingEvent.getAllValues().get(1);
        //Check log level
        assertEquals(Level.ERROR, loggingEvent.getLevel());
        
        //Check the message being logged
        assertTrue(loggingEvent.getRenderedMessage().contains(String.format("Failed to close the JMSContext / NamingContext in [%s:%s]", auditJMSConfigProperties.getHost(), auditJMSConfigProperties.getPort())));
        
		String expectedJobExecutionId = "1";
		assertEquals(stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_LAST_PROCESSED_JOB_EXECUTION_ID), expectedJobExecutionId);
	}
	
	private BatchUserMaintAutoAging createBatchUserMaintAutoAging(long jobExecutionId, int userId, String userName, String name,
		String email, String department, String currentUserStatus, String newUserStatus, Date lastLoginTime, int lastLoginTimeDayDiff, 
		boolean isProcessed, int userDepartmentId) {
		
		BatchUserMaintAutoAging batchUserMaintAutoAging = new BatchUserMaintAutoAging();
		batchUserMaintAutoAging.setJobExecutionId(jobExecutionId);
		batchUserMaintAutoAging.setUserId(userId);
		batchUserMaintAutoAging.setUserName(userName);
		batchUserMaintAutoAging.setName(name);
		batchUserMaintAutoAging.setEmail(email);
		batchUserMaintAutoAging.setUserDepartmentId(userDepartmentId);
		batchUserMaintAutoAging.setDepartment(department);
		batchUserMaintAutoAging.setCurrentUserStatus(currentUserStatus);
		batchUserMaintAutoAging.setNewUserStatus(newUserStatus);
		batchUserMaintAutoAging.setLastLoginTime(lastLoginTime);
		batchUserMaintAutoAging.setLastLoginTimeDayDiff(lastLoginTimeDayDiff);
		batchUserMaintAutoAging.setProcessed(isProcessed);
		
		return batchUserMaintAutoAging;
	}
	
	private String generateExpectedJSON(BatchUserMaintAutoAging batchUserMaintAutoAging, boolean isSuccess, String suspenseMessage) throws UnknownHostException {
		return String.format(EXPECTED_NOTIFICATION_JSON_TEMPLATE,
			jobConfigProperties.getEventCode(),
			Integer.parseInt("1"),
			jobConfigProperties.getAuditMessageUsername(),
			" ",
			(isSuccess) ? AUDIT_MESSAGE_SUCCESS_STATUS_CODE : AUDIT_MESSAGE_UNSUCCESS_STATUS_CODE,
			(isSuccess) ? AUDIT_MESSAGE_SUCCESS_DESCRIPTION : AUDIT_MESSAGE_UNSUCCESS_DESCRIPTION,
			InetAddress.getLocalHost().getHostAddress(),
			batchUserMaintAutoAging.getUserId(),
			batchUserMaintAutoAging.getUserName(),
			batchUserMaintAutoAging.getEmail(),
			batchUserMaintAutoAging.getUserDepartmentId(),
			batchUserMaintAutoAging.getCurrentUserStatus(),
			batchUserMaintAutoAging.getNewUserStatus(),
			DateUtils.formatDateString(batchUserMaintAutoAging.getLastLoginTime(), JSON_DATE_FORMAT),
			(isSuccess) ? JSON_SUCCESS_RESPONSE : String.format(JSON_ERROR_RESPONSE, suspenseMessage),
			batchUserMaintAutoAging.getUserId()
		)
		.replace("{", "\\{")
		.replace("}", "\\}")
		.replace("(", "\\(")
		.replace(")", "\\)")
		.replace("+", "\\+");
	}
	
	private void compareBatchUserMaintAutoAging(BatchUserMaintAutoAging expected, BatchUserMaintAutoAging actual) {
		assertEquals(expected.getJobExecutionId(), actual.getJobExecutionId());
		// Skip the identity randomly generate by the DB
//		assertEquals(expected.getUserId(), actual.getUserId());
		assertEquals(expected.getUserName(), actual.getUserName());
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getEmail(), actual.getEmail());
		assertEquals(expected.getDepartment(), actual.getDepartment());
		assertEquals(expected.getCurrentUserStatus(), actual.getCurrentUserStatus());
		assertEquals(expected.getNewUserStatus(), actual.getNewUserStatus());
		assertEquals(expected.getLastLoginTime(), actual.getLastLoginTime());
		assertEquals(expected.getLastLoginTimeDayDiff(), actual.getLastLoginTimeDayDiff());
		assertEquals(expected.isProcessed(), actual.isProcessed());
		assertEquals(expected.getUserDepartmentId(), actual.getUserDepartmentId());
	}
	
//	private BatchSuspense createBatchSuspense(BatchUserMaintAutoAging batchUserMaintAutoAging, Exception e) {
//		BatchSuspense batchSuspense = new BatchSuspense();
//		batchSuspense.setBatchJobName(TEST_JOB);
//		batchSuspense.setJobExecutionId(TEST_JOB_EXECUTION_ID);
//		batchSuspense.setSuspenseColumn("N/A");
//		batchSuspense.setSuspenseType(SuspenseType.EXCEPTION.toString());
//		batchSuspense.setSuspenseMessage(e.getMessage());
//		batchSuspense.setCreatedTime(new Date());
//		
//		String suspenseRecord = String.format("%s|%s", batchUserMaintAutoAging.getUserId(), batchUserMaintAutoAging.getUserName());
//		batchSuspense.setSuspenseRecord(suspenseRecord);
//		
//		return batchSuspense;
//	}
}
