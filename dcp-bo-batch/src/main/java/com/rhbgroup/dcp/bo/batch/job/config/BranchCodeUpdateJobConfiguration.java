package com.rhbgroup.dcp.bo.batch.job.config;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.config.properties.BranchCodeUpdateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.BranchCodeUpdateStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.BranchCodeUpdateStepBuilder;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseFileToDBJobConfiguration;
import org.springframework.util.StringUtils;

import java.util.Map;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;

@Configuration
@Lazy
public class BranchCodeUpdateJobConfiguration extends BaseFileToDBJobConfiguration {

    private static final Logger logger = Logger.getLogger(BranchCodeUpdateJobConfiguration.class);

    private static final String JOBNAME = "BranchCodeUpdateJob";
    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;
    @Autowired
    private BranchCodeUpdateJobConfigProperties configProperties;
    @Autowired
    private BranchCodeUpdateStagingStepBuilder branchCodeUpdateStagingStepBuilder;
    @Autowired
    private BranchCodeUpdateStepBuilder branchCodeUpdateStepBuilder;

    @Bean(name = JOBNAME)
    public Job buildJob() {

        Map<String,String> initialJobArgs = dcpBatchApplicationContext.getInitialJobArguments();
        String sourceFileFolder = configProperties.getFtpfolder();
        String sourceFileName = configProperties.getName();
        String sourceFileDateFormat = configProperties.getNamedateformat();
        Step copyFtpFileStep = copyFTPFileToLocal(sourceFileFolder,sourceFileName,sourceFileDateFormat);
        Step readFileToStagingStep=this.branchCodeUpdateStagingStepBuilder.buildStep();
        Step branchCodeUpdateStep = this.branchCodeUpdateStepBuilder.buildStep();
        Step finalStep=moveFiletoSuccessOrFailedFolder();
        SimpleJobBuilder job = getDefaultFileToDBJobBuilder(JOBNAME);
        String jobexecutionid="";

        if(null !=initialJobArgs) {
            jobexecutionid = initialJobArgs.get(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY);
        }

        if(StringUtils.isEmpty(jobexecutionid)) {
            logger.info(String.format ("%s Job id is not set in job parameter, download FTP file and load into staging table", JOBNAME));
            job.next(copyFtpFileStep);
            job.next(readFileToStagingStep);
            job.next(branchCodeUpdateStep).on("FAILED").to(finalStep)
                    .from(branchCodeUpdateStep).on("*").to(finalStep)
                    .end();

        }
        else {
            job.next(branchCodeUpdateStep);
        }

        return job.build();
    }
}