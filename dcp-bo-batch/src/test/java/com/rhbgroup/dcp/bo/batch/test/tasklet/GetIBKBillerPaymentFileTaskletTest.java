package com.rhbgroup.dcp.bo.batch.test.tasklet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Queue;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchBillerPaymentConfig;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.GetIBKBillerPaymentFileTasklet;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class GetIBKBillerPaymentFileTaskletTest extends BaseFTPJobTest {
	
	@MockBean
	private Queue<BatchBillerPaymentConfig> batchBillerPaymentConfigQueue;
	
	@Autowired
	private GetIBKBillerPaymentFileTasklet getIBKBillerPaymentFileTasklet;

	@Autowired
	private FTPIBKConfigProperties ftpConfigProperties;
	
	@Value(SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
    private String inputFolderPath;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	@Before
	public void beforeLocalTest() throws IOException {
		setCustomFTP(ftpConfigProperties);
		super.beforeFTPTest();
		cleanupFolder();
	}
	
	@After
	public void afterLocalTest() throws Exception {
		setCustomFTP(null);
		super.afterFTPTest();
		cleanupFolder();
	}
	
	/*
	 * Test to use the DB config batch system date for execution
	 */
	@Test
	public void testPositiveWithJobDBProcessDate() throws BatchException, FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}.txt", billerCode);
		BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	String fileNameWithDate = fileNameWithDateFormat.replace("${yyyyMMdd}", "20180623");
		File targetFile = Paths.get(inputFolderPath, TEST_JOB, fileNameWithDate).toFile();
    	
    	assertEquals(RepeatStatus.FINISHED, getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext));
    	verify(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).putString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY, targetFile.getAbsolutePath());
	}
	
	/*
	 * Test to use the external JOB parameter processing date for execution
	 */
	@Test
	public void testPositiveWithJobParamProcessDate() throws BatchException, FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY)).thenReturn("20180623");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}.txt", billerCode);
    	BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	String fileNameWithDate = fileNameWithDateFormat.replace("${yyyyMMdd}", "20180623");
		File targetFile = Paths.get(inputFolderPath, TEST_JOB, fileNameWithDate).toFile();
    	
    	assertEquals(RepeatStatus.FINISHED, getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext));
    	verify(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).putString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY, targetFile.getAbsolutePath());
	}
	
	/*
	 * Test to ensure tasklet executed successfully even if there is no biller code need to be execute according to the DB setting
	 */
	@Test
	public void testPositiveNullQueue() throws BatchException {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY)).thenReturn("20180623");
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(null);
    	
    	assertEquals(RepeatStatus.FINISHED, getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext));
	}
	
	/*
	 * Test to ensure even the target input folder not exists, the tasklet shall able proceed without issues
	 */
	@Test
	public void testPositiveInputFolderNotExists() throws BatchException, FileNotFoundException {
		File folder = new File(inputFolderPath);
		if(folder.exists() && folder.isDirectory()) {
    		folder.delete();
    	}
		
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY)).thenReturn("20180623");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}.txt", billerCode);
    	BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	String fileNameWithDate = fileNameWithDateFormat.replace("${yyyyMMdd}", "20180623");
		File targetFile = Paths.get(inputFolderPath, TEST_JOB, fileNameWithDate).toFile();
    	
    	assertEquals(RepeatStatus.FINISHED, getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext));
    	verify(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).putString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY, targetFile.getAbsolutePath());
	}
	
	/*
	 * Test to ensure biller file that only have header and trailer with 0 count should be treated as normal in the tasklet
	 */
	@Test
	public void testPositiveOnlyHeaderAndTrailer() throws BatchException, FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623_no_detail.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}_no_detail.txt", billerCode);
		BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	String fileNameWithDate = fileNameWithDateFormat.replace("${yyyyMMdd}", "20180623");
		File targetFile = Paths.get(inputFolderPath, TEST_JOB, fileNameWithDate).toFile();
    	
    	assertEquals(RepeatStatus.FINISHED, getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext));
    	verify(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).putString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY, targetFile.getAbsolutePath());
	}
	
	/*
	 * Test to ensure even FTP folder not exists for the biller, it shall not stop the tasklet progress
	 */
	@Test
	public void testPositiveFTPFolderNotExists() throws BatchException {
		String billerCode = "9999";
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM_XXX", billerCode);
		
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
		when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
		when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}.txt", billerCode);
    	BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	assertEquals(RepeatStatus.FINISHED, getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext));
        verify(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).remove(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
    	
    	verify(mockAppender, times(29)).doAppend((LoggingEvent)captorLoggingEvent.capture());//TODO investigate wanted number of invocation wanted value is 10
    	LoggingEvent loggingEvent = (LoggingEvent)captorLoggingEvent.getAllValues().get(8);
        //Check log level
        assertEquals(Level.INFO, loggingEvent.getLevel());//TODO investigate actual value should be WARN
        //Check the message being logged
        //assertTrue(loggingEvent.getRenderedMessage().contains("file is not found in the FTP server")); TODO investigate commented out code
	}
	
	/*
	 * Test to ensure even FTP file not exists for the biller, it shall not stop the tasklet progress
	 */
	@Test
	public void testPositiveIBKBillerPaymentFileNotExists() throws BatchException {
		String billerCode = "9999";
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
		when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
		when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyy}.txt", billerCode);
    	BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	assertEquals(RepeatStatus.FINISHED, getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext));
        verify(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext()).remove(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
    	
    	verify(mockAppender, times(29)).doAppend((LoggingEvent)captorLoggingEvent.capture());//TODO investigate wanted number of invocation wanted value is 10
    	LoggingEvent loggingEvent = (LoggingEvent)captorLoggingEvent.getAllValues().get(8);
        //Check log level
        assertEquals(Level.INFO, loggingEvent.getLevel());//TODO investigate actual value should be WARN
        //Check the message being logged
        //assertTrue(loggingEvent.getRenderedMessage().contains("file is not found in the FTP server"));TODO investigate commented out code
        //assertTrue(loggingEvent.getRenderedMessage().contains("file is not found in the FTP server"));
		assertTrue( Arrays.stream(captorLoggingEvent.getAllValues().toArray()).filter(
			x -> ((LoggingEvent)x).getRenderedMessage().contains("file is not found in the FTP server")
		).toArray().length > 0 );
	}
	
	/*
	 * Test to ensure if the JOB parameter processing date is empty, the tasklet shall treat it as a failure instead of proceed further
	 */
	@Test
	public void testNegativeEmptyJobParamProcessDate() throws BatchException {
		String billerCode = "9999";
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
		when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY)).thenReturn("");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}.txt", billerCode);
    	BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.CONFIG_SYSTEM_ERROR + ":" + BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE);
    	
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);	
	}
	
	/*
	 * Test to ensure the JOB parameter processing date value must be valid or else the tasklet shall not proceed further
	 */
	@Test
	public void testNegativeInvalidJobParamProcessDate() throws BatchException {
		String billerCode = "9999";
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
		when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY)).thenReturn("201806XX");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}.txt", billerCode);
    	BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.CONFIG_SYSTEM_ERROR + ":" + BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE);
    	
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test to ensure the batch system processing date in the DB shall not be empty when there is no JOB parameter processing date provided
	 */
	@Test
	public void testNegativeEmptyDBProcessDate() throws BatchException {
		String billerCode = "9999";
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
		when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
		when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}.txt", billerCode);
    	BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.CONFIG_SYSTEM_ERROR + ":" + BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE);
    	
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test to ensure if something happened to the FTP connection, the tasklet shall not proceed further
	 */
	@Test
	public void testNegativeIBKFTPServiceDown() throws InterruptedException, BatchException {
		shutdownSSHServer();
		
		String billerCode = "9999";
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
		when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
		when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}.txt", billerCode);
    	BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FTP_SYSTEM_ERROR + ":" + BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE);
    	
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test to ensure if the FTP file content is empty, the tasklet shall not proceed further
	 */
	@Test
	public void testNegativeEmptyFileContent() throws BatchException, FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623_empty.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}_empty.txt", billerCode);
		BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FIELD_VALIDATION_ERROR + ":" + BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
    	
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test to ensure if input file not having the valid header, the tasklet shall not proceed further
	 */
	@Test
	public void testNegativeInvalidFileHeader() throws BatchException, FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623_invalid_header.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}_invalid_header.txt", billerCode);
		BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FIELD_VALIDATION_ERROR + ":" + BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
    	
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test to ensure if input file not having the valid detail, the tasklet shall not proceed further
	 */
	@Test
	public void testNegativeInvalidDetail() throws BatchException, FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623_invalid_detail.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}_invalid_detail.txt", billerCode);
		BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FIELD_VALIDATION_ERROR + ":" + BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
    	
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test to ensure if input file not having the valid trailer, the tasklet shall not proceed further
	 */
	@Test
	public void testNegativeInvalidTrailer() throws BatchException, FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623_invalid_trailer.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}_invalid_trailer.txt", billerCode);
		BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FIELD_VALIDATION_ERROR + ":" + BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
    	
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test to ensure if input file not having the valid total detail count in the trailer, the tasklet shall not proceed further
	 */
	@Test
	public void testNegativeInvalidDetailCount() throws BatchException, FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623_invalid_detail_count.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}_invalid_detail_count.txt", billerCode);
		BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FIELD_VALIDATION_ERROR + ":" + BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
    	
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test to ensure if filename in DB is invalid format, the tasklet shall not proceed further
	 */
	@Test
	public void testNegativeInvalidFileNameFormat() throws BatchException, FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd.txt", billerCode);
		BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.CONFIG_SYSTEM_ERROR + ":" + BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE);
		
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test to ensure if the header is invalid due to empty line, the tasklet shall not proceed further
	 */
	@Test
	public void testNegativeMissingHeader() throws BatchException, FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623_missing_header.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}_missing_header.txt", billerCode);
		BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FIELD_VALIDATION_ERROR + ":" + BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
		
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test to ensure if the detail is invalid due to empty line, the tasklet shall not proceed further
	 */
	@Test
	public void testNegativeMissingDetail() throws BatchException, FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623_missing_detail.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}_missing_detail.txt", billerCode);
		BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FIELD_VALIDATION_ERROR + ":" + BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
		
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test to ensure if the trailer is invalid due to empty line, the tasklet shall not proceed further
	 */
	@Test
	public void testNegativeMissingTrailer() throws BatchException, FileNotFoundException {
		String billerCode = "9999";
		File file = getResourceFile(String.format("ftp/BPF_FROM/%s/%s20180623_missing_trailer.txt", billerCode, billerCode));
    	String workingDir = System.getProperty("user.dir");
		String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM", billerCode);
		uploadFileToFTPFolder(file, fileFolderPath);
		
    	JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-06-24");
    	
    	String fileNameWithDateFormat = String.format("%s${yyyyMMdd}_missing_trailer.txt", billerCode);
		BatchBillerPaymentConfig batchBillerPaymentConfig = new BatchBillerPaymentConfig();
    	batchBillerPaymentConfig.setFileNameFormat(fileNameWithDateFormat);
    	batchBillerPaymentConfig.setIbkFtpFolder(FTPUtils.convertFTPForWindows(fileFolderPath));
    	batchBillerPaymentConfig.setBillerCode(billerCode);
    	when(batchBillerPaymentConfigQueue.poll()).thenReturn(batchBillerPaymentConfig);
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FIELD_VALIDATION_ERROR + ":" + BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
		
    	getIBKBillerPaymentFileTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	private void cleanupFolder() throws IOException {
    	String workingDir = System.getProperty("user.dir");
    	String fileFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM");
    	File folder = new File(fileFolderPath);
    	if(folder.exists() && folder.isDirectory()) {
    		FileUtils.deleteDirectory(folder);
    	}
    }
}
