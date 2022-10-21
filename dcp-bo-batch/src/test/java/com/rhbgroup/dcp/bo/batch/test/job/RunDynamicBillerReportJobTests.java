package com.rhbgroup.dcp.bo.batch.test.job;

import com.jaspersoft.jasperserver.dto.importexport.State;
import com.jaspersoft.jasperserver.dto.serverinfo.ServerInfo;
import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jaspersoft.jasperserver.jaxrs.client.core.operationresult.OperationResult;
import com.rhbgroup.dcp.bo.batch.framework.common.tasklet.RunReportTasklet;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseRunReportJobConfiguration;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.jasper.JasperClientConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.utils.JasperClientUtils;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_EXTERNAL_SYSTEM_DATE;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_BILLER_CODE;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_UNIT_URI;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class, RunDynamicBillerReportJobTests.Config.class})
@ActiveProfiles("test")
public class RunDynamicBillerReportJobTests extends BaseFTPJobTest {

    private static final Logger logger = Logger.getLogger(RunDynamicBillerReportJobTests.class);

    public static final String JOB_NAME = "RunReportJob";
    public static final String JOB_LAUNCHER_UTILS = "RunReportJobLauncherTestUtils";

    @Autowired
    private JasperClientConfigProperties jasperClientConfigProperties;

    @TestConfiguration
    static class Config extends BaseRunReportJobConfiguration {
        @Autowired
        private RunReportTasklet runReportTasklet;

        @Bean
        @Lazy
        @Qualifier(LoadIBKBillerDynamicPaymentJobTests.JOB_LAUNCHER_UTILS)
        public JobLauncherTestUtils getLoadIBKBillerDynamicPaymentJobLauncherTestUtils() {
            return new JobLauncherTestUtils() {
                @Override
                @Autowired
                public void setJob(@Qualifier(LoadIBKBillerDynamicPaymentJobTests.JOB_NAME) Job job) {
                    super.setJob(job);
                }
            };
        }

        private static final String JOB_NAME = "RunReportJob";

        @Bean(name = JOB_NAME)
        public Job BuildJob() {
            SimpleJobBuilder jobBuilder = getDefaultRunReportJobBuilder(JOB_NAME)
                    .next(injectBillerCode())
                    .next(runReport());
            return jobBuilder.build();
        }

        protected Step runReport() {
            return getStepBuilderFactory().get("runReport")
                    .tasklet(this.runReportTasklet)
                    .listener(this.batchJobCommonStepListener)
                    .build();
        }

        protected Step injectBillerCode() {
            return getStepBuilderFactory().get("step1").tasklet((stepContribution, chunkContext) -> {
                chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(REPORT_UNIT_URI, "/reports/DEV/Financial/DMBUD999/daily_successful_bill_ver21a");
                chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put(REPORT_BILLER_CODE, "5052");
                return RepeatStatus.FINISHED;
            }).listener(this.batchJobCommonStepListener).build();
        }

    }

    @Autowired
    @Qualifier(JOB_LAUNCHER_UTILS)
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void testDynamicBillerJob() throws Exception {

        importResourceToServer();
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("now", new Date())
                .addString("jobname", JOB_NAME)
                .addString("reportid", "DMBUD999_5052")
                .addString(BATCH_JOB_PARAMETER_EXTERNAL_SYSTEM_DATE, "2021-11-19")
                .toJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        Assert.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

    }

    public void importResourceToServer() throws BatchException, FileNotFoundException, InterruptedException {
        logger.info(String.format("Initiating session to Jasper Server.. [%s]", jasperClientConfigProperties.getUrl()));
        Session session = JasperClientUtils.initSession(jasperClientConfigProperties);
        logger.info("Session initiated. Getting server info..");
        ServerInfo serverInfo = JasperClientUtils.getServerInfo(session);
        logger.info(String.format("Connected to Jasper Server [version:%s, edition:%s]",serverInfo.getVersion(),serverInfo.getEdition()));
        File file = getResourceFile("jasperimport/daily_successful_bill_ver21a.zip");
        State state  = session
                .importService().newTask().create(file)
                .getEntity();
        logger.info(state);
        do{
            Thread.sleep(1000l);
            OperationResult<State> state2  = session
                    .importService().task(state.getId()).state();
            state = state2.getEntity();
        }while (state.getPhase().equalsIgnoreCase("inprogress"));
        logger.info(state);
        assertEquals("finished", state.getPhase());
    }

}
