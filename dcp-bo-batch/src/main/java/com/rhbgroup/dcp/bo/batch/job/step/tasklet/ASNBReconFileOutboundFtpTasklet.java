package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import java.text.ParseException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchValidationException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ASNBReconSettlementJobParameter.ASNB_OUTPUT_FILE_LIST_RECON;

@Component
@Lazy
public class ASNBReconFileOutboundFtpTasklet implements Tasklet{

    private static final Logger logger = Logger.getLogger(ASNBReconFileOutboundFtpTasklet.class);
    private static final String ASNBREPORTJOB = "AsnbReportJob";
    
    private String outputFolder;
    private String sourceFolder;
  
    private FTPConfigProperties ftpConfigProperties;
    
	public void init(String outputFolder, String sourceFolder) {
      
        this.outputFolder = outputFolder;
        this.sourceFolder = sourceFolder;
       
    }

    
    public void initFTPConfig(FTPConfigProperties ftpConfigProperties) {
        this.ftpConfigProperties = ftpConfigProperties;
    }
    
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext)
			throws BatchException, ParseException {
		logger.info(String.format("Executing tasklet [%s]", this.getClass().getSimpleName()));

		String date = DateUtils.getProcessDate(chunkContext);
		logger.info("Date to be process is :: " + date);

        List<String> fileList = (List<String>) chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(ASNB_OUTPUT_FILE_LIST_RECON);
        logger.info("File list to process ::" + fileList);

	    processUploadFiletoFTP(sourceFolder + "/" + ASNBREPORTJOB + "/", outputFolder, ftpConfigProperties, fileList);
		
		return RepeatStatus.FINISHED;

	}

    private boolean processUploadFiletoFTP(String sourceFileFullPath, String targetFileFullPath,FTPConfigProperties ftpConfigProperties, List<String> fileList) throws BatchException {
        try{
            logger.info(String.format("Preparing file to copy.. [%s]", fileList));
            logger.info(String.format("Preparing target file.. [%s]", targetFileFullPath));
            logger.info(String.format("Preparing target FTP.. [%s:%s]", ftpConfigProperties.getHost(), ftpConfigProperties.getPort()));

            if (fileList != null) {
                for (String file : fileList) {
                    if(ftpConfigProperties.isIssecureftp()) {
                        FTPUtils.uploadFileToFTP(sourceFileFullPath + file, targetFileFullPath, ftpConfigProperties);
                    } else {
                        FTPUtils.uploadFileToFTP(sourceFileFullPath + file, targetFileFullPath + "/" + file, ftpConfigProperties);
                    }
                }
            }
            logger.info(String.format("Tasklet [%s] executed successfully", this.getClass().getSimpleName()));

            return true;
        } catch (BatchValidationException ex) {
            String errorMessage = String.format("Failed to upload file [%s] from source FTP folder [%s] to target FTP folder [%s] using FTP [%s:%s]",
                    fileList, sourceFileFullPath, targetFileFullPath, ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
            logger.error(errorMessage, ex);
            throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, ex);
        }
    }
}
