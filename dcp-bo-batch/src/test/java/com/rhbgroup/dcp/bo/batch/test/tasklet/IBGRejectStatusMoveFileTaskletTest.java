package com.rhbgroup.dcp.bo.batch.test.tasklet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.IBGRejectStatusMoveFileTasklet;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {IBGRejectStatusMoveFileTasklet.class})
@ActiveProfiles("test")
public class IBGRejectStatusMoveFileTaskletTest {
	@Mock
	ChunkContext chunkContext;
	
	@Mock
	StepContext stepContext;
	
	@Mock
	StepExecution stepExecution;
	
	@Mock
	JobExecution jobExecution;
	
	@Mock
	ExecutionContext executionContext;
	
	@Mock
	StepContribution stepContribution;
	
	@MockBean(name="batchParameterImpl")
	BatchParameterRepositoryImpl mockBatchParameterImpl;
	
	@Autowired
	IBGRejectStatusMoveFileTasklet moveFileTasklet;
	
	@Test(expected=Exception.class)
	public void testException() throws Exception{
		moveFileTasklet.execute(stepContribution, chunkContext);
	}

}
