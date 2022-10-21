package com.rhbgroup.dcp.bo.batch.test.tasklet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.step.tasklet.JompayEmatchingReportFtpTasklet;
import org.springframework.batch.repeat.RepeatStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {JompayEmatchingReportFtpTasklet.class})
@ActiveProfiles("test")
public class JompayEmatchingReportFtpTaskletTest {

	@Mock
	ChunkContext chunkContext;
	
	@Mock
	StepContribution stepContribution;
	
	@Value("${job.jompayematchingreportjob.ftpfolder}")
	private String ftpTargetFolder;
	
	@MockBean(name="ftpConfigProperties")
	private FTPConfigProperties ftpConfig;
	
	@Autowired
	JompayEmatchingReportFtpTasklet jompayFtpTasklet;
	
	@Test(expected = BatchException.class)
	public void testException() throws Exception{
		System.out.println("test exception");
		jompayFtpTasklet.execute(stepContribution, chunkContext);
	}
	
	@Before
	public void setup() {
		
	}
	
	@After
	public void cleanup() {
		
	}
	
}
