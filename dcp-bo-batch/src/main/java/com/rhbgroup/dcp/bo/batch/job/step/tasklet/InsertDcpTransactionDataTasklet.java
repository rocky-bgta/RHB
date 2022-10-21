package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import java.util.Date;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_KEY;
import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.AsnbRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.DeviceManagementRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.McaRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.NonFinancialRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.OlaRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.PaymentRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.RegistrationRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.TermDepositRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.TransferRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;

@Component
@Lazy
public class InsertDcpTransactionDataTasklet implements Tasklet, InitializingBean {
    private static final Logger logger = Logger.getLogger(InsertDcpTransactionDataTasklet.class);
    @Autowired
    private PaymentRepositoryImpl paymentRepositoryImpl;
    
    @Autowired
    private AsnbRepositoryImpl asnbRepositoryImpl;
    
    @Autowired
    private RegistrationRepositoryImpl registrationRepositoryImpl;
    
    @Autowired
    private McaRepositoryImpl mcaRepositoryImpl;
    
    @Autowired
    private OlaRepositoryImpl olaRepositoryImpl;
    
    @Autowired
    private DeviceManagementRepositoryImpl deviceManagementRepositoryImpl;
    
    @Autowired
    private TransferRepositoryImpl transferRepositoryImpl;
    
    @Autowired
    private NonFinancialRepositoryImpl nonFinancialRepositoryImpl;
    
    @Autowired
    private TermDepositRepositoryImpl termDepositRepositoryImpl;
    
	protected Date batchProcessingDate;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        logger.info("Initializing DB Biller Payment Config..");
		try {
			String batchSystemDate=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT) ;
			String message=String.format("batch Processing Date =%s",batchProcessingDate);
			logger.info(message);
			String parameterKey = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_KEY);
			message=String.format("parameterKey =%s", parameterKey);
			logger.info(message);
			if(parameterKey.equals("ASNB")) {
				asnbRepositoryImpl.insertStagedData(batchProcessingDate);
			}else if(parameterKey.equals("REGISTRATION")) {
				registrationRepositoryImpl.insertStagedData(batchProcessingDate);
			}else if(parameterKey.equals("MCA")) {
				mcaRepositoryImpl.insertStagedData(batchProcessingDate);
			}else if(parameterKey.equals("PAYMENT")) {
				paymentRepositoryImpl.insertStagedData(batchProcessingDate);
			}else if(parameterKey.equals("OLA")) {
				olaRepositoryImpl.insertStagedData(batchProcessingDate);
			}else if(parameterKey.equals("DEVICE_MANAGEMENT")) {
				deviceManagementRepositoryImpl.insertStagedData(batchProcessingDate);
			}else if(parameterKey.equals("TRANSFER")) {
				transferRepositoryImpl.insertStagedData(batchProcessingDate);
			}else if(parameterKey.equals("NON_FINANCIAL_TXN")) {
				nonFinancialRepositoryImpl.insertStagedData(batchProcessingDate);
			}else if(parameterKey.equals("TERM_DEPOSIT")) {
				termDepositRepositoryImpl.insertStagedData(batchProcessingDate);
			}
		} catch(Exception ex) {
				String errorMsg = String.format("Exception: exception=%s",ex.getMessage());
				logger.error(errorMsg);
				chunkContext.getStepContext().getStepExecution().getJobExecution().setExitStatus(ExitStatus.FAILED);
				throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,errorMsg,ex);
		}
        return RepeatStatus.FINISHED;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
		// Do nothing because this is the implementation of Spring Batch
    }
}
