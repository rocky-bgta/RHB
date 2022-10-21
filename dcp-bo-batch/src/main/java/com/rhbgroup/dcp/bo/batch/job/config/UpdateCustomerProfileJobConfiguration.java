package com.rhbgroup.dcp.bo.batch.job.config;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.config.properties.UpdateCustomerProfileJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.BatchLookupStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.UpdateCustomerProfileStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.UpdateCustomerProfileStepBuilder;
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
public class UpdateCustomerProfileJobConfiguration extends BaseFileToDBJobConfiguration {

    private static final Logger logger = Logger.getLogger(UpdateCustomerProfileJobConfiguration.class);

    private static final String JOBNAME = "UpdateCustomerProfileJob";
    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;
    @Autowired
    private UpdateCustomerProfileJobConfigProperties configProperties;
    @Autowired
    private UpdateCustomerProfileStagingStepBuilder updateCustomerProfileStagingStepBuilder;
    @Autowired
    private BatchLookupStepBuilder batchLookupStepBuilder;
    @Autowired
    private UpdateCustomerProfileStepBuilder updateCustomerProfileStepBuilder;

    @Bean(name = JOBNAME)
    public Job buildJob() {

        Map<String,String> initialJobArgs = dcpBatchApplicationContext.getInitialJobArguments();
        String sourceFileFolder = configProperties.getFtpfolder();
        String sourceFileName = configProperties.getName();
        String sourceFileDateFormat = configProperties.getNamedateformat();
        Step copyFtpFileStep = copyFTPFileToLocal(sourceFileFolder,sourceFileName,sourceFileDateFormat);
        Step readFileToStagingStep=this.updateCustomerProfileStagingStepBuilder.buildStep();
        Step lookupStep = this.batchLookupStepBuilder.buildStep();
        Step updateCustomerProfileStep = this.updateCustomerProfileStepBuilder.buildStep();
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
            job.next(lookupStep).on("*").to(updateCustomerProfileStep)
                .from(updateCustomerProfileStep).on("FAILED").to(finalStep)
                .from(updateCustomerProfileStep).on("*").to(finalStep)
                .end();
        }
        else {
            job.next(lookupStep).on("*").to(updateCustomerProfileStep)
                .end();
        }

        return job.build();
    }
}