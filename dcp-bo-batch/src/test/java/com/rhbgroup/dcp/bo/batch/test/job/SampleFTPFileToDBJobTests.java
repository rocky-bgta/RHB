package com.rhbgroup.dcp.bo.batch.test.job;

import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.job.config.SampleFTPFileToDBJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfig.class,SampleFTPFileToDBJobConfiguration.class})
@Ignore
public class SampleFTPFileToDBJobTests {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("SampleFTPFileToDBJobJobLauncherTestUtils")
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void testJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder().addDate("now",
                new Date()).addString("jobname","SampleFTPFileToDBJob").toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }

    @Before
    public void setup() {
        Environment environment = applicationContext.getEnvironment();
        System.out.println("=====================spring.batch.table-prefix:"+environment.getProperty("spring.batch.table-prefix"));
    }

    @After
    public void after() throws Exception {
        // clear table
        //jdbcTemplate.execute(DELETE_TABLE_SQL);
    }
}
