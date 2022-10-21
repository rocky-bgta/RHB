package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchIBGRejectStatusParameter.BATCH_IBG_REJECT_STATUS_EXEC_FILE_NAME;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.IBGRejectStatusFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBGRejectStatusTxn;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBGRejectTxnRepositoryImpl;
import org.apache.commons.lang3.StringUtils;

@Component
@Lazy
public class IBGRejectStatusLoadFileToStagingStepBuilder extends BaseStepBuilder {

	private static final Logger logger = Logger.getLogger(IBGRejectStatusLoadFileToStagingStepBuilder.class);
	
	private static final String STEP_NAME="IBGRejectStatusFileToStaging";
	
	@Value("${job.updateibgrejectedstatusjob.chunksize}")
    private int chunksize;
	@Value("${job.updateibgrejectedstatusjob.detailnames}")
    private String fileContentNames;
    @Value("${job.updateibgrejectedstatusjob.detailcolumns}")
    private String fileContentColumns;
	@Value("${dcp.bo.batch.inputfolder.path}")
	private String targetFileFolder;
    
    @Autowired
    @Qualifier("IBGRejectStatusFileToStaging.ItemReader")
    private ItemReader<BatchStagedIBGRejectStatusTxn> itemReader;
    
    @Autowired
    @Qualifier("IBGRejectStatusFileToStaging.ItemProcessor")
    private ItemProcessor<BatchStagedIBGRejectStatusTxn,BatchStagedIBGRejectStatusTxn> itemProcessor;
    
    @Autowired
    @Qualifier("IBGRejectStatusFileToStaging.ItemWriter")
    private ItemWriter<BatchStagedIBGRejectStatusTxn> itemWriter;
    
    String logMsg ="";
    String targetFileName="";
        
    @Bean("IBGRejectStatusFileToStaging.ItemReader")
	@StepScope
	@SneakyThrows
	public FlatFileItemReader<BatchStagedIBGRejectStatusTxn> ibgRejectStatusReader(@Value("#{stepExecution}") StepExecution stepExecution) {
        FlatFileItemReader<BatchStagedIBGRejectStatusTxn> reader = new FlatFileItemReader<>();
		try {
	    	targetFileName = stepExecution.getJobExecution().getExecutionContext().getString(BATCH_IBG_REJECT_STATUS_EXEC_FILE_NAME);
	    	String jobname =stepExecution.getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
	    	String targetPath = (new StringBuffer()	
	    								.append(targetFileFolder)
	    								.append(File.separator)
	    								.append(jobname)
	    								.append(File.separator)
	    								.append(targetFileName)).toString();
	    	String targetFullPath = StringUtils.replace(targetPath, "\\", File.separator);
	    	logMsg = String.format("Batch IBG Reject status reading file %s", targetFullPath) ;    	
	    	logger.info(logMsg);
	    	FileSystemResource targetFile = new FileSystemResource(targetFullPath);
	    	if(!targetFile.exists()) {
	        	logMsg = String.format("Batch IBG Reject status file not found-file %s", targetFullPath) ;    	
	        	logger.info(logMsg);
	        	throw new BatchException(BatchErrorCode.FILE_NOT_FOUND, logMsg);
	    	}
	        
	        reader.setResource(targetFile);
	        reader.setLineMapper(getDefaultFixedLengthLineMapper(BatchStagedIBGRejectStatusTxn.class
	                ,fileContentNames
	                ,fileContentColumns
	                ,new IBGRejectStatusFieldSetMapper()));
		}catch(Exception ex) {
        	logMsg = String.format("Batch IBG Reject status Exception while reading file-%s", ex.getMessage()) ;    	
        	logger.info(logMsg);
        	logger.error(ex);
        	throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR,BatchErrorCode.GENERIC_SYSTEM_ERROR_MESSAGE, ex);
		}
        return reader;
    }
    
    @Bean("IBGRejectStatusFileToStaging.ItemProcessor")
	@StepScope
    public ItemProcessor<BatchStagedIBGRejectStatusTxn, BatchStagedIBGRejectStatusTxn> ibgRejectStatusProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        return new ItemProcessor<BatchStagedIBGRejectStatusTxn, BatchStagedIBGRejectStatusTxn>() {
            @Override
            public BatchStagedIBGRejectStatusTxn process(BatchStagedIBGRejectStatusTxn ibgRejectStatus) throws Exception {
                    return ibgRejectStatus;
            }
        };
    }
    @Autowired
    private BatchStagedIBGRejectTxnRepositoryImpl ibgRejectStatusStagingRepositoryImpl ;
    @Bean("IBGRejectStatusFileToStaging.ItemWriter")
    @StepScope
    private ItemWriter<BatchStagedIBGRejectStatusTxn> ibgRejectStatusWriter (@Value("#{stepExecution}") StepExecution stepExecution){
    	  return new ItemWriter<BatchStagedIBGRejectStatusTxn>() {
  			@Override
  			public void write(List<? extends BatchStagedIBGRejectStatusTxn> IBGRejectStatusList) throws Exception {	
  				String jobExecutionId=stepExecution.getJobExecution().getId().toString();
  				for(BatchStagedIBGRejectStatusTxn ibgRejectStatus : IBGRejectStatusList) {
  					ibgRejectStatus.setJobExecutionId(jobExecutionId);
  					ibgRejectStatus.setFileName(targetFileName);
  					logger.trace(String.format("Inserting IBG Reject status to staging job exec id=%s, object [%s] to DB"
  								, jobExecutionId
  								, ibgRejectStatus));
  					int row = ibgRejectStatusStagingRepositoryImpl.addBatchStagedIBGRejectStatusStaging(ibgRejectStatus);
  					logger.info(String.format("Insert record into TBL_BATCH_STAGED_IBG_REJECT_TXN, impacted row =%s", row));
  				}
  			}
  		};
    }
    
    @Bean("IBGRejectStatusFileToStaging")
    public Step buildStep() {
        return getDefaultStepBuilder(STEP_NAME).<BatchStagedIBGRejectStatusTxn,BatchStagedIBGRejectStatusTxn>chunk(chunksize)                
        		.reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }    
}
