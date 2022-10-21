package com.rhbgroup.dcp.bo.batch.job.config;

import com.rhbgroup.dcp.bo.batch.DcpBackofficeBatchApplication;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseFileToDBJobConfiguration;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.step.GSTCentralizedFileUpdateJobCommitStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.GSTCentralizedFileUpdateJobReadFileToStagingStepBuilder;
import com.rhbgroup.dcp.bo.batch.job.step.GSTCentralizedFileUpdateJobValidatorStepBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.Map;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;

@Configuration
@Lazy
public class GSTCentralizedFileUpdateJobConfiguration extends BaseFileToDBJobConfiguration {

    @Value("${job.gstcentralizedfileupadtejob.ftpfolder}")
    private String sourceFileFolder;
    @Value("${job.gstcentralizedfileupadtejob.name}")
    private String sourceFileName;
    @Value("${job.gstcentralizedfileupadtejob.namedateformat}")
    private String sourceFileDateFormat;

    @Autowired
    private GSTCentralizedFileUpdateJobCommitStagingStepBuilder gstCentralizedFileUpdateJobCommitStagingStepBuilder;
    @Autowired
    private GSTCentralizedFileUpdateJobReadFileToStagingStepBuilder gstCentralizedFileUpdateJobReadFileToStagingStepBuilder;
    @Autowired
    private GSTCentralizedFileUpdateJobValidatorStepBuilder gstCentralizedFileUpdateJobValidatorStepBuilder;

    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;

    @Bean(name = "GSTCentralizedFileUpdateJob")
    public Job BuildJob() {

        Step readFileToStagingStep = this.gstCentralizedFileUpdateJobReadFileToStagingStepBuilder.buildStep();
        Step commitStagingStep = this.gstCentralizedFileUpdateJobCommitStagingStepBuilder.buildStep();
        Step validateInputStep = this.gstCentralizedFileUpdateJobValidatorStepBuilder.buildStep();
        Step finalStep = moveFiletoSuccessOrFailedFolder();

        // Build simple job
        SimpleJobBuilder job = getDefaultJobBuilder("GSTCentralizedFileUpdateJob");

        // Check if jobexecutionid being passed or not
        Map<String, String> jobParameters = dcpBatchApplicationContext.getInitialJobArguments();

        // Commit to staging table
        if(!jobParameters.containsKey(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY))
        {
            job.next(copyFTPFileToLocal(sourceFileFolder,sourceFileName,sourceFileDateFormat));
            job.next(readFileToStagingStep);
            job.next(commitStagingStep);
        }

        job.next(validateInputStep);

        if(!jobParameters.containsKey(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY)){
            job.next(finalStep);
        }

        return job.build();
    }
}
