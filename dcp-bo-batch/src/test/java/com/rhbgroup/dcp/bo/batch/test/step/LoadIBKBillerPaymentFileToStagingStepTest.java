package com.rhbgroup.dcp.bo.batch.test.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.beanutils.PropertyUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxn;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnDetail;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnHeader;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnTrailer;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBKPaymentTxnRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class LoadIBKBillerPaymentFileToStagingStepTest extends BaseFTPJobTest {

	private static final String STEP_NAME = "LoadIBKBillerPaymentFileToStagingStep";

	private static final String HEADER_LIST = "HeaderList";
	private static final String DETAIL_LIST = "DetailList";
	private static final String TRAILER_LIST = "TrailerList";
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private FlatFileItemReader<BatchStagedIBKPaymentTxn> itemReader;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchStagedIBKPaymentTxn, BatchStagedIBKPaymentTxn> itemProcessor;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchStagedIBKPaymentTxn> itemWriter;

	@Autowired
	private FTPIBKConfigProperties ftpConfigProperties;
	
	@MockBean
	private BatchStagedIBKPaymentTxnRepositoryImpl batchStagedIBKPaymentTxnRepository;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	private StepExecution stepExecution;

	public StepExecution getStepExection() throws FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623.txt", billerCode, billerCode));
		stepExecution = MetaDataInstanceFactory.createStepExecution();
		stepExecution.getJobExecution().getExecutionContext().putString(
				BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY,
				file.getAbsolutePath());
		return stepExecution;
	}

	@Before
	public void beforeLocalTest() throws IOException {
		setCustomFTP(ftpConfigProperties);
		super.beforeFTPTest();
		Mockito.reset(batchStagedIBKPaymentTxnRepository);
	}
	
	@After
	public void afterLocalTest() throws Exception {
		setCustomFTP(null);
		super.afterFTPTest();
	}
	
	/*
	 * Test to check whether reader able to read the input file content correctly, e.g. header, details and trailer
	 */
	@Test
	public void testPositiveReader() throws UnexpectedInputException, ParseException, Exception {
		itemReader.open(new ExecutionContext());
		Map<String, List<BatchStagedIBKPaymentTxn>> resultMap = StepScopeTestUtils.doInStepScope(stepExecution, new Callable<Map<String, List<BatchStagedIBKPaymentTxn>>>() {
			public Map<String, List<BatchStagedIBKPaymentTxn>> call() throws Exception {
				Map<String, List<BatchStagedIBKPaymentTxn>> resultMap = new HashMap<>();
				List<BatchStagedIBKPaymentTxn> headers = new ArrayList<>();
				List<BatchStagedIBKPaymentTxn> details = new ArrayList<>();
				List<BatchStagedIBKPaymentTxn> trailers = new ArrayList<>();
				BatchStagedIBKPaymentTxn batchStagedIBKPaymentTxn = null;
				
				while((batchStagedIBKPaymentTxn = itemReader.read()) != null) {
					if (batchStagedIBKPaymentTxn instanceof BatchStagedIBKPaymentTxnHeader) {
						headers.add(batchStagedIBKPaymentTxn);
					} else if (batchStagedIBKPaymentTxn instanceof BatchStagedIBKPaymentTxnDetail) {
						details.add(batchStagedIBKPaymentTxn);
					} else if (batchStagedIBKPaymentTxn instanceof BatchStagedIBKPaymentTxnTrailer) {
						trailers.add(batchStagedIBKPaymentTxn);
					}
				}
				
				resultMap.put(HEADER_LIST, headers);
				resultMap.put(DETAIL_LIST, details);
				resultMap.put(TRAILER_LIST, trailers);
				
				return resultMap;
			}
		});
		
		assertTrue(1 == resultMap.get(HEADER_LIST).size());
		assertTrue(30000 == resultMap.get(DETAIL_LIST).size());
		assertTrue(1 == resultMap.get(TRAILER_LIST).size());
		
		BatchStagedIBKPaymentTxnHeader expectedHeader = createBatchStagedIBKPaymentTxnHeader("batchNumber=0001, processDate=20180516, billerAccountNo=21412900159831, billerAccountName=UTest");
		BatchStagedIBKPaymentTxnDetail expectedDetail1 = createBatchStagedIBKPaymentTxnDetail("processDate=null, billerAccountNo=null, billerAccountName=null, txnId=034554, txnDate=20180516, txnAmount=000000000200.00, txnType=CR, txnDescription=, billerRefNo1=834743882, billerRefNo2=0125289475, billerRefNo3=MOHD ALIF BIN ABDUL WAHAB, txnTime=005813");
		BatchStagedIBKPaymentTxnDetail expectedDetail2 = createBatchStagedIBKPaymentTxnDetail("processDate=null, billerAccountNo=null, billerAccountName=null, txnId=102646, txnDate=20180516, txnAmount=000000000300.00, txnType=CR, txnDescription=, billerRefNo1=1440731238, billerRefNo2=0172365379, billerRefNo3=FOO HUI PING, txnTime=025219");
		BatchStagedIBKPaymentTxnDetail expectedDetail3 = createBatchStagedIBKPaymentTxnDetail("processDate=null, billerAccountNo=null, billerAccountName=null, txnId=159783, txnDate=20180516, txnAmount=000000000200.00, txnType=CR, txnDescription=, billerRefNo1=1141511418, billerRefNo2=0124712371, billerRefNo3=YEAP TAN TIAN, txnTime=044405");
		BatchStagedIBKPaymentTxnTrailer expectedTrailer = createBatchStagedIBKPaymentTxnTrailer("processingFlag=Y000, batchTotal=00030000, batchAmount=000000041058.00, hashTotal=00000242.005287");
		
		assertEquals(expectedHeader, (BatchStagedIBKPaymentTxnHeader)resultMap.get(HEADER_LIST).get(0));
		assertEquals(expectedDetail1, (BatchStagedIBKPaymentTxnDetail)resultMap.get(DETAIL_LIST).get(0));
		assertEquals(expectedDetail2, (BatchStagedIBKPaymentTxnDetail)resultMap.get(DETAIL_LIST).get(1));
		assertEquals(expectedDetail3, (BatchStagedIBKPaymentTxnDetail)resultMap.get(DETAIL_LIST).get(2));
		assertEquals(expectedTrailer, (BatchStagedIBKPaymentTxnTrailer)resultMap.get(TRAILER_LIST).get(0));
	}
	
	/*
	 * Test to check whether processor able to process the object correctly, at the end the process shall set additional info to the context for reference by detail
	 */
	@Test
	public void testPositiveProcessor() throws Exception {
		BatchStagedIBKPaymentTxnHeader header = createBatchStagedIBKPaymentTxnHeader("batchNumber=0001, processDate=20180516, billerAccountNo=21412900159831, billerAccountName=UTest");
		assertNull(itemProcessor.process(header));
		// Additional parameters set into the context during itemprocessor processing 
		assertEquals("20180516", stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_PROCESS_DATE));
		assertEquals("UTest", stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NAME));
		assertEquals("21412900159831", stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NO));
		
		BatchStagedIBKPaymentTxnDetail detail = createBatchStagedIBKPaymentTxnDetail("processDate=null, billerAccountNo=null, billerAccountName=null, txnId=034554, txnDate=20180516, txnAmount=000000000200.00, txnType=CR, txnDescription=, billerRefNo1=834743882, billerRefNo2=0125289475, billerRefNo3=MOHD ALIF BIN ABDUL WAHAB, txnTime=005813");
		BatchStagedIBKPaymentTxnDetail detailResponse = (BatchStagedIBKPaymentTxnDetail)itemProcessor.process(detail);
		assertNotNull(detailResponse);
		assertNotNull(detailResponse.getProcessDate());
		assertNotNull(detailResponse.getBillerAccountNo());
		assertNotNull(detailResponse.getBillerAccountName());
		assertEquals(header.getProcessDate(), detailResponse.getProcessDate());
		assertEquals(header.getBillerAccountNo(), detailResponse.getBillerAccountNo());
		assertEquals(header.getBillerAccountName(), detailResponse.getBillerAccountName());

		BatchStagedIBKPaymentTxnTrailer trailer = createBatchStagedIBKPaymentTxnTrailer("processingFlag=Y000, batchTotal=00000010, batchAmount=000000041058.00, hashTotal=00000242.005287");
		assertNull(itemProcessor.process(trailer));
	}

	/*
	 * Test to ensure processor able to verify the date in header must in the right format, e.g. yyyyMMdd
	 */
	@Test
	public void testNegativeProcessorInvalidHeaderProcessDate() throws Exception {
		BatchStagedIBKPaymentTxnHeader header = createBatchStagedIBKPaymentTxnHeader("batchNumber=0001, processDate=2018, billerAccountNo=21412900159831, billerAccountName=UTest");
		
		expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FIELD_VALIDATION_ERROR + ":" + BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
		
		itemProcessor.process(header);
	}
	
	/*
	 * Test to ensure processor able to verify the time in header must in the right format, e.g. HHmmss
	 */
	@Test
	public void testNegativeProcessorInvalidDetailTxnTime() throws Exception {
		BatchStagedIBKPaymentTxnHeader header = createBatchStagedIBKPaymentTxnHeader("batchNumber=0001, processDate=20180516, billerAccountNo=21412900159831, billerAccountName=UTest");
		itemProcessor.process(header);
		
		BatchStagedIBKPaymentTxnDetail detail = createBatchStagedIBKPaymentTxnDetail("processDate=null, billerAccountNo=null, billerAccountName=null, txnId=034554, txnDate=20180516, txnAmount=000000000200.00, txnType=CR, txnDescription=, billerRefNo1=834743882, billerRefNo2=0125289475, billerRefNo3=MOHD ALIF BIN ABDUL WAHAB, txnTime=00");
		
		expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FIELD_VALIDATION_ERROR + ":" + BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
		
		itemProcessor.process(detail);
	}
	
	/*
	 * Test to ensure processor able to verify the writer able to insert to the DB as expected
	 */
	@Test
	public void testPositiveWriter() throws Exception {
		String billerCode = "9999";
		stepExecution.getJobExecution().getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_CODE, billerCode);
		
		BatchStagedIBKPaymentTxnDetail detail = createBatchStagedIBKPaymentTxnDetail("processDate=20180516, billerAccountNo=21412900159831, billerAccountName=UTest, txnId=034554, txnDate=20180516, txnAmount=000000000200.00, txnType=CR, txnDescription=, billerRefNo1=834743882, billerRefNo2=0125289475, billerRefNo3=MOHD ALIF BIN ABDUL WAHAB, txnTime=005813");
		List<BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxns = new ArrayList<>();
		batchStagedIBKPaymentTxns.add(detail);
		
		when(batchStagedIBKPaymentTxnRepository.addBatchStagedIBKPaymentTxnToStaging(detail)).thenReturn(1);
		
		itemWriter.write(batchStagedIBKPaymentTxns);
		
		String expectedBillerCode = "9999";
		String expectedFileName = String.format("%s20180623.txt", billerCode);
		assertEquals((long)stepExecution.getJobExecutionId(), detail.getJobExecutionId());
		assertEquals(expectedFileName, detail.getFileName());
		assertEquals(expectedBillerCode, detail.getBillerCode());
		verify(batchStagedIBKPaymentTxnRepository, times(1)).addBatchStagedIBKPaymentTxnToStaging(detail);
	}
	
	/*
	 * Test to ensure if service down the writer shall not proceed further
	 */
	@Test
	public void testNegativeWriterDBServiceDown() throws Exception {
		String billerCode = "9999";
		stepExecution.getJobExecution().getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_CODE, billerCode);
		
		BatchStagedIBKPaymentTxnDetail detail = createBatchStagedIBKPaymentTxnDetail("processDate=20180516, billerAccountNo=21412900159831, billerAccountName=UTest, txnId=034554, txnDate=20180516, txnAmount=000000000200.00, txnType=CR, txnDescription=, billerRefNo1=834743882, billerRefNo2=0125289475, billerRefNo3=MOHD ALIF BIN ABDUL WAHAB, txnTime=00");
		List<BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxns = new ArrayList<>();
		batchStagedIBKPaymentTxns.add(detail);
		
		when(batchStagedIBKPaymentTxnRepository.addBatchStagedIBKPaymentTxnToStaging(detail))
			.thenThrow(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
		expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.DB_SYSTEM_ERROR + ":" + BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE);
		
		itemWriter.write(batchStagedIBKPaymentTxns);
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
	
	private BatchStagedIBKPaymentTxnHeader createBatchStagedIBKPaymentTxnHeader(String line) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Map<String, String> keyValueMap = getKeyValueMap(line);
		BatchStagedIBKPaymentTxnHeader batchStagedIBKPaymentTxn = new BatchStagedIBKPaymentTxnHeader();
		for(Entry<String, String> entry : keyValueMap.entrySet()) {
			if(!entry.getValue().equals("null")) {
				PropertyUtils.setProperty(batchStagedIBKPaymentTxn, entry.getKey(), entry.getValue());
			}
		}
		return batchStagedIBKPaymentTxn;
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
	
	private BatchStagedIBKPaymentTxnTrailer createBatchStagedIBKPaymentTxnTrailer(String line) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Map<String, String> keyValueMap = getKeyValueMap(line);
		BatchStagedIBKPaymentTxnTrailer batchStagedIBKPaymentTxn = new BatchStagedIBKPaymentTxnTrailer();
		for(Entry<String, String> entry : keyValueMap.entrySet()) {
			if(!entry.getValue().equals("null")) {
				PropertyUtils.setProperty(batchStagedIBKPaymentTxn, entry.getKey(), entry.getValue());
			}
		}
		return batchStagedIBKPaymentTxn;
	}
}
