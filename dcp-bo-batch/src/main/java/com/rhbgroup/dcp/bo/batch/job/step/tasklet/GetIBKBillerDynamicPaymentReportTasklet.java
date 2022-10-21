package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.FTPIBKConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadIBKBillerDynamicPaymentJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchBillerPaymentConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentInboundConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BoBillerTemplateConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchBillerDynamicPaymentConfigRepositoryImpl;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.Date;
import java.util.Queue;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;

@Component
@Lazy
public class GetIBKBillerDynamicPaymentReportTasklet implements Tasklet {

    private static final Logger logger = Logger.getLogger(GetIBKBillerDynamicPaymentReportTasklet.class);

    @Value(SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
    private String inputFolderPath;

    @Autowired
    private LoadIBKBillerDynamicPaymentJobConfigProperties jobConfigProperties;

    @Autowired
    private FTPIBKConfigProperties ftpConfigProperties;

    @Autowired
    BatchBillerDynamicPaymentConfigRepositoryImpl batchBillerDynamicPaymentConfigRepositoryImpl;

    @Autowired
    @Qualifier("BillerPaymentReportInboundConfigQueue")
    private Queue<BillerPaymentInboundConfig> batchBillerPaymentConfigQueue;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws BatchException, ParseException, IOException {
        logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));

        BillerPaymentInboundConfig batchBillerPaymentConfig = batchBillerPaymentConfigQueue.poll();
        if (batchBillerPaymentConfig != null) {
            BatchBillerPaymentConfig paymentConfig = batchBillerDynamicPaymentConfigRepositoryImpl.getBillerPaymentConfigDtls(batchBillerPaymentConfig.getBillerCode());
            BoBillerTemplateConfig boBillerTemplateConfig = batchBillerDynamicPaymentConfigRepositoryImpl.getBillerTemplateConfigDtls(paymentConfig.getReportTemplateId());

            // Get the jobname and batch system date from job parameters
            String batchJobName = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY);

            // Getting the actual batch process date, for batch system date we will need to -1 day to get the process date
            Date batchProcessDate = getProcessDate(chunkContext.getStepContext().getStepExecution());
            logger.debug(String.format("Processing date to be used [%s]", batchProcessDate));

            // Getting the target input folder path appended with job name
            String targetFolderPath = getTargetFolder(Paths.get(inputFolderPath, batchJobName).toFile());

