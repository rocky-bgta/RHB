package com.rhbgroup.dcp.bo.batch.job.step;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.STATUS_FAILED;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.STATUS_SUCCESS;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.UnitTrustAccountHoldingMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUnitTrustJobStatusControl;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustAccountHolding;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustAccountHoldingDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileHeader;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileTrailer;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUnitTrustJobStatusControlRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustAccountHoldingRepositoryImpl;

@Component
@Lazy
public class LoadEMUnitTrustAccountHoldingFileToDBStepBuilder extends LoadEMUnitTrustFileToDBStepBuilder{
	private static final Logger logger = Logger.getLogger(LoadEMUnitTrustAccountHoldingFileToDBStepBuilder.class);
	private static final String STEP_NAME = "LoadEMUnitTrustAccountHoldingFileToDBStep";

	@Autowired
	private UnitTrustAccountHoldingRepositoryImpl utAccountHldRepoImpl;
	
	@Autowired
	private BatchUnitTrustJobStatusControlRepositoryImpl utJobControlRepoImpl;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<UnitTrustFileAbs> itemReader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<UnitTrustFileAbs, UnitTrustFileAbs> itemProcessor;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<UnitTrustFileAbs> itemWriter;
	
    @Bean(STEP_NAME + ".ItemReader")
    @StepScope
	@SneakyThrows
    public FlatFileItemReader<UnitTrustFileAbs> utReader(@Value("#{stepExecution}") StepExecution stepExecution) {
    	getStepParam(stepExecution, configProperties.getUtAccountHldFile());
		cleanAccountTargetData();
		FieldSetMapper<UnitTrustFileAbs> detailMapper = new UnitTrustAccountHoldingMapper();
		return getReader(utFileConfig, detailMapper);
    }
    
    @Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<UnitTrustFileAbs, UnitTrustFileAbs> utProcessor() {
        return new ItemProcessor<UnitTrustFileAbs, UnitTrustFileAbs>() {
            @Override
			public UnitTrustFileAbs process(UnitTrustFileAbs utFileRecord) throws Exception {
            	if(utFileRecord instanceof UnitTrustFileHeader) {
            		logger.debug("processing header");
            		processUTHeader((UnitTrustFileHeader) utFileRecord );
            	}else if(utFileRecord instanceof UnitTrustAccountHoldingDetail) {
            		return processUTDetail((UnitTrustAccountHoldingDetail)utFileRecord);
            	}else if(utFileRecord instanceof UnitTrustFileTrailer) {
            		logger.debug("processing trailer");
            	}
				return null;
			}
        };
    }
    
