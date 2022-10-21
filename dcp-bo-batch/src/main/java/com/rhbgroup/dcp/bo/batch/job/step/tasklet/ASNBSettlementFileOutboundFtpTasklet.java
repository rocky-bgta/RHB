package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import java.text.ParseException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchValidationException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.HostFtpConfigProperties;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ASNBReconSettlementJobParameter.ASNB_OUTPUT_FILE_LIST_RECON;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ASNBReconSettlementJobParameter.ASNB_OUTPUT_FILE_LIST_SETTLEMENT;

@Component
@Lazy
public class ASNBSettlementFileOutboundFtpTasklet implements Tasklet{

    private static final Logger logger = Logger.getLogger(ASNBSettlementFileOutboundFtpTasklet.class);
    private static final String ASNBREPORTJOB = "AsnbReportJob";
    
    private String sourceFolder;
    private String hostFolder;
    
    @Autowired
    private HostFtpConfigProperties hostFtpConfigProperties;
    
    public void init(String sourceFolder, String hostFolder) {
   
        this.sourceFolder = sourceFolder;
        this.hostFolder = hostFolder;
    }

   
    public void initFTPConfig(HostFtpConfigProperties hostFtpConfigProperties) {
        this.hostFtpConfigProperties = hostFtpConfigProperties;
    }

  

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws BatchException, ParseException {
        logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));

        String date = DateUtils.getProcessDate(chunkContext);
        logger.info("Date to be process is :: " + date);

        List<String> fileList = (List<String>) chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(ASNB_OUTPUT_FILE_LIST_SETTLEMENT);
        logger.info("File list to process ::" + fileList);

        processUploadFiletoFTP(sourceFolder + "/" + ASNBREPORTJOB + "/", hostFolder, hostFtpConfigProperties, fileList);

        return RepeatStatus.FINISHED;
    }


    private boolean processUploadFiletoFTP(String sourceFileFullPath, String targetFileFullPath, HostFtpConfigProperties hostFtpConfigProperties, List<String> fileList) throws BatchException {
        try{
            logger.info(String.format("Preparing file to copy.. [%s]", fileList));
            logger.info(String.format("Preparing target file.. [%s]", targetFileFullPath));
            logger.info(String.format("Preparing  target FTP.. [%s:%s]", hostFtpConfigProperties.getHost(), hostFtpConfigProperties.getPort()));

            if (fileList != null) {
                for (String file : fileList) {
                    if(hostFtpConfigProperties.isIssecureftp()) {
                        FTPUtils.uploadFileToFTP(sourceFileFullPath + file, targetFileFullPath, hostFtpConfigProperties);
                    } else {
                        FTPUtils.uploadFileToFTP(sourceFileFullPath + file, targetFileFullPath + "/" + file, hostFtpConfigProperties);
                    }
                }
            }
            logger.info(String.format("Tasklet [%s] executed successfully", this.getClass().getSimpleName()));

            return true;
        } catch (BatchValidationException ex) {
            String errorMessage = String.format("Failed to upload file [%s] from source FTP folder [%s] to target FTP folder [%s] using FTP [%s:%s]",
                    fileList, sourceFileFullPath, targetFileFullPath, hostFtpConfigProperties.getHost(), hostFtpConfigProperties.getPort());
            logger.error(errorMessage, ex);
            throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, ex);
        }
    }

}
