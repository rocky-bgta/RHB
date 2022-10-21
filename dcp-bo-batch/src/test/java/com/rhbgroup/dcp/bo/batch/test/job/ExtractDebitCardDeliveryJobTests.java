package com.rhbgroup.dcp.bo.batch.test.job;

import java.util.Date;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.After;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.ExtractDebitCardDeliveryJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfig.class, ExtractDebitCardDeliveryJobConfiguration.class})
@ActiveProfiles("test")
public class ExtractDebitCardDeliveryJobTests extends BaseFTPJobTest {

    private static final Logger logger = Logger.getLogger(ExtractDebitCardDeliveryJobTests.class);
    public static final String JOB_NAME = "ExtractDebitCardDeliveryJob";
    public static final String JOB_LAUNCHER_UTILS = "ExtractDebitCardDeliveryJobLauncherTestUtils";

    @Lazy
    @Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private FTPConfigProperties ftpConfigProperties;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    //@Test
    public void testJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("now", new Date())
                .addString("jobname", JOB_NAME)
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }

    @Before
    public void setup() throws Exception{

        Environment environment = applicationContext.getEnvironment();
        System.out.println("=====================spring.batch.table-prefix:"+environment.getProperty("spring.batch.table-prefix"));

        String workingDir = System.getProperty("user.dir");
        FTPUtils.createFTPFolderIfNotExists(generateFolderPath(workingDir, "target", "DCP_CISUPP_FROM"), ftpConfigProperties);
    }

    @After
    public void after() throws Exception {

    }
}
