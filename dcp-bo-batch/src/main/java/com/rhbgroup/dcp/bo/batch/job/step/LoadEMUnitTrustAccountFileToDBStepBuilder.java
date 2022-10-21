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
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.UnitTrustAccountMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUnitTrustJobStatusControl;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustAccount;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustAccountDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileHeader;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileTrailer;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUnitTrustJobStatusControlRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustAccountRepositoryImpl;

@Component
@Lazy
public class LoadEMUnitTrustAccountFileToDBStepBuilder extends LoadEMUnitTrustFileToDBStepBuilder{
	private static final Logger logger = Logger.getLogger(LoadEMUnitTrustAccountFileToDBStepBuilder.class);
	private static final String STEP_NAME = "LoadEMUnitTrustAccountFileToDBStep";

	@Autowired
	private UnitTrustAccountRepositoryImpl utAccountRepoImpl;
	
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
    	getStepParam(stepExecution, configProperties.getUtAccountFile());
		cleanAccountTargetData();
		FieldSetMapper<UnitTrustFileAbs> detailMapper = new UnitTrustAccountMapper();
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
            	}else if(utFileRecord instanceof UnitTrustAccountDetail ) {
            		return processUTDetail((UnitTrustAccountDetail)utFileRecord);
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
				String errMsg="";
				logger.debug("Writing UT Account");
				List<UnitTrustAccount> utAccounts = new ArrayList<>();
				for(UnitTrustFileAbs utFileRec : utList) {
					if(errorCount>0) {
						logger.error(String.format("Error while writing unit trust account errorCount=%s",errorCount));
						break;
					}
					try {
						UnitTrustAccountDetail utDetail = (UnitTrustAccountDetail)utFileRec;
						UnitTrustAccount utAccount = new UnitTrustAccount();
						utAccount.setAcctNo(utDetail.getAccountNo());
						utAccount.setAcctType(utDetail.getAccountType());
						utAccount.setSignatoryCode(utDetail.getSignatoryCode());
						utAccount.setSignatoryDesc(utDetail.getSignatoryDescription() );
						utAccount.setAcctStatusCode(utDetail.getAccountStatusCode());
						utAccount.setAcctStatusDesc(utDetail.getAccountStatusDesc());
						utAccount.setInvestProd(utDetail.getAccountInvestProduct());
						utAccount.setLastPerformedTxnDate( DateUtils.getDateFromString(utDetail.getLastPerformedTxnDate(), BatchSystemConstant.General.COMMON_DATE_DATA_FORMAT ));
						setCommonUTField(utAccount);
						utAccounts.add(utAccount);
					}catch(Exception ex) {
						++errorCount;
						errMsg = String.format("Exception while writing unit trust account errorCount=%s, exception=%s",errorCount,ex.getMessage());
						logger.error(errMsg);
					}
				}
			
				try {
					if (errorCount == 0) {
						int inserted = utAccountRepoImpl.addRecordBatch(utAccounts, targetDataSet);
						logger.debug(String.format("Insert %s row UT Account", inserted));
						utFileConfig.setStatus(STATUS_SUCCESS);
					}else {
						utFileConfig.setStatus(STATUS_FAILED);
						stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, errMsg));
					}
					updateUTTblStatus();
				}catch(Exception ex) {
					++errorCount;
					utFileConfig.setStatus(STATUS_FAILED);
					errMsg = String.format("Exception while update tbl account job status errorCount=%s",errorCount);
					logger.error(ex);
					throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,errMsg,ex);
				}
			}
		};
	}
    
    private UnitTrustAccountDetail processUTDetail(UnitTrustAccountDetail acctDetail) {
    	++line;
    	if( StringUtils.isBlank(acctDetail.getAccountNo())) {
    		logger.error(String.format("Line %s-Account no must not be null or empty",line));
    		++errorCount;
    		return null;
    	}
    	
    	if( StringUtils.isBlank(acctDetail.getAccountType() )) {
    		logger.error(String.format("Line %s-Account Type must not be null or empty",line));
    		++errorCount;
    		return null;
    	}
    	
    	if( StringUtils.isBlank(acctDetail.getAccountStatusCode() )) {
    		logger.error(String.format("Line %s-Account Status Code must not be null or empty",line));
    		++errorCount;
    		return null;
    	}
    	
    	if( StringUtils.isBlank(acctDetail.getAccountStatusDesc() )) {
    		logger.error(String.format("Line %s-Account Status Desc must not be null or empty",line));
    		++errorCount;
    		return null;
    	}
    	
    	if( StringUtils.isBlank(acctDetail.getAccountInvestProduct() )) {
    		logger.error(String.format("Line %s-Account Invest Prod must not be null or empty",line));
    		++errorCount;
    		return null;
    	}
    	
    	return acctDetail;
    }

    @SneakyThrows
	private void cleanAccountTargetData() {
		try {
			int row=utAccountRepoImpl.deleteAllRecords(targetDataSet);
			logger.info( String.format("Deleted %s records", row));
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
		utTblJobStatus.setTblUtAccountStatus(utFileConfig.getStatus());
		utTblJobStatus.setUpdatedBy(configProperties.getBatchCode());
		utTblJobStatus.setUpdatedTime(new Date());
		row = utJobControlRepoImpl.updateTblAccountStatus(utTblJobStatus);	
		logger.debug( String.format("update Batch UT Job Control Status, TBL_UT_ACCOUNT_STATUS=%s, impacted row=%s",utFileConfig.getStatus(),row));
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
