package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.job.config.ExternalInterfacesCheckJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class, ExternalInterfacesCheckJobConfiguration.class })
@ActiveProfiles("test")
public class ExternalInterfacesCheckJobTest extends BaseFTPJobTest {

	private static final Logger logger = Logger.getLogger(ExternalInterfacesCheckJobTest.class);
	public static final String JOB_NAME="ExternalInterfacesCheckJob";
	public static final String JOB_LAUNCHER_UTILS="ExternalInterfacesCheckJobLauncherTestUtils";

	@Lazy
	@Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
	private JobLauncherTestUtils jobLauncherTestUtils;
	
	@Value("${dcp.bo.batch.inputfolder.path}")
	private String targetFileFolder;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	
	@Test
	public void testDummy() throws Exception{
		Assert.assertEquals(1, 1);
	}
	
	@Test
	public void testJob() throws Exception {
		logger.info("test executing new job");
		JobParameters jobParameters = new JobParametersBuilder()
				.addDate("now", new Date())
				.addString(BATCH_JOB_PARAMETER_JOB_NAME_KEY , JOB_NAME)
				.toJobParameters();
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
		Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
	}
	
	@Before
	public void setup() throws Exception{
		File localDownloadFolder = Paths.get(targetFileFolder, JOB_NAME).toFile();
		if(!localDownloadFolder.exists()) {
			FileUtils.forceMkdir(localDownloadFolder);
		}
		String workingDir = System.getProperty("user.dir");
		String ftpIBKFolderPath = generateFolderPath(workingDir, "target", "BPF_FROM");
		File ftpIBKFolder = new File(ftpIBKFolderPath) ;//${user.dir}/target/BPF_FROM
		if(!ftpIBKFolder.exists()) {
			FileUtils.forceMkdir(ftpIBKFolder);
		}
		
		String ftpIBKPrepaidFolderPath = generateFolderPath(workingDir, "target", "nibk_prepaid_to");
		File ftpIBKPrepaidFoler = new File(ftpIBKPrepaidFolderPath) ;//${user.dir}/target/nibk_prepaid_to
		if(!ftpIBKPrepaidFoler.exists()) {
			FileUtils.forceMkdir(ftpIBKPrepaidFoler);
		}
	}
}
