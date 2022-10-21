package com.rhbgroup.dcp.bo.batch.job.config;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;

import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseFileToDBJobConfiguration;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PremierCustomerInfoandRMCodeTaggingJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.step.PremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.PremierCustomerInfoandRMCodeTaggingJobCommitStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.PremierCustomerInfoandRMCodeTaggingJobReadFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.PremierCustomerInfoandRMCodeTaggingJobTruncateStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooterStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.PremierCustomerInfoandRMCodeTaggingJobValidatorStepBuilder;

@Configuration
@Lazy
public class PremierCustomerInfoandRMCodeTaggingJobConfiguration extends BaseFileToDBJobConfiguration {
	
    @Autowired
    private PremierCustomerInfoandRMCodeTaggingJobConfigProperties configProperties;

    @Autowired
    private PremierCustomerInfoandRMCodeTaggingJobValidateHeaderFooterStepBuilder premierCustomerInfoandRMCodeTaggingJobValidateHeaderFooterStepBuilder;
    @Autowired
    private PremierCustomerInfoandRMCodeTaggingJobTruncateStagingStepBuilder premierCustomerInfoandRMCodeTaggingJobTruncateStagingStepBuilder;
    @Autowired
    private PremierCustomerInfoandRMCodeTaggingJobReadFileToStagingStepBuilder premierCustomerInfoandRMCodeTaggingJobReadFileToStagingStepBuilder;
    @Autowired
    private PremierCustomerInfoandRMCodeTaggingJobCommitStagingStepBuilder premierCustomerInfoandRMCodeTaggingJobCommitStagingStepBuilder;
    @Autowired
    private PremierCustomerInfoandRMCodeTaggingJobValidatorStepBuilder premierCustomerInfoandRMCodeTaggingJobValidatorStepBuilder;
    @Autowired
    private PremierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundStepBuilder premierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundStepBuilder;

    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;

    @Bean(name = "PremierCustomerInfoandRMCodeTaggingJob")
    public Job BuildJob() {

        // Build simple job
        SimpleJobBuilder job = getDefaultJobBuilder("PremierCustomerInfoandRMCodeTaggingJob");

        // Check if jobexecutionid being passed or not
        Map<String, String> jobParameters = dcpBatchApplicationContext.getInitialJobArguments();

        // Commit to staging table
        if(!jobParameters.containsKey(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY))
        {
            job.next(copyFTPFileToLocal(configProperties.getFtpFolder(),configProperties.getName(),configProperties.getNameDateFormat()));
            job.next(this.premierCustomerInfoandRMCodeTaggingJobValidateHeaderFooterStepBuilder.buildStep());
            // Not to truncate the staging table currently

            job.next(this.premierCustomerInfoandRMCodeTaggingJobReadFileToStagingStepBuilder.buildStep());
            job.next(this.premierCustomerInfoandRMCodeTaggingJobCommitStagingStepBuilder.buildStep());
        }

        // Validator
        job.next(this.premierCustomerInfoandRMCodeTaggingJobValidatorStepBuilder.buildStep());
        job.next(this.premierCustomerInfoandRMCodeTaggingJobCheckCIFNotFoundStepBuilder.buildStep());

        if(!jobParameters.containsKey(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY)){
            job.next(moveFiletoSuccessOrFailedFolder());
        }

        return job.build();
    }
}
