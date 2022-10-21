package com.rhbgroup.dcp.bo.batch.test.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.util.ResourceUtils;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.AuditJMSConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.SmsJMSConfigProperties;

@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class BaseJobTest {
	
	protected static final String TEST_JOB = "TestJob";
	protected static final String TEST_STEP = "TestStep";
	protected static final String TEST_TASKLET = "TestTasklet";
	private static final long TEST_JOB_INSTANCE_ID = 1L;
	protected static final long TEST_JOB_EXECUTION_ID = 1L;
	
	protected static final String BATCH_READ_COUNT_KEY = "batch.read.count";
	protected static final String BATCH_WRITE_COUNT_KEY = "batch.write.count";
	protected static final String BATCH_SKIP_COUNT_KEY = "batch.skip.count";
	protected static final String BATCH_COMMIT_COUNT_KEY = "batch.commit.count";
	
	protected static final String JOB_COMPLETED = "COMPLETED";
	protected static final String JOB_FAILED = "FAILED";
	
	@Autowired
	protected JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Lazy
	protected SmsJMSConfigProperties smsJMSConfigProperties;
	
	@Autowired
	@Lazy
	protected AuditJMSConfigProperties auditJMSConfigProperties;
	
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Mock
	protected Appender mockAppender;
	
	@Captor
	protected ArgumentCaptor<?> captorLoggingEvent;
	
	@Before
	public void beforeTest() {	
		LogManager.getRootLogger().addAppender(mockAppender);
	}
	
	protected File getResourceFile(String resourcePath) throws FileNotFoundException {
		return ResourceUtils.getFile(this.getClass().getClassLoader().getResource(resourcePath));
	}
		
	protected String generateFolderPath(String... sources) {
		StringBuilder strBuilder = new StringBuilder();
		for(String source : sources) {
			strBuilder.append(source).append(File.separator);
		}
		return strBuilder.toString();
	}
	
	protected StepContribution createMockStepContribution() {
    	StepContribution mockStepContribution = Mockito.mock(StepContribution.class);
    	
    	return mockStepContribution;
    }
	
	protected ChunkContext createMockChunkContext(JobParameters mockJobParameters) {
    	ChunkContext mockChunkContext = Mockito.mock(ChunkContext.class);
    	StepContext mockStepContext = Mockito.mock(StepContext.class);
    	StepExecution mockStepExecution = Mockito.mock(StepExecution.class);
        JobExecution mockJobExecution = Mockito.mock(JobExecution.class);
        ExecutionContext mockExecutionContext = Mockito.mock(ExecutionContext.class);

        Mockito.when(mockChunkContext.getStepContext()).thenReturn(mockStepContext);
        Mockito.when(mockStepContext.getStepExecution()).thenReturn(mockStepExecution);
        Mockito.when(mockStepExecution.getJobExecution()).thenReturn(mockJobExecution);
        Mockito.when(mockStepExecution.getExecutionContext()).thenReturn(mockExecutionContext);
        Mockito.when(mockJobExecution.getExecutionContext()).thenReturn(mockExecutionContext);
        Mockito.when(mockJobExecution.getJobParameters()).thenReturn(mockJobParameters);

        return mockChunkContext;
    }
	
	protected StepExecution createStepExecution(String stepName, Map<String, Object> jobParamEntryMap, Map<String, Object> executionContextMap) {
		JobParametersBuilder jobParamEntryBuilder = new JobParametersBuilder();
		
		if(jobParamEntryMap != null) {
			for(Entry<String, Object> jobParamEntry : jobParamEntryMap.entrySet()) {
				if(jobParamEntry.getValue() instanceof Long) {
					jobParamEntryBuilder.addLong(jobParamEntry.getKey(), (long)jobParamEntry.getValue());
				} else if(jobParamEntry.getValue() instanceof Date) {
					jobParamEntryBuilder.addDouble(jobParamEntry.getKey(), (double)jobParamEntry.getValue());
				} else if(jobParamEntry.getValue() instanceof Date) {
					jobParamEntryBuilder.addDate(jobParamEntry.getKey(), (Date)jobParamEntry.getValue());
				} else if(jobParamEntry.getValue() instanceof String) {
					jobParamEntryBuilder.addString(jobParamEntry.getKey(), (String)jobParamEntry.getValue());
				}
			}
		}
		
		JobParameters jobParamEntrys = jobParamEntryBuilder.toJobParameters();
		JobInstance jobInstance = new JobInstance(TEST_JOB_INSTANCE_ID, TEST_JOB);
		JobExecution jobExecution = new JobExecution(jobInstance, jobParamEntrys);
		jobExecution.setId(TEST_JOB_EXECUTION_ID);
		
		ExecutionContext executionContext = new ExecutionContext();
		if(executionContextMap != null) {
			for(Entry<String, Object> executionContextEntry : executionContextMap.entrySet()) {
				if(executionContextEntry.getValue() instanceof Integer) {
					executionContext.putInt(executionContextEntry.getKey(), (int)executionContextEntry.getValue());
				} else if(executionContextEntry.getValue() instanceof Long) {
					executionContext.putLong(executionContextEntry.getKey(), (long)executionContextEntry.getValue());
				} else if(executionContextEntry.getValue() instanceof Date) {
					executionContext.putDouble(executionContextEntry.getKey(), (double)executionContextEntry.getValue());
				} else if(executionContextEntry.getValue() instanceof String) {
					executionContext.putString(executionContextEntry.getKey(), (String)executionContextEntry.getValue());
				}
			}
		}
		jobExecution.setExecutionContext(executionContext);
		
		return new StepExecution(stepName, jobExecution);
	}
	
	// This method simply execute the reader and count how many record it had read
	protected int executeSimpleItemReader(StepExecution stepExecution, ItemReader<?> itemReader) throws Exception {
		return StepScopeTestUtils.doInStepScope(stepExecution, new Callable<Integer>() {
			public Integer call() throws Exception {
				int count = 0;
				while (itemReader.read() != null) {
					count++;
				}
				return count;
			}
		});
	}
}
