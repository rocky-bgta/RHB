package com.rhbgroup.dcp.bo.batch.test.job;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.Map;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.junit.Assert;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.InitialBatchConfigJobConfiguration;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class, InitialBatchConfigJobConfiguration.class})
@ActiveProfiles("test")
public class InitialDCPBatchConfigJobTests extends BaseJobTest {

    @Autowired
    @Qualifier("InitialBatchConfigJobJobLauncherTestUtils")
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    public JdbcTemplate jdbcTemplate;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    @Test
    public void testJob() throws Exception {

        Date date=new Date();
        String dateString=DateUtils.formatDateString(date, DEFAULT_DATE_FORMAT);
        String batchConfigBatchSystemDateSql="SELECT TOP 1 * FROM TBL_BATCH_CONFIG WHERE PARAMETER_KEY=?";
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("now", date)
                .addString("jobname", "InitialBatchConfigJob")
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

        Map<String,Object> batchSystemDateRow = jdbcTemplate.queryForMap(batchConfigBatchSystemDateSql
               , new Object[] { BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY});
        if(batchSystemDateRow!=null) {
            Assert.assertEquals(dateString,batchSystemDateRow.get("PARAMETER_VALUE"));
        }
        else {
            fail(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY+" not found in TBL_BATCH_CONFIG");
        }

        int isRequiredToExecFeletedOrInactiveBillerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE IS_REQUIRED_TO_EXECUTE='0'"
                , null, Integer.class);
        int isRequiredToExecActiveBillerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM TBL_BATCH_BILLER_PAYMENT_CONFIG WHERE IS_REQUIRED_TO_EXECUTE='1'"
                , null, Integer.class);

        Assert.assertEquals(3,isRequiredToExecActiveBillerCount);
        Assert.assertEquals(2,isRequiredToExecFeletedOrInactiveBillerCount);
    }
}
