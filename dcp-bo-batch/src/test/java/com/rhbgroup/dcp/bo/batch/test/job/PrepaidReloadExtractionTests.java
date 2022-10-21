package com.rhbgroup.dcp.bo.batch.test.job;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.PrepaidReloadExtractionJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class, PrepaidReloadExtractionJobConfiguration.class})
@ActiveProfiles("test")
public class PrepaidReloadExtractionTests extends BaseFTPJobTest {

    @Autowired
    private ApplicationContext applicationContext;
    @Lazy
    @Autowired
    @Qualifier("PrepaidReloadExtractionJobJobLauncherTestUtils")
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private FTPConfigProperties ftpConfigProperties;
    @Autowired
    DcpBatchApplicationContext dcpBatchApplicationContext;
    private Map<String,String> initialArguments;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    @Test
    public void testJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("now",new Date())
                .addString("jobname","PrepaidReloadExtractionJob")
                .addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, initialArguments.get(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY))
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }

    @Before
    public void setup() throws Exception{

        // Set processing date to retrieve file/Use batchSystemDate if empty. Example: "2018-08-29"
        String jobProcessDate = "2018-10-02";

        // Initialize dcpBatchApplicationContext for partial failure reprocessing
        initialArguments=new HashMap<String,String>();
        if(!jobProcessDate.isEmpty())
            initialArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY,jobProcessDate);
        dcpBatchApplicationContext.setInitialJobArguments(initialArguments);

        String workingDir = System.getProperty("user.dir");
        FTPUtils.createFTPFolderIfNotExists(generateFolderPath(workingDir, "target", "DCP_PREPAID_FROM"), ftpConfigProperties);
    }
}
