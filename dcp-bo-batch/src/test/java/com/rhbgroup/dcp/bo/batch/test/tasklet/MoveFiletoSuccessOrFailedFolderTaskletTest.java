package com.rhbgroup.dcp.bo.batch.test.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_FAILED_DIRECTORY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_SUCCESS_DIRECTORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
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

import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.MoveFiletoSuccessOrFailedFolderTasklet;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class MoveFiletoSuccessOrFailedFolderTaskletTest extends BaseJobTest {
	
	@Autowired
	private MoveFiletoSuccessOrFailedFolderTasklet moveFiletoSuccessOrFailedFolderTasklet;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Value(BATCH_SYSTEM_FOLDER_SUCCESS_DIRECTORY)
    private String successFolderPath;
    
    @Value(BATCH_SYSTEM_FOLDER_FAILED_DIRECTORY)
    private String failedFolderPath;
	
	@Before
	public void beforeLocalTest() throws IOException {	
		super.beforeTest();
		cleanupFolder();
	}
	
	@After
	public void afterLocalTest() throws IOException {
		cleanupFolder();
	}
	
	/*
	 * Test to ensure if no error happened before the file should be store to the success folder accordingly
	 */
	@Test
	public void testPositiveMoveToSuccessFolder() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	String workingDir = System.getProperty("user.dir");
		File file = getResourceFile("batch/input/success.txt");
		String fileFolderPath = generateFolderPath(workingDir, "target", "batch", "input", TEST_JOB);
		File inputFile = Paths.get(fileFolderPath, file.getName()).toFile();
		FileUtils.copyFile(file, inputFile);
		
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(true);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(inputFile.getAbsolutePath());
    	
    	assertEquals(RepeatStatus.FINISHED, moveFiletoSuccessOrFailedFolderTasklet.execute(mockStepContribution, mockChunkContext));
    	assertFalse(inputFile.exists());
    	
    	File successFilePath = Paths.get(successFolderPath, TEST_JOB, inputFile.getName()).toFile();
    	assertTrue(successFilePath.exists());
	}
	
	/*
	 * Test to ensure in case any error found in the context, the input file shall be copy over to the failed folder
	 */
	@Test
	public void testPositiveMoveToFailedFolder() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	String workingDir = System.getProperty("user.dir");
		File file = getResourceFile("batch/input/success.txt");
		String fileFolderPath = generateFolderPath(workingDir, "target", "batch", "input", TEST_JOB);
		File inputFile = Paths.get(fileFolderPath, file.getName()).toFile();
		FileUtils.copyFile(file, inputFile);
		
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(true);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(inputFile.getAbsolutePath());
    	
    	List<Throwable> failureExceptions = new ArrayList<>();
    	failureExceptions.add(new Exception("Dummy exception"));
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getAllFailureExceptions()).thenReturn(failureExceptions);
    	
    	assertEquals(RepeatStatus.FINISHED, moveFiletoSuccessOrFailedFolderTasklet.execute(mockStepContribution, mockChunkContext));
    	assertFalse(inputFile.exists());
    	
    	File successFilePath = Paths.get(failedFolderPath, TEST_JOB, inputFile.getName()).toFile();
    	assertTrue(successFilePath.exists());
	}
		
	/*
	 * Test to ensure if the input file missing, the tasklet shall not proceed further
	 */
	@Test
	public void testNegativeInputFileNotFound() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	String workingDir = System.getProperty("user.dir");
    	File inputFile = Paths.get(workingDir, "target", "batch", "input", TEST_JOB, "dummy.txt").toFile();
		
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(true);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(inputFile.getAbsolutePath());
    	
    	expectedEx.expect(BatchException.class);
    	expectedEx.expectMessage(BatchErrorCode.GENERIC_SYSTEM_ERROR + ":" + BatchErrorCode.GENERIC_SYSTEM_ERROR_MESSAGE);
    	
    	moveFiletoSuccessOrFailedFolderTasklet.execute(mockStepContribution, mockChunkContext);
	}
	
	/*
	 * Test to ensure if no input file path found in context, just act like do nothing
	 */
	@Test
	public void testNegativeNoInputFilePathInContext() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(false);
    	
    	assertEquals(RepeatStatus.FINISHED, moveFiletoSuccessOrFailedFolderTasklet.execute(mockStepContribution, mockChunkContext));
	}

	private void cleanupFolder() throws IOException {
		String workingDir = System.getProperty("user.dir");
		File inputFolder = new File(generateFolderPath(workingDir, "target", "batch", "input", TEST_JOB));
    	File successFolder = new File(successFolderPath);
    	File failedFolder = new File(failedFolderPath);
    	deleteFolderIfExists(inputFolder);
    	deleteFolderIfExists(successFolder);
    	deleteFolderIfExists(failedFolder);
    }
	
	private void deleteFolderIfExists(File folder) throws IOException {
		if(folder.exists() && folder.isDirectory()) {
			FileUtils.deleteDirectory(folder);
    	}
	}
}
