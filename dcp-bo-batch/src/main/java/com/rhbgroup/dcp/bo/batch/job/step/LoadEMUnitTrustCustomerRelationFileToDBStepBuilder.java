
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
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.UnitTrustCustomerRelationshipMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUnitTrustJobStatusControl;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomerRelationDetail;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustCustomerRelationship;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileHeader;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileTrailer;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUnitTrustJobStatusControlRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UnitTrustCustomerRelationshipRepositoryImpl;

@Component
@Lazy
public class LoadEMUnitTrustCustomerRelationFileToDBStepBuilder extends LoadEMUnitTrustFileToDBStepBuilder{
	private static final Logger logger = Logger.getLogger(LoadEMUnitTrustCustomerRelationFileToDBStepBuilder.class);
	private static final String STEP_NAME = "LoadEMUnitTrustCustomerRelationFileToDBStep";

	@Autowired
	private UnitTrustCustomerRelationshipRepositoryImpl utCustomerRelRepoImpl;
	
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
    	getStepParam(stepExecution, configProperties.getUtCustomerRelFile());
		cleanCustRelationTargetData();
		FieldSetMapper<UnitTrustFileAbs> detailMapper = new UnitTrustCustomerRelationshipMapper();
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
            	}else if(utFileRecord instanceof UnitTrustCustomerRelationDetail) {
            		return processUTDetail((UnitTrustCustomerRelationDetail)utFileRecord);
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
				logger.debug("Writing UT Customer Relationship");
				List <UnitTrustCustomerRelationship> utCustomers = new ArrayList<>();
				for(UnitTrustFileAbs utFileRec : utList) {
					if(errorCount>0) {
						logger.error(String.format("Error while writing unit trust customer relationship errorCount=%s",errorCount));
						break;
					}
					try {
						UnitTrustCustomerRelationDetail utDetail = (UnitTrustCustomerRelationDetail)utFileRec;
						UnitTrustCustomerRelationship utCustomer = new UnitTrustCustomerRelationship();
						utCustomer.setCisNo(utDetail.getCisNo());
						utCustomer.setAcctNo(utDetail.getAccountNo());
						utCustomer.setJoinType(utDetail.getJoinType());
						setCommonUTField(utCustomer);
						utCustomers.add(utCustomer);
					}catch(Exception ex) {
						++errorCount;
						errorMsg = String.format("Exception while adding record into tbl customer relationship job status errorCount=%s,exception=%s",errorCount,ex.getMessage());
						logger.error(errorMsg);
					}
				}
			
				try {
					if (errorCount == 0) {
						int inserted = utCustomerRelRepoImpl.addRecordBatch(utCustomers, targetDataSet);
						logger.debug(String.format("Insert %s row UT Customer Relation", inserted));
						utFileConfig.setStatus(STATUS_SUCCESS);
					}else {
						utFileConfig.setStatus(STATUS_FAILED);
						stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, errorMsg));
					}
					updateUTTblStatus();
				}catch(Exception ex) {
					++errorCount;
					utFileConfig.setStatus(STATUS_FAILED);
					errorMsg = String.format("Exception while update tbl customer relationship job status errorCount=%s,exception=%s",errorCount,ex.getMessage());
					logger.error(errorMsg);
					throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,errorMsg,ex);
				}
			}
		};
	}
    
    private UnitTrustCustomerRelationDetail processUTDetail(UnitTrustCustomerRelationDetail custDetail) {
    	++line;
    	if( StringUtils.isBlank(custDetail.getCisNo())) {
    		logger.error(String.format("Line %s-CIS No must not be null or empty",line));
    		++errorCount;
    		return null;
    	}
    	if( StringUtils.isBlank(custDetail.getAccountNo())) {
    		logger.error(String.format("Line %s-Account Number must not be null or empty",line));
    		++errorCount;
    		return null;
    	}
    	if( StringUtils.isBlank(custDetail.getJoinType())) {
    		logger.error(String.format("Line %s-Join Type must not be null or empty",line));
    		++errorCount;
    		return null;
    	}
    	return custDetail;
    }

    @SneakyThrows
	private void cleanCustRelationTargetData() {
		try {
			int row=utCustomerRelRepoImpl.deleteAllRecords(targetDataSet);
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
		utTblJobStatus.setTblUtCustomerRelStatus(utFileConfig.getStatus());
		utTblJobStatus.setUpdatedBy(configProperties.getBatchCode());
		utTblJobStatus.setUpdatedTime(new Date());
		row = utJobControlRepoImpl.updateTblCustomerRelStatus(utTblJobStatus);	
		logger.debug( String.format("update Batch UT Job Control Status, Tbl_customer_rel_status=%s, impacted row=%s",utFileConfig.getStatus(),row));
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
