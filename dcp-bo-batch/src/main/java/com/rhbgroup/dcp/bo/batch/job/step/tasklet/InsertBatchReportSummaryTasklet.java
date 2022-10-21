package com.rhbgroup.dcp.bo.batch.job.step.tasklet;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;

import java.util.Date;

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
import com.rhbgroup.dcp.bo.batch.framework.repository.BillerRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.BillerSettlementRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.FpxPaymentRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.JompayBillerRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.McaRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.OlaRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.RegistrationRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.repository.TermDepositRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;

@Component
@Lazy
public class InsertBatchReportSummaryTasklet implements Tasklet, InitializingBean {
    private static final Logger logger = Logger.getLogger(InsertBatchReportSummaryTasklet.class);
    
    @Autowired
    private AsnbRepositoryImpl asnbRepositoryImpl;
    
    @Autowired
    private RegistrationRepositoryImpl registrationRepositoryImpl;
    
    @Autowired
    private McaRepositoryImpl mcaRepositoryImpl;
    
    @Autowired
    private BillerSettlementRepositoryImpl billerSettlementRepositoryImpl;
    
    @Autowired
    private JompayBillerRepositoryImpl jompayBillerRepositoryImpl;
    
    @Autowired
    private OlaRepositoryImpl olaRepositoryImpl;
    
    @Autowired
    private TermDepositRepositoryImpl termDepositRepositoryImpl;
    
    @Autowired
    private FpxPaymentRepositoryImpl fpxPaymentRepositoryImpl;
    
    protected Date batchProcessingDate;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        logger.info("Initializing Report Summary Config..");
		try {
			String batchSystemDate=chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT) ;
			String message=String.format("batch Processing Date =%s",batchProcessingDate);
			logger.info(message);
			String parameterKey = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_KEY);
			message=String.format("parameterKey =%s", parameterKey);
			logger.info(message);
			if(parameterKey.equals("ASNB")) {
				asnbRepositoryImpl.insertSummary(batchProcessingDate);
			}else if(parameterKey.equals("REGISTRATION")) {
				registrationRepositoryImpl.insertSummary(batchProcessingDate);
			}else if(parameterKey.equals("MCA_CURRENCY")) {
				mcaRepositoryImpl.insertSummary(batchProcessingDate);
			}else if(parameterKey.equals("MCA_PRECIOUS_METAL")) {
				mcaRepositoryImpl.insertMetalSummary(batchProcessingDate);
			}else if(parameterKey.equals("MCA_PLACEMENT_WITHDRAWAL")) {
				mcaRepositoryImpl.insertPlacementSummary(batchProcessingDate);
			}else if(parameterKey.equals("OTHER_BILLER")) {
				billerSettlementRepositoryImpl.insertSummary(batchProcessingDate);
			}else if(parameterKey.equals("OTHER_BILLER_SETTLEMENT")) {
				billerSettlementRepositoryImpl.insertDailySummary(batchProcessingDate);
			}else if(parameterKey.equals("JOMPAY_BILLER")) {
				jompayBillerRepositoryImpl.insertDailySummary(batchProcessingDate);
			}else if(parameterKey.equals("OLA")) {
				olaRepositoryImpl.insertDailySummary(batchProcessingDate);
			}else if(parameterKey.equals("TERM_DEPOSIT_PLACEMENT_WITHDRAWAL")) {
				termDepositRepositoryImpl.insertSummary(batchProcessingDate);
			}else if(parameterKey.equals("FPX_PAYMENT_GATEWAY")) {
				fpxPaymentRepositoryImpl.insertSummary(batchProcessingDate);
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
