package com.rhbgroup.dcp.bo.batch.test.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedJompayFailureTxn;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedJompayFailureTxnRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class LoadIBKJompayFailureValidationExtractionStepTest extends BaseFTPJobTest {
	
	private static final String STEP_NAME = "LoadIBKJompayFailureValidationExtractionStep";
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private FlatFileItemReader<BatchStagedJompayFailureTxn> itemReader;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchStagedJompayFailureTxn, BatchStagedJompayFailureTxn> itemProcessor;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchStagedJompayFailureTxn> itemWriter;

	@Autowired
	private FTPIBKConfigProperties ftpConfigProperties;

	@MockBean
	private BatchStagedJompayFailureTxnRepositoryImpl batchStagedJompayFailureTxnRepository;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	private StepExecution stepExecution;

	public StepExecution getStepExection() throws FileNotFoundException {
		File file = getResourceFile("ftp/nbps_channel_to/IBKUD041_20180612.txt");
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
		Mockito.reset(batchStagedJompayFailureTxnRepository);
	}
	
	@After
	public void afterLocalTest() throws Exception {
		setCustomFTP(null);
		super.afterFTPTest();
	}
	
	/*
	 * Test to ensure the reader able to read the file and then parse them to objects correctly
	 */
	@Test
	public void testPositiveReader() throws Exception {
		itemReader.open(new ExecutionContext());
		List<BatchStagedJompayFailureTxn> result = StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchStagedJompayFailureTxn>>() {
			public List<BatchStagedJompayFailureTxn> call() throws Exception {
				BatchStagedJompayFailureTxn batchStagedJompayFailureTxn;
				List<BatchStagedJompayFailureTxn> batchStagedJompayFailureTxns = new ArrayList<>();
				while((batchStagedJompayFailureTxn = itemReader.read()) != null) {
					batchStagedJompayFailureTxns.add(batchStagedJompayFailureTxn);
				}
				return batchStagedJompayFailureTxns;
			}
		});
		
		assertTrue(2 == result.size());
		
		BatchStagedJompayFailureTxn expectedObj1 = createBatchStagedJompayFailureTxn("4600", "3", "180806111110", "Timeout", null);
		BatchStagedJompayFailureTxn expectedObj2 = createBatchStagedJompayFailureTxn("4600", "3", "180806222220", "Network Issue", null);
		
		compareBatchStagedJompayFailureTxn(expectedObj1, result.get(0));
		compareBatchStagedJompayFailureTxn(expectedObj2, result.get(1));
	}
	
	/*
	 * Test to ensure if the target file not found the job execution should failed
	 */
	@Test
	public void testNegativeReaderFileNotFound() throws Exception {
		stepExecution.getJobExecution().getExecutionContext().remove(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
		
		expectedEx.expect(BeanCreationException.class);
		expectedEx.expectMessage(Matchers.containsString("Path must not be null"));
		
		itemReader.open(new ExecutionContext());
		
		StepScopeTestUtils.doInStepScope(stepExecution, new Callable<List<BatchStagedJompayFailureTxn>>() {
			public List<BatchStagedJompayFailureTxn> call() throws Exception {
				BatchStagedJompayFailureTxn batchStagedJompayFailureTxn;
				List<BatchStagedJompayFailureTxn> batchStagedJompayFailureTxns = new ArrayList<>();
				while((batchStagedJompayFailureTxn = itemReader.read()) != null) {
					batchStagedJompayFailureTxns.add(batchStagedJompayFailureTxn);
				}
				return batchStagedJompayFailureTxns;
			}
		});
	}
	
	/*
	 * Test to ensure what object passed to the processor shall return as same as original
	 */
	@Test
	public void testPositiveProcessor() throws Exception {
		BatchStagedJompayFailureTxn original = createBatchStagedJompayFailureTxn("4600", "3", "180806111110", "Timeout", null);
		BatchStagedJompayFailureTxn actual = itemProcessor.process(original);
		compareBatchStagedJompayFailureTxn(original, actual);
	}
	
	/*
	 * Test to ensure writer able to process each object accordingly and insert to the DB correctly
	 */
	@Test
	public void testPositiveWriter() throws Exception {
		BatchStagedJompayFailureTxn batchStagedJompayFailureTxn1 = createBatchStagedJompayFailureTxn("4600", "3", "180806111110", "Timeout", null);
		BatchStagedJompayFailureTxn batchStagedJompayFailureTxn2 = createBatchStagedJompayFailureTxn("4600", "3", "180806222220", "Network Issue", null);
		List<BatchStagedJompayFailureTxn> batchStagedJompayFailureTxns = new ArrayList<>();
		batchStagedJompayFailureTxns.add(batchStagedJompayFailureTxn1);
		batchStagedJompayFailureTxns.add(batchStagedJompayFailureTxn2);
		
		itemWriter.write(batchStagedJompayFailureTxns);
		
		verify(batchStagedJompayFailureTxnRepository, times(1)).addBatchStagedIBKPaymentTxnToStaging(batchStagedJompayFailureTxn1);
		verify(batchStagedJompayFailureTxnRepository, times(1)).addBatchStagedIBKPaymentTxnToStaging(batchStagedJompayFailureTxn2);
		
		String expectedFileName = "IBKUD041_20180612.txt";
		assertEquals(expectedFileName, batchStagedJompayFailureTxn1.getFileName());
		assertNotNull(batchStagedJompayFailureTxn1.getCreatedTime());
		assertEquals(expectedFileName, batchStagedJompayFailureTxn2.getFileName());
		assertNotNull(batchStagedJompayFailureTxn2.getCreatedTime());
	}
	
	/*
	 * Test to ensure in case DB issues hit the writer shall proceed to other record without stop but the final batch status shall end with FAILED status
	 */
	@Test
	public void testNegativeWriterDBIssue() throws Exception {
		BatchStagedJompayFailureTxn batchStagedJompayFailureTxn1 = createBatchStagedJompayFailureTxn("4600", "3", "180806111110", "Timeout", null);
		BatchStagedJompayFailureTxn batchStagedJompayFailureTxn2 = createBatchStagedJompayFailureTxn("4600", "3", "180806222220", "Network Issue", null);
		List<BatchStagedJompayFailureTxn> batchStagedJompayFailureTxns = new ArrayList<>();
		batchStagedJompayFailureTxns.add(batchStagedJompayFailureTxn1);
		batchStagedJompayFailureTxns.add(batchStagedJompayFailureTxn2);
		
		when(batchStagedJompayFailureTxnRepository.addBatchStagedIBKPaymentTxnToStaging(batchStagedJompayFailureTxn1)).thenThrow(BatchException.class);
		
		itemWriter.write(batchStagedJompayFailureTxns);
		
		verify(batchStagedJompayFailureTxnRepository, times(1)).addBatchStagedIBKPaymentTxnToStaging(batchStagedJompayFailureTxn2);
		
		String expectedFileName = "IBKUD041_20180612.txt";
		assertEquals(expectedFileName, batchStagedJompayFailureTxn2.getFileName());
		assertNotNull(batchStagedJompayFailureTxn2.getCreatedTime());
		
		assertEquals(BatchStatus.FAILED, stepExecution.getJobExecution().getStatus());
	}
	
	/*
	 * Test to ensure in case object got any invalid field(s) it shall be treated as invalid row and the writer execution shall end with FAILED batch status
	 */
	@Test
	public void testNegativeWriterInvalidFields() throws Exception {
		BatchStagedJompayFailureTxn batchStagedJompayFailureTxn1 = createBatchStagedJompayFailureTxn("4600", "3", "A80806111110", "Timeout", null);
		List<BatchStagedJompayFailureTxn> batchStagedJompayFailureTxns = new ArrayList<>();
		batchStagedJompayFailureTxns.add(batchStagedJompayFailureTxn1);
		
		itemWriter.write(batchStagedJompayFailureTxns);
		
		assertEquals(BatchStatus.FAILED, stepExecution.getJobExecution().getStatus());
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
	
	private void compareBatchStagedJompayFailureTxn(BatchStagedJompayFailureTxn expected, BatchStagedJompayFailureTxn actual) {
		assertEquals(expected.getBillerCode(), actual.getBillerCode());
		assertEquals(expected.getPaymentChannel(), actual.getPaymentChannel());
		assertEquals(expected.getRequestTime(), actual.getRequestTime());
		assertEquals(expected.getReasonForFailure(), actual.getReasonForFailure());
		assertEquals(expected.getFileName(), actual.getFileName());
		
	}
}
