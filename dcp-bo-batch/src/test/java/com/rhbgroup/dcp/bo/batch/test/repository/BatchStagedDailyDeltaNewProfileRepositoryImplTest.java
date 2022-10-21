package com.rhbgroup.dcp.bo.batch.test.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
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
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedDailyDeltaNewProfile;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedDailyDeltaNewProfileRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class BatchStagedDailyDeltaNewProfileRepositoryImplTest extends BaseJobTest {

	@Autowired
	private BatchStagedDailyDeltaNewProfileRepositoryImpl repository;
	
	@MockBean
	JdbcTemplate mockJdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testPositiveUpdateIsProcessed() throws BatchException {
		Date date = Date.from(LocalDate.of(2018, 8, 6).atStartOfDay(ZoneId.systemDefault()).toInstant());
		BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile = createBatchStagedDailyDeltaNewProfile(9991, date, false);
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any()))
		.thenReturn(1);
		
		assertEquals(1, repository.updateIsProcessed(batchStagedDailyDeltaNewProfile));
	}
	
	@Test
	public void testNegativeUpdateIsProcessed() throws BatchException {
		Date date = Date.from(LocalDate.of(2018, 8, 6).atStartOfDay(ZoneId.systemDefault()).toInstant());
		BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile = createBatchStagedDailyDeltaNewProfile(9991, date, false);
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any()))
		.thenThrow(UncategorizedSQLException.class);
		
		expectedEx.expect(BatchException.class);
		expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.DB_SYSTEM_ERROR + ":" + BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
		repository.updateIsProcessed(batchStagedDailyDeltaNewProfile);
	}
	
	private BatchStagedDailyDeltaNewProfile createBatchStagedDailyDeltaNewProfile(int userId, Date processingDate, boolean isProcessed) {
		BatchStagedDailyDeltaNewProfile batchStagedDailyDeltaNewProfile = new BatchStagedDailyDeltaNewProfile();
		batchStagedDailyDeltaNewProfile.setUserId(userId);
		batchStagedDailyDeltaNewProfile.setProcessingDate(processingDate);
		batchStagedDailyDeltaNewProfile.setProcessed(isProcessed);
		return batchStagedDailyDeltaNewProfile;
	}
}