            // Download  biller file from the FTP
            String sourceFileName = null;
            try {
                String fileNameFormat = batchBillerPaymentConfig.getReportNameFormat();
                sourceFileName = BatchUtils.generateSourceFileName(fileNameFormat, batchProcessDate);
            } catch (Exception e) {
                String errorMessage = String.format("Error happened when generating source filename with format [%s] using date [%s]", batchBillerPaymentConfig.getFileNameFormat(), batchProcessDate);
                logger.error(errorMessage, e);
                throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, e);
            }
            String sourceFilePath = batchBillerPaymentConfig.getIbkFtpFolder() + sourceFileName;
            File targetFile = Paths.get(targetFolderPath, sourceFileName).toFile();
            String targetFilePath = targetFile.getAbsolutePath();
            try {
                logger.info(String.format("FTP IP [%s] UserName [%s] Pass [%s] IssecureFTP [%s] port [%s]", ftpConfigProperties.getHost(), ftpConfigProperties.getUsername(), ftpConfigProperties.getPassword(), ftpConfigProperties.isIssecureftp(), ftpConfigProperties.getPort()));
                logger.debug(String.format("FTP get source [%s] and store into destination [%s]", sourceFilePath, targetFilePath));
                logger.debug(String.format("Storing biller code [%s] to context", batchBillerPaymentConfig.getBillerCode()));
                chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_CODE, batchBillerPaymentConfig.getBillerCode());
                FTPUtils.downloadFileFromFTP(sourceFilePath, targetFilePath, ftpConfigProperties);
                //backup the downloaded file
                Files.move(Paths.get(targetFolderPath, sourceFileName),
                        Paths.get(targetFolderPath, sourceFileName).resolveSibling(sourceFileName + ".ori"),
                        StandardCopyOption.REPLACE_EXISTING);
                preprocessFile(targetFilePath + ".ori", targetFilePath, boBillerTemplateConfig);

                String processingDateStr = DateUtils.formatDateString(batchProcessDate, DEFAULT_JOB_PARAMETER_DATE_FORMAT);

                logger.info(String.format("processingDateStr [%s] ", processingDateStr));

                // Store the saved filepath to context for later step reference
                logger.debug(String.format("Storing target filepath [%s] to context", targetFilePath));
                chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY, targetFilePath);
            } catch (BatchException e) {
                String errorMessage = String.format("Error happened when trying to FTP-Get the source file [%s] from [%s:%s], skip processing for BillerCode [%s]", sourceFilePath,
                        ftpConfigProperties.getHost(), ftpConfigProperties.getPort(),
                        batchBillerPaymentConfig.getBillerCode());
                if (e.getDcpStatusCode().equalsIgnoreCase(BatchErrorCode.FILE_NOT_FOUND)) {
                    errorMessage += ", file is not found in the FTP server";
                    logger.warn(errorMessage, e);
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().remove(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
                } else {
                    logger.error(errorMessage, e);
                    throw e;
                }
            }

        }

        logger.info(String.format("Tasklet [%s] executed successfully", this.getClass().getSimpleName()));
        return RepeatStatus.FINISHED;
    }

    private void preprocessFile(String source, String target, BoBillerTemplateConfig boBillerTemplateConfig) throws IOException {

        try (
                FileReader fr = new FileReader(new File(source));
                BufferedReader br = new BufferedReader(fr);
                FileWriter fw = new FileWriter(target);
                BufferedWriter bw = new BufferedWriter(fw);
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() > 0) {
                    if (Integer.toHexString(line.charAt(0) | 0x10000).substring(1).equalsIgnoreCase("000c")) {
                        int lineToSkip = boBillerTemplateConfig.getLineSkipFromTop();
                        while ((line = br.readLine()) != null && lineToSkip > 0) {
                            --lineToSkip;
                        }
                    }
                    bw.write(line);
                }
                bw.append("\n");     //line feed
            }
            fw.flush();
        } catch (IOException e) {
            logger.error("Failed to process at preprocessFile : ", e);
            throw e;
        }
    }

    private String getTargetFolder(File targetFolder) {
        if (!targetFolder.exists()) {
            logger.debug(String.format("Target folder [%s] doesn't exists, create it", targetFolder));
            targetFolder.mkdirs();
        }
        return targetFolder.getAbsolutePath();
    }

    private Date getProcessDate(StepExecution stepExecution) throws BatchException {
        String batchProcessDateStr = stepExecution.getJobExecution().getJobParameters().getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY);
        logger.debug(String.format("Batch processing date from external [%s]", batchProcessDateStr));

        // Get batch system date from DB if it not found in job parameters
        String batchSystemDateStr = null;
        if (batchProcessDateStr == null) {
            batchSystemDateStr = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
            logger.debug(String.format("Batch processing date from DB [%s]", batchSystemDateStr));
        }

        // Getting the actual batch process date, for batch system date we will need to -1 day to get the process date
        Date batchProcessDate = null;
        String dateStr = null;
        String dateStrFormat = null;
        try {
            if (batchProcessDateStr != null) {
                // The job data parameter is expected to be in format yyyyMMdd
                dateStr = batchProcessDateStr;
                dateStrFormat = General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
                batchProcessDate = DateUtils.getDateFromString(dateStr, dateStrFormat);
            } else {
                dateStr = batchSystemDateStr;
                dateStrFormat = General.DEFAULT_DATE_FORMAT;
                batchProcessDate = DateUtils.getDateFromString(dateStr, dateStrFormat);
                batchProcessDate = DateUtils.addDays(batchProcessDate, -1);
            }
        } catch (ParseException e) {
            String errorMessage = String.format("Error happened while parsing date [%s] using format [%s]", dateStr, dateStrFormat);
            logger.error(errorMessage, e);
            throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, e);
        }

        return batchProcessDate;
    }
}
