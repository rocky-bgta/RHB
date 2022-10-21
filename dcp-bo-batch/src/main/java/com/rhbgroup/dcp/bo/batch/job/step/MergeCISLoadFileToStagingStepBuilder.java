package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.MergeCISParameter.MERGE_CIS_EXEC_FILE_NAME;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.MergeCISFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedMergeCISDetailTxn;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedMergeCISRepositoryImpl;

@Component
@Lazy
public class MergeCISLoadFileToStagingStepBuilder extends BaseStepBuilder{
	
	private static final Logger logger = Logger.getLogger(MergeCISLoadFileToStagingStepBuilder.class);
	
	private static final String STEP_NAME="MergeCISLoadFileToStaging";
	
    @Autowired
    private BatchStagedMergeCISRepositoryImpl batchStagedMergeCISRepositoryImpl;
    
	@Value("${job.mergecisjob.detailnames}")
    private String fileContentNames;
	
    @Value("${job.mergecisjob.delimiter}")
    private String delimiters;
    
	@Value("${dcp.bo.batch.inputfolder.path}")
	private String targetFileFolder;
	
    @Autowired
    @Qualifier("MergeCISLoadFileToStaging.ItemReader")
    private ItemReader<BatchStagedMergeCISDetailTxn> itemReader;
    
    @Autowired
    @Qualifier("MergeCISLoadFileToStaging.ItemProcessor")
    private ItemProcessor<BatchStagedMergeCISDetailTxn, BatchStagedMergeCISDetailTxn> itemProcessor;
	
    @Autowired
    @Qualifier("MergeCISLoadFileToStaging.ItemWriter")
    private ItemWriter<BatchStagedMergeCISDetailTxn> itemWriter;
    
    String logMsg ="";
    String targetFileName="";
    
    @Bean("MergeCISLoadFileToStaging.ItemReader")
	@StepScope
	public FlatFileItemReader<BatchStagedMergeCISDetailTxn> mergeCISReader(@Value("#{stepExecution}") StepExecution stepExecution) {
        FlatFileItemReader<BatchStagedMergeCISDetailTxn> reader = new FlatFileItemReader<>();
    	targetFileName = stepExecution.getJobExecution().getExecutionContext().getString(MERGE_CIS_EXEC_FILE_NAME);
    	String jobname =stepExecution.getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
    	String targetPath = (new StringBuffer()	
    								.append(targetFileFolder)
    								.append(File.separator)
    								.append(jobname)
    								.append(File.separator)
    								.append(targetFileName)).toString();
    	String targetFullPath = StringUtils.replace(targetPath, "\\", File.separator);
    	logMsg = String.format("Batch Merge CIS reading file %s", targetFullPath) ;    	
    	logger.info(logMsg);
    	FileSystemResource targetFile = new FileSystemResource(targetFullPath);
        reader.setResource(targetFile);
        reader.setLineMapper( getDefaultDelimitedLineMapper(BatchStagedMergeCISDetailTxn.class
        		, fileContentNames
        		, delimiters
        		, new MergeCISFieldSetMapper()));
        return reader;
    }
    
    @Bean("MergeCISLoadFileToStaging.ItemProcessor")
	@StepScope
	public ItemProcessor<BatchStagedMergeCISDetailTxn, BatchStagedMergeCISDetailTxn> mergeCISProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		return new ItemProcessor<BatchStagedMergeCISDetailTxn, BatchStagedMergeCISDetailTxn>() {
			@Override
			public BatchStagedMergeCISDetailTxn process(BatchStagedMergeCISDetailTxn cisDetailTxn) throws Exception {
				return cisDetailTxn;
			}
		};
	}
    
    @Bean("MergeCISLoadFileToStaging.ItemWriter")
	@StepScope
    private ItemWriter<BatchStagedMergeCISDetailTxn> mergeCISWriter(@Value("#{stepExecution}") StepExecution stepExecution){
    	  return new ItemWriter<BatchStagedMergeCISDetailTxn>() {
  			@Override
  			public void write(List<? extends BatchStagedMergeCISDetailTxn> MergeCISDetailTxnList) throws Exception {	
  				String jobExecutionId=stepExecution.getJobExecution().getId().toString();
  				for(BatchStagedMergeCISDetailTxn mergeCISDetail : MergeCISDetailTxnList) {
  					mergeCISDetail.setJobExecutionId(jobExecutionId);
  					mergeCISDetail.setFileName(targetFileName);
  					logger.info(String.format("Inserting Merge CIS record to staging job exec id=%s, object [%s] to DB"
  								, jobExecutionId
  								, mergeCISDetail));
  					int row = batchStagedMergeCISRepositoryImpl.addMergeCISRecord(mergeCISDetail);
  					logger.info(String.format("Insert record into TBL_BATCH_STAGED_MERGE_CIS, impacted row =%s", row));
  				}
  			}
  		};
    }

	@Override
    @Bean(STEP_NAME)
	public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME).<BatchStagedMergeCISDetailTxn, BatchStagedMergeCISDetailTxn>chunk(100)
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.build();
	}
}
