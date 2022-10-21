package com.rhbgroup.dcp.bo.batch.framework.common.listener;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;

@Component
@Lazy
public class BatchJobCommonStepListener implements StepExecutionListener {
    static final Logger logger = Logger.getLogger(BatchJobCommonStepListener.class);
    static final Logger jobLogger = Logger.getLogger("com.rhbgroup.dcp.bo.batch.log");

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.info(String.format("Step Name:%s, Start Time:%s", stepExecution.getStepName(),
                DateUtils.getFormattedCurrentDateTimeString()));

        jobLogger.info(String.format("[%s]\tProceeding to step '%s'....",
                DateUtils.getFormattedCurrentDateTimeString(),stepExecution.getStepName()));
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        int readCount=stepExecution.getReadCount();
        int writeCount=stepExecution.getWriteCount();
        int skipCount=stepExecution.getSkipCount();
        int commitCount=stepExecution.getCommitCount();

        BatchUtils.addReadCount(readCount, stepExecution.getJobExecution());
        BatchUtils.addWriteCount(writeCount, stepExecution.getJobExecution());
        BatchUtils.addSkipCount(skipCount, stepExecution.getJobExecution());
        BatchUtils.addCommitCount(commitCount, stepExecution.getJobExecution());

        logger.info(String.format("Step Name:%s, Read Count:%d, Write Count:%d, Skip Count: %d, Commit Count: %d"
                ,stepExecution.getStepName()
                ,readCount
                ,writeCount
                ,skipCount
                ,commitCount));

        logger.info(String.format("Step Name:%s, End Time:%s", stepExecution.getStepName()
                , DateUtils.getFormattedCurrentDateTimeString()));

        if(!stepExecution.getFailureExceptions().isEmpty()) {
            for(Throwable exception: stepExecution.getFailureExceptions()){
                jobLogger.error(String.format("[%s]\tERROR:\t%s",DateUtils.getFormattedCurrentDateTimeString(),exception.getMessage()));
            }
        }
        return null;
    }
}