    @Bean(STEP_NAME + ".ItemWriter")
    @StepScope
	public ItemWriter<UnitTrustFileAbs> utWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
  	  return new ItemWriter<UnitTrustFileAbs>() {
			@Override
			public void write(List<? extends UnitTrustFileAbs> utList) throws Exception {
				String errorMsg = "";
				logger.debug("Writing UT Account Holding");
				List<UnitTrustAccountHolding> utAccountHoldings = new ArrayList<>();
				for(UnitTrustFileAbs utFileRec : utList) {
					if(errorCount>0) {
						logger.error(String.format("Error while writing unit trust account holding errorCount=%s",errorCount));
						break;
					}
					try {
						UnitTrustAccountHoldingDetail utDetail = (UnitTrustAccountHoldingDetail)utFileRec;
						UnitTrustAccountHolding utAccount = new UnitTrustAccountHolding();
						utAccount.setJobExecutionId(jobExecutionId);
						utAccount.setAcctNo(utDetail.getAcctNo() );
						utAccount.setFundId(utDetail.getFundId());
						utAccount.setHoldingUnit(Double.parseDouble(utDetail.getHoldingUnit()));
						
						utAccount.setFundCurrMarketVal(Double.parseDouble(utDetail.getFundCurrMarketVal()));
						utAccount.setFundCurrUnrealisedGainLoss(Double.parseDouble(utDetail.getFundCurrUnrealisedGainLoss()));
						utAccount.setFundCurrUnrealisedGainLossPercent(Double.parseDouble(utDetail.getFundCurrUnrealisedGainLossPercent()));
						utAccount.setFundCurrInvestAmnt(Double.parseDouble(utDetail.getFundCurrInvestAmnt()));
						utAccount.setFundCurrAvgUnitPrice(Double.parseDouble(utDetail.getFundCurrAvgUnitPrice()));
						
						utAccount.setFundMyrMarketVal(Double.parseDouble(utDetail.getFundMyrMarketVal()));
						utAccount.setFundMyrUnrealisedGainLoss(Double.parseDouble(utDetail.getFundMyrUnrealisedGainLoss()));
						utAccount.setFundMyrUnrealisedGainLossPercent(Double.parseDouble(utDetail.getFundMyrUnrealisedGainLossPercent()));
						utAccount.setFundMyrInvestAmnt(Double.parseDouble(utDetail.getFundMyrInvestAmnt()));
						utAccount.setFundMyrAvgUnitPrice (Double.parseDouble(utDetail.getFundMyrAvgUnitPrice()));
						setCommonUTField(utAccount);
						utAccountHoldings.add(utAccount);
					}catch(Exception ex) {
						++errorCount;
						errorMsg=String.format("Exception while writing unit trust account holding errorCount=%s, exception=%s",errorCount,ex.getMessage());
						logger.error(errorMsg);
					}
				}
			
				try {
					if (errorCount == 0) {
						int inserted = utAccountHldRepoImpl.addRecordBatch(utAccountHoldings, targetDataSet);
						logger.debug(String.format("Insert %s row UT Account Holding", inserted));
						utFileConfig.setStatus(STATUS_SUCCESS);
					}else {
						utFileConfig.setStatus(STATUS_FAILED);
						stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, errorMsg));
					}
					updateUTTblStatus();
				}catch(Exception ex) {
					++errorCount;
					utFileConfig.setStatus(STATUS_FAILED);
					errorMsg = String.format("Exception while update tbl account holding job status errorCount=%s",errorCount);
					logger.error(errorMsg);
					throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,errorMsg,ex);
				}
			}
		};
	}
    
	private UnitTrustAccountHoldingDetail processUTDetail(UnitTrustAccountHoldingDetail acctDetail) {
		++line;
		if (StringUtils.isBlank(acctDetail.getAcctNo())) {
			logger.error(String.format("Line %s-Account No must not be null or empty",line));
			++errorCount;
			return null;
		}
		if (StringUtils.isBlank(acctDetail.getFundId())) {
			logger.error(String.format("Line %s-Fund Id must not be null or empty",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(acctDetail.getHoldingUnit())) {
			logger.error(String.format("Line %s-Holding Unit must be numeric",line));
			++errorCount;
			return null;
		}
		if (StringUtils.isBlank(acctDetail.getFundCurrMarketVal())) {
			logger.error(String.format("Line %s-Fund Curr Market Val must be numeric",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(acctDetail.getFundCurrUnrealisedGainLoss())) {
			logger.error(String.format("Line %s-Fund Curr Unrealised GainLoss must be numeric",line));
			++errorCount;
			return null;
		}
		if (StringUtils.isBlank(acctDetail.getFundCurrUnrealisedGainLossPercent())) {
			logger.error(String.format("Line %s-Fund Curr Unrealised GainLoss Percent must be numeric",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(acctDetail.getFundCurrInvestAmnt())) {
			logger.error(String.format("Line %s-Fund Curr Invest Amnt must be numeric",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(acctDetail.getFundCurrAvgUnitPrice())) {
			logger.error(String.format("Line %s-Fund Curr AvgUnit Price must be numeric",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(acctDetail.getFundMyrMarketVal())) {
			logger.error(String.format("Line %s-Fund Myr Market Val  must be numeric",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(acctDetail.getFundMyrUnrealisedGainLoss())) {
			logger.error(String.format("Line %s-Fund Myr Unrealised GainLoss  must be numeric",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(acctDetail.getFundMyrUnrealisedGainLossPercent())) {
			logger.error(String.format("Line %s-Fund Myr Unrealised GainLoss Percent  must be numeric",line));
			++errorCount;
			return null;
		}
		if (StringUtils.isBlank(acctDetail.getFundMyrInvestAmnt())) {
			logger.error(String.format("Line %s-Fund Myr Invest Amnt must be numeric %s",line,acctDetail.getFundMyrInvestAmnt()));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(acctDetail.getFundMyrAvgUnitPrice())) {
			logger.error(String.format("Line %s-Fund Myr Avg Unit Price must be numeric %s",line,acctDetail.getFundMyrAvgUnitPrice()));
			++errorCount;
			return null;
		}
		return acctDetail;
	}

	@SneakyThrows
	private void cleanAccountTargetData() {
		try {
			int row=utAccountHldRepoImpl.deleteAllRecords(targetDataSet);
			logger.debug( String.format("Deleted %s records", row));
		} catch (Exception ex) {
			logger.error("Exception while delete data", ex);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, ex);
		}
	}

	@SneakyThrows
	protected void updateUTTblStatus() {
		int row =0;
		BatchUnitTrustJobStatusControl utTblJobStatus= new BatchUnitTrustJobStatusControl();
		utTblJobStatus.setJobExecutionId(jobExecutionId);
		utTblJobStatus.setBatchEndDatetime(new Date());
		utTblJobStatus.setTblUtAccountHoldingStatus(utFileConfig.getStatus());
		utTblJobStatus.setUpdatedBy(configProperties.getBatchCode());
		utTblJobStatus.setUpdatedTime(new Date());
		row = utJobControlRepoImpl.updateTblAccountHldStatus(utTblJobStatus);	
		logger.debug( String.format("update Batch UT Job Control Status, TBL_UT_ACCOUNT_HLD_STATUS=%s, impacted row=%s",utFileConfig.getStatus(),row));
		if(STATUS_FAILED==utFileConfig.getStatus()) {
			utTblJobStatus.setStatus(STATUS_FAILED);
			utJobControlRepoImpl.updateJobStatus(utTblJobStatus);
		}
	}
	
	@Bean
	public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME)
				.<UnitTrustFileAbs, UnitTrustFileAbs>chunk(configProperties.getChunkSize())
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.build();
	}
	
	
}
