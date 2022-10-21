package com.rhbgroup.dcp.bo.batch.test.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Date;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUserMaintAutoAging;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUserMaintAutoAgingRespositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class BatchUserMaintAutoAgingRespositoryImplTest extends BaseJobTest {

	private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	@Autowired
	private BatchUserMaintAutoAgingRespositoryImpl repository;
	
	@MockBean
	JdbcTemplate mockJdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testPositveAddBatchUserMaintAutoAgingToDB() throws ParseException, BatchException {
		long jobExecutionId = 0L;
		int userId = 4;
		
		Date currentDate = new Date();
		String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), TIME_FORMAT);
		int departmentId = 9999;
		String department = "FINANCE";
		
		// Mandatory fields are NULL
		BatchUserMaintAutoAging batchUserMaintAutoAging = createBatchUserMaintAutoAging(
			jobExecutionId, userId, "TESTER4", "TESTER4", "tester4@rhbgroup.com", department, "A", null, DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any()))
		.thenReturn(1);
		
		assertEquals(1, repository.addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging));
	}
	
	@Test
	public void testNegativeddBatchUserMaintAutoAgingToDB() throws ParseException, BatchException {
		long jobExecutionId = 0L;
		int userId = 4;
		
		Date currentDate = new Date();
		String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), TIME_FORMAT);
		int departmentId = 9999;
		String department = "FINANCE";
		
		// Mandatory fields are NULL
		BatchUserMaintAutoAging batchUserMaintAutoAging = createBatchUserMaintAutoAging(
			jobExecutionId, userId, "TESTER4", "TESTER4", "tester4@rhbgroup.com", department, "A", null, DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any()))
		.thenThrow(UncategorizedSQLException.class);
		
		expectedEx.expect(BatchException.class);
		expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.DB_SYSTEM_ERROR + ":" + BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
		repository.addBatchUserMaintAutoAgingToDB(batchUserMaintAutoAging);
	}
	
	@Test
	public void testPositveUpdateIsProcessed() throws ParseException, BatchException {
		long jobExecutionId = 0L;
		int userId = 4;
		
		Date currentDate = new Date();
		String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), TIME_FORMAT);
		int departmentId = 9999;
		String department = "FINANCE";
		
		BatchUserMaintAutoAging batchUserMaintAutoAging = createBatchUserMaintAutoAging(
			jobExecutionId, userId, null, null, null, department, "A", null, DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.anyInt()))
		.thenReturn(1);
		
		assertEquals(1, repository.updateIsProcessed(batchUserMaintAutoAging));
	}
	
	@Test
	public void testNegativeUpdateIsProcessed() throws ParseException, BatchException {
		long jobExecutionId = 0L;
		int userId = 4;
		
		Date currentDate = new Date();
		String diff31Days = DateUtils.formatDateString(DateUtils.addDays(currentDate, -31), TIME_FORMAT);
		int departmentId = 9999;
		String department = "FINANCE";
		
		BatchUserMaintAutoAging batchUserMaintAutoAging = createBatchUserMaintAutoAging(
			jobExecutionId, userId, null, null, null, department, "A", null, DateUtils.getDateFromString(diff31Days, TIME_FORMAT), 31, false, departmentId);
		
		when(mockJdbcTemplate.update(
				Mockito.anyString(),
				Mockito.anyInt()))
			.thenThrow(UncategorizedSQLException.class);
		
		expectedEx.expect(BatchException.class);
		expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.DB_SYSTEM_ERROR + ":" + BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
		repository.updateIsProcessed(batchUserMaintAutoAging);
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
}
