package com.rhbgroup.dcp.bo.batch.test.job;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.config.SnapshotBoUsersGroupJobConfiguration;
import com.rhbgroup.dcp.bo.batch.job.repository.SnapshotBoUsersGroupRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class, SnapshotBoUsersGroupJobConfiguration.class})
@ActiveProfiles("test")
public class SnapshotBoUsersGroupJobTests extends BaseJobTest {

    public static final String JOB_NAME = "SnapshotBoUsersGroupJob";
    public static final String JOB_LAUNCHER_UTILS = "SnapshotBoUsersGroupJobLauncherTestUtils";
    @Autowired
    private ApplicationContext applicationContext;
    @Lazy
    @Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private FTPConfigProperties ftpConfigProperties;
    @Autowired
    DcpBatchApplicationContext dcpBatchApplicationContext;
    @Autowired
    private SnapshotBoUsersGroupRepositoryImpl snapshotBoUsersGroupRepository;
    private Map<String,String> initialArguments;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    @Before
    public void setup() throws Exception{

        // Set processing date to retrieve file/Use batchSystemDate if empty. Example: "2018-08-29"
        String jobProcessDate = "";

        // Initialize dcpBatchApplicationContext for partial failure reprocessing
        initialArguments=new HashMap<String,String>();
        if(!jobProcessDate.isEmpty())
            initialArguments.put(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY,jobProcessDate);
        dcpBatchApplicationContext.setInitialJobArguments(initialArguments);
    }

    @Test
    public void testJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("now",new Date())
                .addString("jobname","JOB_NAME")
                .addString(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY, initialArguments.get(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY))
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }
}
