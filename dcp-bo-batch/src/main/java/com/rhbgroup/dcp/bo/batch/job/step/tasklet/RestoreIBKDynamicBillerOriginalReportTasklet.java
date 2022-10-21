package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
@Lazy
public class RestoreIBKDynamicBillerOriginalReportTasklet implements Tasklet {

    private static final Logger logger = Logger.getLogger(RestoreIBKDynamicBillerOriginalReportTasklet.class);

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));
        if (chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)) {
            String inputFilePath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
            logger.debug(String.format("Fetching input file path [%s] from context", inputFilePath));
            Files.move(Paths.get(inputFilePath + ".ori"),
                    Paths.get(inputFilePath),
                    StandardCopyOption.REPLACE_EXISTING);

            logger.info(String.format("Restore [%s] to [%s]", inputFilePath + ".ori", inputFilePath));
        } else {
            logger.info("No record(s) to be delete in DB table TBL_BATCH_STAGED_IBK_PAYMENT_RPT");
        }

        return RepeatStatus.FINISHED;
    }
}
