package com.rhbgroup.dcp.bo.batch.test.tasklet;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.job.repository.SnapshotBoUsersGroupRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.DeleteDuplicateSnapshotBoUsersGroupTasklet;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { DeleteDuplicateSnapshotBoUsersGroupTasklet.class })
@ActiveProfiles("test")
public class DeleteDuplicateSnapshotBoUsersGroupTaskletTest extends BaseJobTest {


	@Autowired
	private DeleteDuplicateSnapshotBoUsersGroupTasklet deleteDuplicateSnapshotBoUsersGroupTasklet;
	
	@MockBean
	private SnapshotBoUsersGroupRepositoryImpl snapshotBoUsersGroupRepository;
	
	@MockBean
	private JdbcTemplate mockJdbcTemplate;
	
	// Positive test to delete records in the database
	@Test
	public void testPositiveDelete() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(mockChunkContext.getStepContext().getStepExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)).thenReturn(false);
		when(snapshotBoUsersGroupRepository.deleteUserGroupSameDayRecords(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(true);
		assertTrue(snapshotBoUsersGroupRepository.deleteUserGroupSameDayRecords(1, new Date()));
    	assertEquals(RepeatStatus.FINISHED, deleteDuplicateSnapshotBoUsersGroupTasklet.execute(mockStepContribution, mockChunkContext));
	}

	// Negative test to delete records in the database
	@Test
	public void testNegativeDelete() throws Exception {
		JobParameters mockJobParameters = Mockito.mock(JobParameters.class);
    	ChunkContext mockChunkContext = createMockChunkContext(mockJobParameters);
    	StepContribution mockStepContribution = createMockStepContribution();
    	
    	when(mockJobParameters.getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY)).thenReturn(TEST_JOB);
    	when(snapshotBoUsersGroupRepository.deleteUserGroupSameDayRecords(Mockito.anyInt(), Mockito.any(Date.class))).thenReturn(false);
		assertFalse(snapshotBoUsersGroupRepository.deleteUserGroupSameDayRecords(Mockito.anyInt(), Mockito.any(Date.class)));
    	assertEquals(RepeatStatus.FINISHED, deleteDuplicateSnapshotBoUsersGroupTasklet.execute(mockStepContribution, mockChunkContext));
	}
}
