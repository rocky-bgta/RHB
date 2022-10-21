package com.rhbgroup.dcp.bo.batch.test.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.rhbgroup.dcp.bo.batch.job.enums.SuspenseType;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchSuspenseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.rowmapper.BatchSuspenseRowMapper;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class BatchSuspenseRepositoryImplTest extends BaseJobTest {

	@Autowired
	private BatchSuspenseRepositoryImpl repository;
	
	@MockBean
	JdbcTemplate mockJdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testPositiveAddBatchSuspenseToDB() throws BatchException {
		String suspenseMessage = "TEST_SUSPENSE_MESSAGE";
		String suspenseRecord = "TEST_SUSPENSE_RECORD";
		BatchSuspense batchSuspense = createBatchSuspense(suspenseMessage, suspenseRecord);
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any()))
		.thenReturn(1);
		
		assertEquals(1, repository.addBatchSuspenseToDB(batchSuspense));
	}
	
	@Test
	public void testNegativeAddBatchSuspenseToDB() throws BatchException {
		String suspenseMessage = "TEST_SUSPENSE_MESSAGE";
		String suspenseRecord = "TEST_SUSPENSE_RECORD";
		BatchSuspense batchSuspense = createBatchSuspense(suspenseMessage, suspenseRecord);
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
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
		
		repository.addBatchSuspenseToDB(batchSuspense);
	}
	
	@Test
	public void testPositiveGetByJobNameAndJobExecutionId() throws BatchException {
		String jobName = TEST_JOB;
		String jobExecutionId = Long.toString(TEST_JOB_EXECUTION_ID);
		int maxLimit = 10;
		String suspenseMessage = "TEST_SUSPENSE_MESSAGE";
		String suspenseRecord = "TEST_SUSPENSE_RECORD";
		
		List<BatchSuspense> batchSuspenses = new ArrayList<>();
		for(int i=0; i<maxLimit; i++) {
			BatchSuspense batchSuspense = createBatchSuspense(suspenseMessage, suspenseRecord);
			batchSuspenses.add(batchSuspense);
		}
		
		when(mockJdbcTemplate.query(
			Mockito.anyString(),
			Mockito.any(BatchSuspenseRowMapper.class),
			Mockito.any(),
			Mockito.any()))
		.thenReturn(batchSuspenses);
		
		List<BatchSuspense> result = repository.getByJobNameAndJobExecutionId(jobName, jobExecutionId, maxLimit);
		assertEquals(maxLimit, result.size());
	}
	
	@Test
	public void testNegativeGetByJobNameAndJobExecutionId() throws BatchException {
		String jobName = TEST_JOB;
		String jobExecutionId = Long.toString(TEST_JOB_EXECUTION_ID);
		int maxLimit = 1;
		
		when(mockJdbcTemplate.query(
			Mockito.anyString(),
			Mockito.any(BatchSuspenseRowMapper.class),
			Mockito.any(),
			Mockito.any()))
		.thenThrow(UncategorizedSQLException.class);
		
		expectedEx.expect(BatchException.class);
		expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.DB_SYSTEM_ERROR + ":" + BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
		repository.getByJobNameAndJobExecutionId(jobName, jobExecutionId, maxLimit);
	}
	
	private BatchSuspense createBatchSuspense(String suspenseMessage, String suspenseRecord) {
		BatchSuspense batchSuspense = new BatchSuspense();
		batchSuspense.setBatchJobName(TEST_JOB);
		batchSuspense.setJobExecutionId(TEST_JOB_EXECUTION_ID);
		batchSuspense.setSuspenseColumn("N/A");
		batchSuspense.setSuspenseType(SuspenseType.EXCEPTION.toString());
		batchSuspense.setSuspenseMessage(suspenseMessage);
		batchSuspense.setCreatedTime(new Date());
		batchSuspense.setSuspenseRecord(suspenseRecord);
		
		return batchSuspense;
	}
}
