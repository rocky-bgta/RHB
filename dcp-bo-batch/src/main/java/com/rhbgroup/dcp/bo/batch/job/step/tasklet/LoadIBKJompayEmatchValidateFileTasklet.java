package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadIBKJompayEmatchJobConfigProperties;
@Component
@Lazy
public class LoadIBKJompayEmatchValidateFileTasklet  implements Tasklet{
	private static final Logger logger = Logger.getLogger(LoadIBKJompayEmatchValidateFileTasklet.class);

	@Autowired
	LoadIBKJompayEmatchJobConfigProperties configProperty;
	
	@Override
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		String filePath="";
		filePath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
		File ibkJompayEmatchingFile = FileUtils.getFile(filePath);
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(ibkJompayEmatchingFile))){
			filePath = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
			String header;
			String detail;
			int totalRec = 0;
			double totalAmount=0;
			header = bufferedReader.readLine();
			if( StringUtils.isBlank(header) ||  !header.startsWith(configProperty.getHeaderprefix())) {
				logger.info(String.format("File header not found while validating file %s", filePath ));
				throw new BatchException(BatchErrorCode.FILE_VALIDATION_ERROR, "Header not found");
			}
			while((detail = bufferedReader.readLine())!=null) {
				double debitAmount=0;
				if (detail.startsWith(configProperty.getDetailprefix())) {
					logger.info( "detail substring amount = "+ StringUtils.substring(detail, 45, 62));
					totalRec++;
					debitAmount = Double.parseDouble(StringUtils.substring(detail, 45, 62));
					totalAmount = totalAmount + debitAmount;
					logger.info(String.format("Validating detail record lineCount=%s,debitAmount=%s,totalAmount=%s",totalRec, debitAmount, totalAmount));
				} else if (detail.startsWith(configProperty.getTrailerprefix())) {
					logger.info("Validating trailer record");
					logger.info( "trailer substring amount = "+ StringUtils.substring(detail, 21, 38) );

					int trailerCount = Integer.parseInt(StringUtils.substring(detail, 2, 19));
					double trailerAmount = Double.parseDouble(StringUtils.substring(detail, 21, 38));
					if(trailerCount!=totalRec) {
						logger.error("Total number of records in the file does not match the total records inserted");
						throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
					}

					if (BatchUtils.roundDecimal(trailerAmount,2) != BatchUtils.roundDecimal( totalAmount,2) ) {
						logger.error("Total debit amount in the file does not match total debit amount inserted");
						throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
					}
				}
			}
		}catch(IOException ex) {
			logger.info(String.format("IO exception while validating file %s", ex.getMessage()));
			logger.error(ex);
			throw new BatchException(BatchErrorCode.FILE_VALIDATION_ERROR, BatchErrorCode.FILE_VALIDATION_ERROR_MESSAGE, ex);
		}catch(NumberFormatException ex) {
			logger.info(String.format("Number format exception while validating file %s", ex.getMessage()));
			logger.error(ex);
			throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE, ex);
		}
		return RepeatStatus.FINISHED;
	}
}
