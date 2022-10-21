package com.rhbgroup.dcp.bo.batch.test.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

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

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.CopyOutputFolderFileToFTPTasklet;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class CopyOutputFolderFileToFTPTaskletTest extends BaseFTPJobTest {

	@Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
	
	@Autowired
	private CopyOutputFolderFileToFTPTasklet copyOutputFolderFileToFTPTasklet;
	
	@Autowired
	private FTPConfigProperties ftpConfigProperties;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Before
	public void beforeLocalTest() throws IOException {	
		super.beforeFTPTest();
		cleanupFolder();
	}
	
	@After
	public void afterLocalTest() throws Exception {
		super.afterFTPTest();
		cleanupFolder();
	}
	
	/*
	 * Test to ensure if no error happened before the file should be store to the success folder accordingly
	 */
//	@Test
	public void testPositiveMoveToSuccessFolder() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	// Copy the source file to the output folder first
    	String workingDir = System.getProperty("user.dir");
		File file = getResourceFile("batch/output/output_ftp.txt");
		String fileFolderPath = generateFolderPath(workingDir, "target", "batch", "output", TEST_JOB);
		File outputFile = Paths.get(fileFolderPath, file.getName()).toFile();
		FileUtils.copyFile(file, outputFile);
		
		// Create the target FTP folder
		String targetFTPFolder = generateFolderPath(workingDir, "target", "FTP", TEST_JOB);
		FTPUtils.createFTPFolderIfNotExists(targetFTPFolder, ftpConfigProperties);
		String targetFileName = "output_{#date}";
		String targetFileDateFormat = "yyyyMMdd";
		
		copyOutputFolderFileToFTPTasklet.init(targetFTPFolder, targetFileName, targetFileDateFormat);
		copyOutputFolderFileToFTPTasklet.initFTPConfig(ftpConfigProperties);
		
		when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY)).thenReturn(true);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY)).thenReturn(outputFile.getAbsolutePath());
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-08-31");
    	
    	assertEquals(RepeatStatus.FINISHED, copyOutputFolderFileToFTPTasklet.execute(mockStepContribution, mockChunkContext));
    	assertTrue(outputFile.exists());
	}
	
	/*
	 * Test to ensure if no error happened if there is no file to move in context
	 */
//	@Test
	public void testPositiveNoFileToMoveInContext() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
		when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY)).thenReturn(false);
    	
    	assertEquals(RepeatStatus.FINISHED, copyOutputFolderFileToFTPTasklet.execute(mockStepContribution, mockChunkContext));
    	
    	verify(mockAppender, times(2)).doAppend((LoggingEvent)captorLoggingEvent.capture());
    	LoggingEvent loggingEvent = (LoggingEvent)captorLoggingEvent.getAllValues().get(1);
        //Check log level
        assertEquals(Level.INFO, loggingEvent.getLevel());
        //Check the message being logged
        assertTrue(loggingEvent.getRenderedMessage().contains("No output file detected in the context, finishing the tasklet "));
	}
	
	/*
	 * Test to ensure if no error happened if there is no file to move in context
	 */
	@Test
	public void testNegativeFTPServiceDown() throws Exception {
		shutdownSSHServer();
		
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	// Copy the source file to the output folder first
    	String workingDir = System.getProperty("user.dir");
		File file = getResourceFile("batch/output/output_ftp.txt");
		String fileFolderPath = generateFolderPath(workingDir, "target", "batch", "output", TEST_JOB);
		File outputFile = Paths.get(fileFolderPath, file.getName()).toFile();
		FileUtils.copyFile(file, outputFile);
		
		// Create the target FTP folder
		String targetFTPFolder = generateFolderPath(workingDir, "target", "FTP", TEST_JOB);
		String targetFileName = "output_{#date}";
		String targetFileDateFormat = "yyyyMMdd";

		copyOutputFolderFileToFTPTasklet.init(targetFTPFolder, targetFileName, targetFileDateFormat);
		copyOutputFolderFileToFTPTasklet.initFTPConfig(ftpConfigProperties);
		
		when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY)).thenReturn(true);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY)).thenReturn(outputFile.getAbsolutePath());
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-08-31");
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn("2018-08-31");

    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.FTP_SYSTEM_ERROR + ":" + BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE);
    	
    	copyOutputFolderFileToFTPTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	private void cleanupFolder() throws IOException {
    	String workingDir = System.getProperty("user.dir");
    	String fileFolderPath = generateFolderPath(workingDir, "target", "batch", "output", TEST_JOB);
    	File folder = new File(fileFolderPath);
    	if(folder.exists() && folder.isDirectory()) {
    		FileUtils.deleteDirectory(folder);
    	}
    }
}
