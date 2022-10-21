package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBKPaymentTxnRepositoryImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Lazy
public class DeleteExistingIBKDynamicBillerReportTasklet implements Tasklet {

    private static final Logger logger = Logger.getLogger(DeleteExistingIBKDynamicBillerReportTasklet.class);

    @Autowired
    private BatchStagedIBKPaymentTxnRepositoryImpl batchStagedIBKPaymentTxnRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));
        if (chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)) {
            String inputFilePath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
            logger.debug(String.format("Fetching input file path [%s] from context", inputFilePath));
            String fileName = new File(inputFilePath).getName();
            int counter = batchStagedIBKPaymentTxnRepository.deleteExistingBatchStagedIBKPaymentReport(fileName);
            logger.info(String.format("Deleted [%d] records in DB table TBL_BATCH_STAGED_IBK_PAYMENT_RPT where FILE_NAME is [%s]", counter, fileName));
        } else {
            logger.info("No record(s) to be delete in DB table TBL_BATCH_STAGED_IBK_PAYMENT_RPT");
        }

        return RepeatStatus.FINISHED;
    }
}
