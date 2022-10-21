package com.rhbgroup.dcp.bo.batch.test.step;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.log4j.spi.LoggingEvent;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadMassNotificationsJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotifMass;
import com.rhbgroup.dcp.bo.batch.job.model.LoadMassNotifications;
import com.rhbgroup.dcp.bo.batch.job.model.LoadMassNotificationsDetail;
import com.rhbgroup.dcp.bo.batch.job.model.LoadMassNotificationsHeader;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedNotifMassRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UserProfileRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({ 
	DependencyInjectionTestExecutionListener.class, 
	StepScopeTestExecutionListener.class,
	MockitoTestExecutionListener.class })
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class LoadMassNotificationsFileToStagingStepTest extends BaseJobTest {
	
	private static final String STEP_NAME = "LoadMassNotificationsFileToStagingStep";
	
	private static final String FILE_FOLDER = "dcp_mass_notification_from";
	
	private static final String FILE_NAME = "DCP_LDCPA6005T_20181206.txt";
	
	private static final String WORKING_DIR = System.getProperty("user.dir");
	
	private static final String HEADER_LIST = "HeaderList";
	
	private static final String DETAIL_LIST = "DetailList";
	
	private static final Date PARAMETER_DATE = Date.from(Instant.parse("2007-12-03T10:15:30.00Z"));
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private FlatFileItemReader<LoadMassNotifications> itemReader;
	
	@Autowired
    @Qualifier(STEP_NAME + ".ItemProcessor")
    private ItemProcessor<LoadMassNotifications, LoadMassNotifications> itemProcessor;
    
	@Autowired
    @Qualifier(STEP_NAME + ".ItemWriter")
    private ItemWriter<LoadMassNotifications> itemWriter;
	
	@Autowired
	private LoadMassNotificationsJobConfigProperties configProperties;
	
	@MockBean
	private BatchStagedNotifMassRepositoryImpl mockBatchStagedNotifMassRepositoryImpl;
	
	@MockBean
	private UserProfileRepositoryImpl mockUserProfileRepositoryImpl;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	private StepExecution stepExecution;
	
	public StepExecution getStepExecution() {
		Map<String, Object> jobParamMap = new HashMap<>();
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY, TEST_JOB);
		jobParamMap.put(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, DateUtils.formatDate(PARAMETER_DATE, configProperties.getNameDateFormat()));
		stepExecution = createStepExecution(STEP_NAME, jobParamMap, null);
		
		return stepExecution;
	}
	
	@Before
	public void beforeLocalTest() throws IOException {
		super.beforeTest();
		cleanupFolder();
	}
	
	@After
	public void afterLocalTest() {
		Mockito.reset(mockBatchStagedNotifMassRepositoryImpl);
		Mockito.reset(mockUserProfileRepositoryImpl);
	}
	
	/**
	 * Positive case for ItemReader with default parameter
	 * @throws Exception
	 */
	@Test
	public void testPositiveReader() throws Exception {
		createFolder();
		createInputFile();
		
		itemReader.open(new ExecutionContext());
		
		Map<String, List<LoadMassNotifications>> resultMap = StepScopeTestUtils.doInStepScope(stepExecution, new Callable<Map<String, List<LoadMassNotifications>>>() {
			public Map<String, List<LoadMassNotifications>> call() throws Exception {
				Map<String, List<LoadMassNotifications>> resultMap = new HashMap<>();
				List<LoadMassNotifications> headers = new ArrayList<>();
				List<LoadMassNotifications> details = new ArrayList<>();
				LoadMassNotifications loadMassNotifications = null;
				
				while((loadMassNotifications = itemReader.read()) != null) {
					if (loadMassNotifications instanceof LoadMassNotificationsHeader) {
						headers.add(loadMassNotifications);
					} else if (loadMassNotifications instanceof LoadMassNotificationsDetail) {
						details.add(loadMassNotifications);
					}
				}
				
				resultMap.put(HEADER_LIST, headers);
				resultMap.put(DETAIL_LIST, details);
				
				return resultMap;
			}
		});
		
		assertEquals(1, resultMap.get(HEADER_LIST).size());
		assertEquals(1, resultMap.get(DETAIL_LIST).size());
		
		LoadMassNotificationsHeader expectedHeader = createLoadMassNotificationsHeader("recordIndicator=DH, eventCode=90002");
		LoadMassNotificationsDetail expectedDetail = createLoadMassNotificationsDetail("recordIndicator=DD, "
				+ "content=RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.");
		
		assertEquals(expectedHeader, (LoadMassNotificationsHeader)resultMap.get(HEADER_LIST).get(0));
		assertEquals(expectedDetail, (LoadMassNotificationsDetail)resultMap.get(DETAIL_LIST).get(0));
	}
	
	/**
	 * Positive case for ItemProcessor
	 * @throws Exception
	 */
	@Test
	public void testPositiveProcessor() throws Exception {
		LoadMassNotificationsHeader header = createLoadMassNotificationsHeader("recordIndicator=DH, eventCode=90002");
		assertNull(itemProcessor.process(header));
		assertEquals("90002", stepExecution.getExecutionContext().getString("eventCode"));
		
		LoadMassNotificationsDetail detail = createLoadMassNotificationsDetail("recordIndicator=DD, "
				+ "content=RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.");
		LoadMassNotificationsDetail detailRepsonse = (LoadMassNotificationsDetail)itemProcessor.process(detail);
		assertEquals(detailRepsonse.getContent(), stepExecution.getExecutionContext().getString("content"));
	}
	
	/**
	 * Positive case for ItemWriter
	 * @throws Exception
	 */
	@Test
	public void testPositiveWriter() throws Exception {
		LoadMassNotificationsHeader header = createLoadMassNotificationsHeader("recordIndicator=DH, eventCode=90002");
		itemProcessor.process(header);
		
		LoadMassNotificationsDetail detail = createLoadMassNotificationsDetail("recordIndicator=DD, "
				+ "content=RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.");
		itemProcessor.process(detail);
		
		List<LoadMassNotifications> loadMassNotificationsList = new ArrayList<>();
		loadMassNotificationsList.add(header);
		loadMassNotificationsList.add(detail);
		
		List<Map<String, Object>> mockActiveUserProfilesList = new ArrayList<>();
		Map<String, Object> mockActiveUserProfiles = new HashMap<>();
		mockActiveUserProfiles.put("ID", 1L);
		mockActiveUserProfiles.put("CIS_NO", "01234567");
		mockActiveUserProfiles.put("USER_STATUS", "A");
		mockActiveUserProfilesList.add(mockActiveUserProfiles);
		
		when(mockUserProfileRepositoryImpl.getActiveUserProfiles()).thenReturn(mockActiveUserProfilesList);
		when(mockBatchStagedNotifMassRepositoryImpl.addRecordBatchStagedNotifMass(Mockito.any(BatchStagedNotifMass.class))).thenReturn(1);
		
		itemWriter.write(loadMassNotificationsList);
		
		verify(mockUserProfileRepositoryImpl, atLeastOnce()).getActiveUserProfiles();
		
		verify(mockAppender, atLeastOnce()).doAppend((LoggingEvent) captorLoggingEvent.capture());
		LoggingEvent loggingEvent = (LoggingEvent) captorLoggingEvent.getAllValues().get(2);
		assertEquals("Batch Insert TBL_BATCH_STAGED_NOTIF_MASS [1]", loggingEvent.getMessage());
	}
	
	/**
	 * Positive case for ItemWriter more than 1 user status 'A'
	 * @throws Exception
	 */
	@Test
	public void testPositiveWriterMoreActiveUserProfiles() throws Exception {
		LoadMassNotificationsHeader header = createLoadMassNotificationsHeader("recordIndicator=DH, eventCode=90002");
		itemProcessor.process(header);
		
		LoadMassNotificationsDetail detail = createLoadMassNotificationsDetail("recordIndicator=DD, "
				+ "content=RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.");
		itemProcessor.process(detail);
		
		List<LoadMassNotifications> loadMassNotificationsList = new ArrayList<>();
		loadMassNotificationsList.add(header);
		loadMassNotificationsList.add(detail);
		
		List<Map<String, Object>> mockActiveUserProfilesList = new ArrayList<>();
		Map<String, Object> mockActiveUserProfiles1 = new HashMap<>();
		mockActiveUserProfiles1.put("ID", 1L);
		mockActiveUserProfiles1.put("CIS_NO", "01234567");
		mockActiveUserProfiles1.put("USER_STATUS", "A");
		mockActiveUserProfilesList.add(mockActiveUserProfiles1);
		Map<String, Object> mockActiveUserProfiles2 = new HashMap<>();
		mockActiveUserProfiles2.put("ID", 2L);
		mockActiveUserProfiles2.put("CIS_NO", "01234568");
		mockActiveUserProfiles2.put("USER_STATUS", "A");
		mockActiveUserProfilesList.add(mockActiveUserProfiles2);
		
		when(mockUserProfileRepositoryImpl.getActiveUserProfiles()).thenReturn(mockActiveUserProfilesList);
		when(mockBatchStagedNotifMassRepositoryImpl.addRecordBatchStagedNotifMass(Mockito.any(BatchStagedNotifMass.class))).thenReturn(1);
		
		itemWriter.write(loadMassNotificationsList);
		
		verify(mockUserProfileRepositoryImpl, atLeastOnce()).getActiveUserProfiles();
		
		verify(mockAppender, atLeastOnce()).doAppend((LoggingEvent) captorLoggingEvent.capture());
		LoggingEvent loggingEvent = (LoggingEvent) captorLoggingEvent.getAllValues().get(2);
		assertEquals("Batch Insert TBL_BATCH_STAGED_NOTIF_MASS [2]", loggingEvent.getMessage());
	}
	
	/**
	 * Positive case for ItemWriter if no user status 'A'
	 * @throws Exception
	 */
	@Test
	public void testPositiveWriterNoActiveUserProfiles() throws Exception {
		LoadMassNotificationsHeader header = createLoadMassNotificationsHeader("recordIndicator=DH, eventCode=90002");
		itemProcessor.process(header);
		
		LoadMassNotificationsDetail detail = createLoadMassNotificationsDetail("recordIndicator=DD, "
				+ "content=RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.");
		itemProcessor.process(detail);
		
		List<LoadMassNotifications> loadMassNotificationsList = new ArrayList<>();
		loadMassNotificationsList.add(header);
		loadMassNotificationsList.add(detail);
		
		List<Map<String, Object>> mockActiveUserProfilesList = new ArrayList<>();
		
		when(mockUserProfileRepositoryImpl.getActiveUserProfiles()).thenReturn(mockActiveUserProfilesList);
		when(mockBatchStagedNotifMassRepositoryImpl.addRecordBatchStagedNotifMass(Mockito.any(BatchStagedNotifMass.class))).thenReturn(1);
		
		itemWriter.write(loadMassNotificationsList);
		
		verify(mockUserProfileRepositoryImpl, atLeastOnce()).getActiveUserProfiles();
		
		verify(mockAppender, atLeastOnce()).doAppend((LoggingEvent) captorLoggingEvent.capture());
		LoggingEvent loggingEvent = (LoggingEvent) captorLoggingEvent.getAllValues().get(2);
		assertEquals("Batch Insert TBL_BATCH_STAGED_NOTIF_MASS [0]", loggingEvent.getMessage());
	}
	
	/**
	 * Negative case for ItemReader if file not found
	 * @throws Exception
	 */
	@Test
	public void testNegativeReaderFileNotFound() throws Exception {
		expectedEx.expect(ItemStreamException.class);
		expectedEx.expectMessage(Matchers.containsString("Failed to initialize the reader"));
		
		itemReader.open(new ExecutionContext());
		
		StepScopeTestUtils.doInStepScope(stepExecution, new Callable<Map<String, List<LoadMassNotifications>>>() {
			public Map<String, List<LoadMassNotifications>> call() throws Exception {
				Map<String, List<LoadMassNotifications>> resultMap = new HashMap<>();
				List<LoadMassNotifications> headers = new ArrayList<>();
				List<LoadMassNotifications> details = new ArrayList<>();
				LoadMassNotifications loadMassNotifications = null;
				
				while((loadMassNotifications = itemReader.read()) != null) {
					if (loadMassNotifications instanceof LoadMassNotificationsHeader) {
						headers.add(loadMassNotifications);
					} else if (loadMassNotifications instanceof LoadMassNotificationsDetail) {
						details.add(loadMassNotifications);
					}
				}
				
				resultMap.put(HEADER_LIST, headers);
				resultMap.put(DETAIL_LIST, details);
				
				return resultMap;
			}
		});
	}
	
	/**
	 * Negative case for ItemProcessor if character is exceed 145 characters
	 * @throws Exception
	 */
	@Test
	public void testNegativeProcessorExceedMaxLength() throws Exception {
		LoadMassNotificationsHeader header = createLoadMassNotificationsHeader("recordIndicator=DH, eventCode=90002");
		assertNull(itemProcessor.process(header));
		assertEquals("90002", stepExecution.getExecutionContext().getString("eventCode"));
		
		LoadMassNotificationsDetail detail = createLoadMassNotificationsDetail("recordIndicator=DD, "
				+ "content=RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now. ppppppppppppppppppppppppppppppppppppppppppp");
		assertNull(itemProcessor.process(detail));
		assertTrue(stepExecution.getJobExecution().getFailureExceptions().size() > 0);
	}
	
	/**
	 * Negative case for ItemProcessor if event code length is exceed 5 digit
	 * @throws Exception
	 */
	@Test
	public void testNegativeProcessorWrongEventCodeLength() throws Exception {
		LoadMassNotificationsHeader header = createLoadMassNotificationsHeader("recordIndicator=DH, eventCode=900002");
		assertNull(itemProcessor.process(header));
		assertTrue(stepExecution.getJobExecution().getFailureExceptions().size() > 0);
	}
	
	/**
	 * Negative case for ItemWriter if error reading TBL_USER_PROFILE table
	 * @throws Exception
	 */
	@Test
	public void testNegativeWriterActiveUser() throws Exception {
		LoadMassNotificationsHeader header = createLoadMassNotificationsHeader("recordIndicator=DH, eventCode=90002");
		itemProcessor.process(header);
		
		LoadMassNotificationsDetail detail = createLoadMassNotificationsDetail("recordIndicator=DD, "
				+ "content=RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.");
		itemProcessor.process(detail);
		
		List<LoadMassNotifications> loadMassNotificationsList = new ArrayList<>();
		loadMassNotificationsList.add(header);
		loadMassNotificationsList.add(detail);
		
		when(mockUserProfileRepositoryImpl.getActiveUserProfiles()).thenThrow(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, 
				"Error happened while reading record in DB TBL_USER_PROFILE"));
		
		itemWriter.write(loadMassNotificationsList);
		
		assertTrue(stepExecution.getJobExecution().getFailureExceptions().size() > 0);
	}
	
	/**
	 * Negative case for ItemWriter if error inserting data into  TBL_BATCH_STAGED_NOTIF_MASS table
	 * @throws Exception
	 */
	@Test
	public void testNegativeWriterBatchStagedNotifMass() throws Exception {
		LoadMassNotificationsHeader header = createLoadMassNotificationsHeader("recordIndicator=DH, eventCode=90002");
		itemProcessor.process(header);
		
		LoadMassNotificationsDetail detail = createLoadMassNotificationsDetail("recordIndicator=DD, "
				+ "content=RHB:Get Premium Mortgage with good flexibility and lots of savings!Apply via RHB Mobile Banking App now.");
		itemProcessor.process(detail);
		
		List<LoadMassNotifications> loadMassNotificationsList = new ArrayList<>();
		loadMassNotificationsList.add(header);
		loadMassNotificationsList.add(detail);
		
		List<Map<String, Object>> mockActiveUserProfilesList = new ArrayList<>();
		Map<String, Object> mockActiveUserProfiles = new HashMap<>();
		mockActiveUserProfiles.put("ID", 1L);
		mockActiveUserProfiles.put("CIS_NO", "01234567");
		mockActiveUserProfiles.put("USER_STATUS", "A");
		mockActiveUserProfilesList.add(mockActiveUserProfiles);
		
		when(mockUserProfileRepositoryImpl.getActiveUserProfiles()).thenReturn(mockActiveUserProfilesList);
		when(mockBatchStagedNotifMassRepositoryImpl.addRecordBatchStagedNotifMass(Mockito.any(BatchStagedNotifMass.class))).thenThrow(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, 
				"Error happened while inserting in DB TBL_BATCH_STAGED_NOTIF_MASS"));
		
		itemWriter.write(loadMassNotificationsList);
		
		assertTrue(stepExecution.getJobExecution().getFailureExceptions().size() > 0);
	}	
	
	private LoadMassNotificationsHeader createLoadMassNotificationsHeader(String line) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Map<String, String> keyValueMap = getKeyValueMap(line);
		LoadMassNotificationsHeader loadMassNotificationsHeader = new LoadMassNotificationsHeader();
		for(Entry<String, String> entry : keyValueMap.entrySet()) {
			if(!entry.getValue().equals("null")) {
				PropertyUtils.setProperty(loadMassNotificationsHeader, entry.getKey(), entry.getValue());
			}
		}
		return loadMassNotificationsHeader;
	}
	
	private LoadMassNotificationsDetail createLoadMassNotificationsDetail(String line) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Map<String, String> keyValueMap = getKeyValueMap(line);
		LoadMassNotificationsDetail loadMassNotificationsDetail = new LoadMassNotificationsDetail();
		for(Entry<String, String> entry : keyValueMap.entrySet()) {
			if(!entry.getValue().equals("null")) {
				PropertyUtils.setProperty(loadMassNotificationsDetail, entry.getKey(), entry.getValue());
			}
		}
		return loadMassNotificationsDetail;
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
	
	private void cleanupFolder() throws IOException {
		File inputFolder = new File(generateFolderPath(WORKING_DIR, "target", "batch", "input", FILE_FOLDER));
    	deleteFolderIfExists(inputFolder);
    }
	
	private void deleteFolderIfExists(File folder){
		try {
			if(folder.exists() && folder.isDirectory()) {
				FileUtils.deleteDirectory(folder);
			}
		}
		catch (Exception ex) {
			System.out.print(ex.getMessage());
		}
	}
	
	private void createFolder() throws IOException {
		File inputFolder = new File(generateFolderPath(WORKING_DIR, "target", "batch", "input", FILE_FOLDER));
		createFolderIfNotExists(inputFolder);
	}
	
	private void createFolderIfNotExists(File folder) throws IOException {
		if(!folder.exists()) {
			FileUtils.forceMkdir(folder);
    	}
	}
	
	private void createInputFile() throws IOException {
		File sourceFile = new File(generateFolderPath(WORKING_DIR, "src", "test", "resources", "batch", "input", FILE_FOLDER, FILE_NAME));
		String inputFilename = FILE_NAME;
		inputFilename = inputFilename.replace("20181206", DateUtils.formatDate(PARAMETER_DATE, configProperties.getNameDateFormat()));
		String inputFilePath = generateFolderPath(WORKING_DIR, "target", "batch", "input", FILE_FOLDER, inputFilename);
		File inputFile = new File(inputFilePath);
		createInputFileIfNotExists(sourceFile, inputFile);
	}
	
	private void createInputFileIfNotExists(File sourceFile, File inputFile) throws IOException {
		if(!inputFile.exists()) {
			FileUtils.copyFile(sourceFile, inputFile);
    	}
	}

}
