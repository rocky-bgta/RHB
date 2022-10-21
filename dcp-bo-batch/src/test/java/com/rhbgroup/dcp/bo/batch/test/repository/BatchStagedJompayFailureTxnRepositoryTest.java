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
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedJompayFailureTxn;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedJompayFailureTxnRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class BatchStagedJompayFailureTxnRepositoryTest extends BaseJobTest {

	@Autowired
	private BatchStagedJompayFailureTxnRepositoryImpl repository;
	
	@MockBean
	private JdbcTemplate mockJdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testPositiveAddBatchStagedIBKPaymentTxnToStaging() throws BatchException {
		BatchStagedJompayFailureTxn batchStagedJompayFailureTxn = createBatchStagedJompayFailureTxn("4600", "3", "180806111110", "Timeout", null);
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any()))
		.thenReturn(1);
		
		assertEquals(1, repository.addBatchStagedIBKPaymentTxnToStaging(batchStagedJompayFailureTxn));
	}
	
	@Test
	public void testNegativeAddBatchStagedIBKPaymentTxnToStaging() throws BatchException {
		BatchStagedJompayFailureTxn batchStagedJompayFailureTxn = createBatchStagedJompayFailureTxn("4600", "3", "180806111110", "Timeout", null);
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any()))
		.thenThrow(UncategorizedSQLException.class);
		
		expectedEx.expect(BatchException.class);
		expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.DB_SYSTEM_ERROR + ":" + BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
		repository.addBatchStagedIBKPaymentTxnToStaging(batchStagedJompayFailureTxn);
	}
	
	@Test
	public void testPositiveDeleteExistingBatchStagedJompayFailureTxns() throws BatchException {
		String fileName = "WIBKD8919B_180611.txt";
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.anyString()))
		.thenReturn(100);
		
		assertEquals(100, repository.deleteExistingBatchStagedJompayFailureTxns(fileName));
	}
	
	@Test
	public void testNegativeDeleteExistingBatchStagedJompayFailureTxns() throws BatchException {
		String fileName = "WIBKD8919B_180611.txt";
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.anyString()))
		.thenThrow(UncategorizedSQLException.class);
		
		expectedEx.expect(BatchException.class);
		expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.DB_SYSTEM_ERROR + ":" + BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
		repository.deleteExistingBatchStagedJompayFailureTxns(fileName);
	}
	
	private BatchStagedJompayFailureTxn createBatchStagedJompayFailureTxn(String billerCode, String paymentChannel, String requestTime, String reasonForFailure, String filename) {
		BatchStagedJompayFailureTxn batchStagedJompayFailureTxn = new BatchStagedJompayFailureTxn();
		batchStagedJompayFailureTxn.setBillerCode(billerCode);
		batchStagedJompayFailureTxn.setPaymentChannel(paymentChannel);
		batchStagedJompayFailureTxn.setRequestTimeStr(requestTime);
		batchStagedJompayFailureTxn.setReasonForFailure(reasonForFailure);
		batchStagedJompayFailureTxn.setFileName(filename);
		
		return batchStagedJompayFailureTxn;
	}
}
