package com.rhbgroup.dcp.bo.batch.job.step;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.STATUS_FAILED;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.STATUS_SUCCESS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.STATUS_INITIAL;

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
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.UnitTrustCustomerMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUnitTrustJobStatusControl;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomer;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomerDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileHeader;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileTrailer;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUnitTrustJobStatusControlRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustCustomerRepositoryImpl;

@Component
@Lazy
public class LoadEMUnitTrustCustomerFileToDBStepBuilder extends LoadEMUnitTrustFileToDBStepBuilder{
	private static final Logger logger = Logger.getLogger(LoadEMUnitTrustCustomerFileToDBStepBuilder.class);
	private static final String STEP_NAME = "LoadEMUnitTrustCustomerFileToDBStep";

	@Autowired
	private UnitTrustCustomerRepositoryImpl utCustomerRepoImpl;
	
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
    	getStepParam(stepExecution, configProperties.getUtCustomerFile());
		cleanCustTargetData();
		FieldSetMapper<UnitTrustFileAbs> detailMapper = new UnitTrustCustomerMapper();
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
            	}else if(utFileRecord instanceof UnitTrustCustomerDetail) {
            		return processUTDetail((UnitTrustCustomerDetail)utFileRecord);
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
				String errorMsg ="";
				logger.debug("Start writing UT customer");
				List<UnitTrustCustomer> utCustomers = new ArrayList<>();
				for(UnitTrustFileAbs utFileRec : utList) {
					if(errorCount>0) {
						logger.error(String.format("Error while writing unit trust customer errorCount=%s",errorCount));
						break;
					}
					try {
						UnitTrustCustomerDetail utDetail = (UnitTrustCustomerDetail) utFileRec;
						UnitTrustCustomer utCustomer = new UnitTrustCustomer();
						utCustomer.setCisNo(utDetail.getCisNo());
						utCustomer.setStatus(STATUS_INITIAL);
						utCustomer.setName(utDetail.getCustomerName());
						utCustomer.setFileName(fileName);
						setCommonUTField(utCustomer);
						utCustomers.add(utCustomer);
					}catch(Exception ex) {
						++errorCount;
						errorMsg = String.format("Exception while adding into tbl customer job status errorCount=%s,exception=%s",errorCount,ex.getMessage());
						logger.error(errorMsg);
					}
				}
				
				try {
					if(errorCount==0) {
						int inserted = utCustomerRepoImpl.addRecordBatch(utCustomers, targetDataSet);
						logger.debug(String.format("UT Customer -Inserted rows  %s",inserted));
						utFileConfig.setStatus(STATUS_SUCCESS);
					}else {
						utFileConfig.setStatus(STATUS_FAILED);
						stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, errorMsg));
					}
					updateUTTblStatus();
				}catch(Exception ex) {
					++errorCount;
					utFileConfig.setStatus(STATUS_FAILED);
					errorMsg = String.format("Exception while update tbl customer job status errorCount=%s,exception=%s",errorCount,ex.getMessage());
					logger.error(errorMsg);
					throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,errorMsg,ex);
				}
			}
		};
	}
    
    private UnitTrustCustomerDetail processUTDetail(UnitTrustCustomerDetail custDetail) {
    	++line;
    	if( StringUtils.isBlank(custDetail.getCisNo())) {
    		logger.error(String.format("Line %s-CIS No should not be null or empty",line));
    		++errorCount;
    		return null;
    	}
    	if( StringUtils.isBlank(custDetail.getCustomerName())) {
    		logger.error(String.format("Line %s-Customer Name should not be null or empty",line));
    		++errorCount;
    		return null;
    	}
    	return custDetail;
    }

    @SneakyThrows
	private void cleanCustTargetData() {
		try {
			int row=utCustomerRepoImpl.deleteAllRecords(targetDataSet);
			logger.debug( String.format("Deleted %s records", row));
		} catch (Exception ex) {
			logger.error("Exception while delete data", ex);
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, ex);
		}
	}

	@SneakyThrows
	private void updateUTTblStatus() {
		int row =0;
		BatchUnitTrustJobStatusControl utTblJobStatus= new BatchUnitTrustJobStatusControl();
		utTblJobStatus.setJobExecutionId(jobExecutionId);
		utTblJobStatus.setBatchEndDatetime(new Date());
		utTblJobStatus.setTblUtCustomerStatus(utFileConfig.getStatus());
		utTblJobStatus.setUpdatedBy(configProperties.getBatchCode());
		utTblJobStatus.setUpdatedTime(new Date());
		row = utJobControlRepoImpl.updateTblCustomerStatus(utTblJobStatus);	
		logger.debug( String.format("update Batch UT Job Control Status, Tbl_customer_status=%s, impacted row=%s",utFileConfig.getStatus(),row));
		if(STATUS_FAILED==utFileConfig.getStatus()) {
			utTblJobStatus.setStatus(STATUS_FAILED);
			utJobControlRepoImpl.updateJobStatus(utTblJobStatus);	
		}
	}
	
	@Override
	@Bean
	public Step buildStep() {
		// TODO Auto-generated method stub
		return getDefaultStepBuilder(STEP_NAME)
			.<UnitTrustFileAbs, UnitTrustFileAbs>chunk(configProperties.getChunkSize())
			.reader(itemReader)
			.processor(itemProcessor)
			.writer(itemWriter)
			.build();
	}
	
	
}
