package com.rhbgroup.dcp.bo.batch.test.job;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.RunReportWithDateRangeJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import freemarker.template.Configuration;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class, RunReportWithDateRangeJobConfiguration.class})
@ActiveProfiles("test")
public class RunReportWithDateRangeJobTests extends BaseFTPJobTest {

	public static final String JOB_NAME = "RunReportWithDateRangeJob";
	public static final String JOB_LAUNCHER_UTILS = "RunReportWithDateRangeJobLauncherTestUtils";
   
    @Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    DcpBatchApplicationContext dcpBatchApplicationContext;
    Map<String,String> initialArguments;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    @Test
    public void testJob() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
        			.addDate("now",new Date())
        			.addString("jobname",JOB_NAME)
        			.addString("reportid", "DMBUM031")
                    //.addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY,initialArguments.get(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY))
                    .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

    }
    

    @Test
    public void testJobWithException() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
        			.addDate("now",new Date())
        			.addString("jobname",JOB_NAME)
        			.addString("reportid", "UNKNOWN")
                    .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
        for(Throwable ex : jobExecution.getFailureExceptions()) {
        	if(ex instanceof BatchException) {
            	Assert.assertEquals(BatchErrorCode.JASPER_CLIENT_ERROR, ((BatchException)ex).getDcpStatusCode());
        	}
        }
    }
    

    @Test
    public void testJobWithNoConfig() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
        			.addDate("now",new Date())
        			.addString("jobname",JOB_NAME)
        			.addString("reportid", "NEVER_FOUND")
                    .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("FAILED", jobExecution.getExitStatus().getExitCode());
        for(Throwable ex : jobExecution.getFailureExceptions()) {
        	if(ex instanceof BatchException) {
            	Assert.assertEquals("Missing report config", ((BatchException)ex).getDcpStatusDescription());
        	}
        }
    }

    @Before
    public void setup() throws Exception{
/*
        String jobProcessDate = "2018-08-15";
        initialArguments=new HashMap<String,String>();
        if(!jobProcessDate.isEmpty())
            initialArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY,jobProcessDate);
        dcpBatchApplicationContext.setInitialJobArguments(initialArguments);
*/

        String workingDir = System.getProperty("user.dir");
        FTPUtils.createFTPFolderIfNotExists(generateFolderPath(workingDir, "target", "DCP_JOMPAY_ONUS_SUCCESS_PAYMENT_MONTHLY"), defaultFTPConfigProperties);
    }

}
