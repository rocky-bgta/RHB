package com.rhbgroup.dcp.bo.batch.test.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.beanutils.PropertyUtils;
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
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnDetail;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBKPaymentTxnRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class BatchStagedIBKPaymentTxnRepositoryImplTest extends BaseJobTest {

	@Autowired
	private BatchStagedIBKPaymentTxnRepositoryImpl repository;
	
	@MockBean
	JdbcTemplate mockJdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	@Test
	public void testPositiveAddBatchStagedIBKPaymentTxnToStaging() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, BatchException {
		BatchStagedIBKPaymentTxnDetail batchStagedIBKPaymentTxnDetail = createBatchStagedIBKPaymentTxnDetail("processDate=null, billerAccountNo=null, billerAccountName=null, txnId=034554, txnDate=20180516, txnAmount=000000000200.00, txnType=CR, txnDescription=, billerRefNo1=834743882, billerRefNo2=0125289475, billerRefNo3=MOHD ALIF BIN ABDUL WAHAB, txnTime=005813");
		
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
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
			Mockito.any(),
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
		
		System.out.println(batchStagedIBKPaymentTxnDetail);
		assertEquals(1, repository.addBatchStagedIBKPaymentTxnToStaging(batchStagedIBKPaymentTxnDetail));
		assertEquals(0, repository.addBatchStagedIBKPaymentTxnToStaging(batchStagedIBKPaymentTxnDetail));
	}
	
	@Test
	public void testNegativeAddBatchStagedIBKPaymentTxnToStaging() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, BatchException {
		BatchStagedIBKPaymentTxnDetail batchStagedIBKPaymentTxnDetail = createBatchStagedIBKPaymentTxnDetail("processDate=null, billerAccountNo=null, billerAccountName=null, txnId=034554, txnDate=20180516, txnAmount=000000000200.00, txnType=CR, txnDescription=, billerRefNo1=834743882, billerRefNo2=0125289475, billerRefNo3=MOHD ALIF BIN ABDUL WAHAB, txnTime=005813");
		
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
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
				Mockito.any(),
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

		repository.addBatchStagedIBKPaymentTxnToStaging(batchStagedIBKPaymentTxnDetail);
	}
	
	@Test
	public void testPositiveDeleteExistingBatchStagedIBKPaymentTxns() throws BatchException {
		String filename = "TEST_FILENAME";
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.anyString()))
		.thenReturn(1);
		
		assertEquals(1, repository.deleteExistingBatchStagedIBKPaymentTxns(filename));
	}
	
	@Test
	public void testNegativeDeleteExistingBatchStagedIBKPaymentTxns() throws BatchException {
		String filename = "TEST_FILENAME";
		
		when(mockJdbcTemplate.update(
			Mockito.anyString(),
			Mockito.anyString()))
		.thenThrow(UncategorizedSQLException.class);
		
		expectedEx.expect(BatchException.class);
		expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.DB_SYSTEM_ERROR + ":" + BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
		repository.deleteExistingBatchStagedIBKPaymentTxns(filename);
	}
	
	private BatchStagedIBKPaymentTxnDetail createBatchStagedIBKPaymentTxnDetail(String line) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Map<String, String> keyValueMap = getKeyValueMap(line);
		BatchStagedIBKPaymentTxnDetail batchStagedIBKPaymentTxn = new BatchStagedIBKPaymentTxnDetail();
		for(Entry<String, String> entry : keyValueMap.entrySet()) {
			if(!entry.getValue().equals("null")) {
				PropertyUtils.setProperty(batchStagedIBKPaymentTxn, entry.getKey(), entry.getValue());
			}
		}
		return batchStagedIBKPaymentTxn;
	}
	
	private Map<String, String> getKeyValueMap(String line) {
		Map<String, String> keyValueMap = new HashMap<>();
		String[] keyValuePairs = line.split(",");
		for(String keyValuePair : keyValuePairs) {
			String[] values = keyValuePair.split("=");
			if(values.length == 1) {
				keyValueMap.put(values[0].trim(), "");
			} else {
				keyValueMap.put(values[0].trim(), values[1].trim());
			}
			
		}
		return keyValueMap;
	}
	
}
