package com.rhbgroup.dcp.bo.batch.framework.common.tasklet;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.text.ParseException;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobPropertyFile.BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;

@Component
@Lazy
public class UploadLocalFileToFTPServer implements Tasklet {

    private String targetFileFolderPath;
    private String targetFileName;
    private String targetFileDateFormat;

    @Autowired
    FTPConfigProperties ftpConfigProperties;

    public void init(String targetFileFolderPath, String targetFileName, String targetFileDateFormat) {
        this.targetFileFolderPath = targetFileFolderPath;
        this.targetFileName = targetFileName;
        this.targetFileDateFormat = targetFileDateFormat;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws BatchException, ParseException {
        String targetFileFullPath = null;
        String targetDateStr = null;

        String batchSystemDateStr = (String)chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
        String sourceFileFullPath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY);

        targetDateStr = DateUtils.convertDateFormat(batchSystemDateStr, DEFAULT_DATE_FORMAT, targetFileDateFormat);
        targetFileFullPath = this.targetFileName.replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, targetDateStr);
        StringBuilder strBuilder = new StringBuilder(targetFileFolderPath);
        targetFileFullPath = strBuilder.append(BatchSystemConstant.FTP.FTP_SEPARATOR).append(targetFileFullPath).toString();

        CopyOutputFolderFileToFTPTasklet ftpTasklet = new CopyOutputFolderFileToFTPTasklet();
        ftpTasklet.processUploadFiletoFTP(sourceFileFullPath, targetFileFullPath, ftpConfigProperties);
        return RepeatStatus.FINISHED;
    }
}