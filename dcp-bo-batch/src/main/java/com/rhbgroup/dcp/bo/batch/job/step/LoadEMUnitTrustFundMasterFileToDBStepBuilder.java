
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
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.UnitTrustFundMasterMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUnitTrustJobStatusControl;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileHeader;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileTrailer;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFundMaster;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFundMasterDetail;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUnitTrustJobStatusControlRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustFundMasterRepositoryImpl;

@Component
@Lazy
public class LoadEMUnitTrustFundMasterFileToDBStepBuilder extends LoadEMUnitTrustFileToDBStepBuilder{
	private static final Logger logger = Logger.getLogger(LoadEMUnitTrustFundMasterFileToDBStepBuilder.class);
	private static final String STEP_NAME = "LoadEMUnitTrustFundMasterFileToDBStep";

	@Autowired
	private UnitTrustFundMasterRepositoryImpl utFundMasterRepoImpl;
	
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
    	getStepParam(stepExecution, configProperties.getUtFundFile());
		cleanFundMasterData();
		FieldSetMapper<UnitTrustFileAbs> detailMapper = new UnitTrustFundMasterMapper();
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
            	}else if(utFileRecord instanceof UnitTrustFundMasterDetail) {
            		return processUTDetail((UnitTrustFundMasterDetail)utFileRecord);
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
				List<UnitTrustFundMaster> utFunds = new ArrayList<>();
				for(UnitTrustFileAbs utFileRec : utList) {
					if(errorCount>0) {
						logger.error(String.format("Error while writing unit trust fund master errorCount=%s",errorCount));
						break;
					}
					try {
						UnitTrustFundMasterDetail utDetail = (UnitTrustFundMasterDetail) utFileRec;
						UnitTrustFundMaster utFund = new UnitTrustFundMaster();
						utFund.setFundId(utDetail.getFundId());
						utFund.setFundName(utDetail.getFundName());
						utFund.setFundCurr(utDetail.getFundCurr());
						utFund.setFundCurrNavPrice( Double.parseDouble( utDetail.getFundCurrNavPrice() ) );
						utFund.setNavDate( DateUtils.getDateFromString(utDetail.getNavDate() , BatchSystemConstant.General.COMMON_DATE_DATA_FORMAT )   );
						utFund.setProdCategoryCode(utDetail.getProdCategoryCode());
						utFund.setProdCategoryDesc(utDetail.getProdCategoryDesc());
						utFund.setRiskLevelCode (utDetail.getRiskLevelCode());
						utFund.setRiskLevelDesc (utDetail.getRiskLevelDesc());
						utFund.setMyrNavPrice(Double.parseDouble( utDetail.getMyrNavPrice()) );
						setCommonUTField(utFund);
						utFunds.add(utFund);
					}catch(Exception ex) {
						++errorCount;
						errorMsg=String.format("Exception while adding unit trust fund master errorCount=%s,exception=%s",errorCount,ex.getMessage());
						logger.error(errorMsg);
					}
				}
			
				try {
					if (errorCount == 0) {
						int inserted = utFundMasterRepoImpl.addRecordBatch(utFunds, targetDataSet);
						logger.debug(String.format("Insert %s row UT Fund Master", inserted));
						utFileConfig.setStatus(STATUS_SUCCESS);
					}else {
						utFileConfig.setStatus(STATUS_FAILED);
						stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, errorMsg));
					}
					updateUTTblStatus();
				}catch(Exception ex) {
					++errorCount;
					utFileConfig.setStatus(STATUS_FAILED);
					errorMsg = String.format("Exception while update tbl fund master job status errorCount=%s, exception=%s",errorCount,ex.getMessage());
					logger.error(errorMsg);
					throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,errorMsg,ex);
				}
			}
		};
	}
    
	private UnitTrustFundMasterDetail processUTDetail(UnitTrustFundMasterDetail fundDetail) {
		++line;
		if (StringUtils.isBlank(fundDetail.getFundId())) {
			logger.error(String.format("Line %s-Fund ID must not be null or empty",line));
			++errorCount;
			return null;
		}
		if (StringUtils.isBlank(fundDetail.getFundName())) {
			logger.error(String.format("Line %s-Fund Name must not be null or empty",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(fundDetail.getFundCurrNavPrice())) {
			logger.error(String.format("Line %s-Fund Curr NAV value format invalid",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(fundDetail.getNavDate())) {
			logger.error(String.format("Line %s-NAV DATE format invalid",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(fundDetail.getProdCategoryCode())) {
			logger.error(String.format("Line %s-Prod Category code must not be null",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(fundDetail.getProdCategoryDesc())) {
			logger.error(String.format("Line %s-Prod Category desc must not be null",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(fundDetail.getRiskLevelCode())) {
			logger.error(String.format("Line %s-Risk level code must not be null",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(fundDetail.getRiskLevelDesc())) {
			logger.error(String.format("Line %s-Risk level desc must not be null",line));
			++errorCount;
			return null;
		}

		if (StringUtils.isBlank(fundDetail.getMyrNavPrice())) {
			logger.error(String.format("Line %s-MYR NAV price must not be null",line));
			++errorCount;
			return null;
		}
		return fundDetail;
	}

	@SneakyThrows
	private void cleanFundMasterData() {
		try {
			int row=utFundMasterRepoImpl.deleteAllRecords(targetDataSet);
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
		utTblJobStatus.setTblUtFundMasterStatus (utFileConfig.getStatus());
		utTblJobStatus.setUpdatedBy(configProperties.getBatchCode());
		utTblJobStatus.setUpdatedTime(new Date());
		row = utJobControlRepoImpl.updateTblFundMasterStatus(utTblJobStatus);	
		logger.debug( String.format("update Batch UT Job Control Status, Tbl_fund_master_status=%s, impacted row=%s",utFileConfig.getStatus(),row));
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
