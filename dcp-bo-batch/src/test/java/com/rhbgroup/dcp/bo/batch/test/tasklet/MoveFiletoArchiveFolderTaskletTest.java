package com.rhbgroup.dcp.bo.batch.test.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.MoveFiletoArchiveFolderTasklet;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class MoveFiletoArchiveFolderTaskletTest extends BaseJobTest {
	
	private static final String WORKING_DIR = System.getProperty("user.dir");
	
	private static final String FILE_FOLDER = "dcp_mass_notification_from";
	
	private static final String ARCHIVE_FOLDER = "dcp_mass_notification_from_archive";
	
	private static final String PARAMETER_DATE = "20181210";
	
	private static final String BATCH_EXECUTION_CONTEXT_INPUT_FILE_FULL_PATH = "inputFolderFullPath";
	
	@Autowired
	private MoveFiletoArchiveFolderTasklet moveFiletoArchiveFolderTasklet;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
	private String sourceFilePath;
	
	@Before
	public void beforeLocalTest() throws IOException {	
		super.beforeTest();
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
    	
    	File file = getResourceFile("batch/input/" + FILE_FOLDER + "/DCP_LDCPA6005T_20181206.txt");
		String fileFolderPath = generateFolderPath(WORKING_DIR, "target", "batch", "input", FILE_FOLDER);
		String fileName = (file.getName()).replace("20181206", PARAMETER_DATE);
		File inputFile = Paths.get(fileFolderPath, fileName).toFile();
		FileUtils.copyFile(file, inputFile);
		
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BATCH_EXECUTION_CONTEXT_INPUT_FILE_FULL_PATH)).thenReturn(true);
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_EXECUTION_CONTEXT_INPUT_FILE_FULL_PATH)).thenReturn(inputFile.getAbsolutePath());
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getAllFailureExceptions()).thenReturn(java.util.Collections.emptyList());
    	when(mockChunkContext.getStepContext().getStepExecution().getJobExecution().getStatus()).thenReturn(BatchStatus.COMPLETED);
    	
    	assertEquals(RepeatStatus.FINISHED, moveFiletoArchiveFolderTasklet.execute(mockStepContribution, mockChunkContext));
    	assertFalse(inputFile.exists());
    	
    	File successFilePath = Paths.get(sourceFilePath, ARCHIVE_FOLDER, inputFile.getName()).toFile();
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
		
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	
    	//expectedEx.expect(BatchException.class);
    	//expectedEx.expectMessage(BatchErrorCode.GENERIC_SYSTEM_ERROR + ":" + BatchErrorCode.GENERIC_SYSTEM_ERROR_MESSAGE);
    	
    	moveFiletoArchiveFolderTasklet.execute(mockStepContribution, mockChunkContext);
    	
    	verify(mockAppender, atLeastOnce()).doAppend((LoggingEvent) captorLoggingEvent.capture());
    	LoggingEvent loggingEvent = (LoggingEvent) captorLoggingEvent.getAllValues().get(1);
    	assertEquals("Skip archive file due to file not found in context", loggingEvent.getMessage());
	}

	private void cleanupFolder() throws IOException {
		File inputFolder = new File(generateFolderPath(WORKING_DIR, "target", "batch", "input", FILE_FOLDER));
    	File archiveFolder = new File(generateFolderPath(WORKING_DIR, "target", "batch", "input", ARCHIVE_FOLDER));
    	deleteFolderIfExists(inputFolder);
    	deleteFolderIfExists(archiveFolder);
    }
	
	private void deleteFolderIfExists(File folder) throws IOException {
		if(folder.exists() && folder.isDirectory()) {
			FileUtils.deleteDirectory(folder);
    	}
	}
}
