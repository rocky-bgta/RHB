package com.rhbgroup.dcp.bo.batch.test.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBGRejectTxn;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBGRejectTxnRespositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class BatchStagedIBGRejectTxnRespositoryImplTest extends BaseJobTest {

	@Autowired
	private BatchStagedIBGRejectTxnRespositoryImpl repository;
	
	@MockBean
	JdbcTemplate mockJdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testPositiveUpdateIsNotificationSent() throws BatchException {
		BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = createBatchStagedIBGRejectTxn(
	    	111111, "100.00", "TESTER1_BENE", "20180831", "A/C CLOSED");
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.anyLong()))
		.thenReturn(1);
		
		assertEquals(1, repository.updateIsNotificationSent(batchStagedIBGRejectTxn));
	}
	
	@Test
	public void testNegativeUpdateIsNotificationSent() throws BatchException {
		BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = createBatchStagedIBGRejectTxn(
	    	111111, "100.00", "TESTER1_BENE", "20180831", "A/C CLOSED");
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.anyLong()))
		.thenThrow(UncategorizedSQLException.class);
		
		expectedEx.expect(BatchException.class);
		expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.DB_SYSTEM_ERROR + ":" + BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
		repository.updateIsNotificationSent(batchStagedIBGRejectTxn);
	}
	
	private BatchStagedIBGRejectTxn createBatchStagedIBGRejectTxn(
		int userId, String amount, String beneName, String date, String rejectDescription) {
		
		BatchStagedIBGRejectTxn batchStagedIBGRejectTxn = new BatchStagedIBGRejectTxn();
		// Id might change due to sequence or identity, we cant compare it
//			batchStagedIBGRejectTxn.setId(id);
		batchStagedIBGRejectTxn.setUserId(userId);
		batchStagedIBGRejectTxn.setAmount(amount);
		batchStagedIBGRejectTxn.setBeneName(beneName);
		batchStagedIBGRejectTxn.setDate(date);
		batchStagedIBGRejectTxn.setRejectDescription(rejectDescription);
		
		return batchStagedIBGRejectTxn;
	}
}
