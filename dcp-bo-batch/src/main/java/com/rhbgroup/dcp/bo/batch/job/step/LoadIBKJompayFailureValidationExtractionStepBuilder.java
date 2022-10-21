package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadIBKJompayFailureValidationExtractionJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BatchStagedJompayFailureTxnMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedJompayFailureTxn;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedJompayFailureTxnRepositoryImpl;

@Component
@Lazy
public class LoadIBKJompayFailureValidationExtractionStepBuilder extends BaseStepBuilder {

	private static final Logger logger = Logger.getLogger(LoadIBKJompayFailureValidationExtractionStepBuilder.class);
	
	private static final String STEP_NAME = "LoadIBKJompayFailureValidationExtractionStep";
	
	private static final String COLUMN_VALUES_EMPTY_OR_NULL_MESSAGE_TEMPLATE = "Column(s) %s values shall not be empty/null/invalid format, values[%s|%s|%s|%s]";
	
	private static final String REQUEST_TIME_FORMAT = "yyMMddHHmmss";
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private FlatFileItemReader<BatchStagedJompayFailureTxn> itemReader;
	
	@Autowired
    @Qualifier(STEP_NAME + ".ItemProcessor")
    private ItemProcessor<BatchStagedJompayFailureTxn,BatchStagedJompayFailureTxn> itemProcessor;
    
	@Autowired
    @Qualifier(STEP_NAME + ".ItemWriter")
    private ItemWriter<BatchStagedJompayFailureTxn> itemWriter;
	
	@Autowired
	private BatchStagedJompayFailureTxnRepositoryImpl batchStagedJompayFailureTxnRepository;
	
	@Autowired
	private LoadIBKJompayFailureValidationExtractionJobConfigProperties jobConfigProperties;
	
	@SuppressWarnings("unchecked")
	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	public FlatFileItemReader<BatchStagedJompayFailureTxn> batchStagedJompayFailureTxnItemReader(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		
		FlatFileItemReader<BatchStagedJompayFailureTxn> reader = new FlatFileItemReader<>();
		reader.setResource(new FileSystemResource((String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)));
		reader.setLineMapper(getSkipFixedLengthLineMapper(BatchStagedJompayFailureTxn.class
            ,jobConfigProperties.getDetailNames()
            ,jobConfigProperties.getDetailColumns()
            ,new BatchStagedJompayFailureTxnMapper()));
		// Skip the header
		reader.setLinesToSkip(1);
		
		logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
        return reader;
	}
	
	@Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<BatchStagedJompayFailureTxn, BatchStagedJompayFailureTxn> batchStagedJompayFailureTxnItemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
        return new ItemProcessor<BatchStagedJompayFailureTxn, BatchStagedJompayFailureTxn>() {
			@Override
			public BatchStagedJompayFailureTxn process(BatchStagedJompayFailureTxn batchStagedJompayFailureTxn) throws Exception {
				logger.trace(String.format("BatchStagedJompayFailureTxn received by processor [%s]", batchStagedJompayFailureTxn));
				return batchStagedJompayFailureTxn;
			}
        };
    }
	
	@Bean(STEP_NAME + ".ItemWriter")
    @StepScope
    public ItemWriter<BatchStagedJompayFailureTxn> batchStagedJompayFailureTxnItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
    	logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
        return new ItemWriter<BatchStagedJompayFailureTxn>() {
			@Override
			public void write(List<? extends BatchStagedJompayFailureTxn> batchStagedJompayFailureTxns) throws Exception {
				String inputFilePath = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
				logger.debug(String.format("Fetching input file path [%s] from context", inputFilePath));
				String fileName = new File(inputFilePath).getName();
				boolean isFieldsValid = true;
				boolean isDBIssuesFound = false;
				
				for(BatchStagedJompayFailureTxn batchStagedJompayFailureTxn : batchStagedJompayFailureTxns) {
					List<String> invalidColumns = getInvalidColumnNames(batchStagedJompayFailureTxn);
					if(invalidColumns != null) {
						isFieldsValid = false;
						logger.error(String.format("Invalid BatchStagedJompayFailureTxn [%s] found", batchStagedJompayFailureTxn));
						String suspenseColumns = StringUtils.collectionToCommaDelimitedString(invalidColumns);
						String suspenseMessage = String.format(COLUMN_VALUES_EMPTY_OR_NULL_MESSAGE_TEMPLATE, suspenseColumns,
							batchStagedJompayFailureTxn.getBillerCode(), 
							batchStagedJompayFailureTxn.getPaymentChannel(), 
							batchStagedJompayFailureTxn.getRequestTime(), 
							batchStagedJompayFailureTxn.getReasonForFailure());
						logger.error(suspenseMessage);
					} else {
						batchStagedJompayFailureTxn.setFileName(fileName);
						batchStagedJompayFailureTxn.setCreatedTime(new Date());
						
						logger.trace(String.format("Inserting BatchStagedJompayFailureTxn object [%s] to DB", batchStagedJompayFailureTxn));
						try {
							batchStagedJompayFailureTxnRepository.addBatchStagedIBKPaymentTxnToStaging(batchStagedJompayFailureTxn);
						} catch(BatchException e) {
							isDBIssuesFound = true;
							stepExecution.addFailureException(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
						}
					}
				}
				
				// Limit exception to add inside the context, we just need to each exception once
				if(!isFieldsValid) {
					stepExecution.addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE));
				}

				if(!isFieldsValid || isDBIssuesFound) {
					stepExecution.getJobExecution().setStatus(BatchStatus.FAILED);
				}
			}
		};
    }
	
	@Override
	public Step buildStep() {
		logger.info(String.format("Building step [%s]", STEP_NAME));
		
		Step step = getDefaultStepBuilder(STEP_NAME)
				.<BatchStagedJompayFailureTxn,BatchStagedJompayFailureTxn>chunk(jobConfigProperties.getChunkSize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
		
		logger.info(String.format("[%s] step build successfully", STEP_NAME));
		return step;
	}
	
	private List<String> getInvalidColumnNames(BatchStagedJompayFailureTxn batchStagedJompayFailureTxn) {
		List<String> invalidColumns = null;
		
		// Retrieving required info from the BatchStagedIBGRejectTxn object
		String billerCode = batchStagedJompayFailureTxn.getBillerCode();
		String paymentChannel = batchStagedJompayFailureTxn.getPaymentChannel();
		String requestTime = batchStagedJompayFailureTxn.getRequestTimeStr();
		String reasonForFailure = batchStagedJompayFailureTxn.getReasonForFailure();
		
		invalidColumns = addInvalidStringColumnToList(billerCode, "BILLER_CODE", invalidColumns);
		invalidColumns = addInvalidStringColumnToList(paymentChannel, "PAYMENT_CHANNEL", invalidColumns);
		invalidColumns = addInvalidDateStringColumnToList(requestTime, "REQUEST_TIME", invalidColumns);
		invalidColumns = addInvalidStringColumnToList(reasonForFailure, "REASON_FOR_FAILURE", invalidColumns);
		
		return invalidColumns;
	}
	
	private List<String> addInvalidStringColumnToList(String source, String columnName, List<String> invalidColumns) {
		if(source == null || source.trim().isEmpty()) {
			if(invalidColumns == null) {
				invalidColumns = new ArrayList<>();
			}
			invalidColumns.add(columnName);
		}
		
		return invalidColumns;
	}
	
	private List<String> addInvalidDateStringColumnToList(String source, String columnName, List<String> invalidColumns) {
		boolean isValid = true;
		
		if(source == null || source.trim().isEmpty()) {
			isValid = false;
		}
		
		// Check the format of the date if it is not empty
		if(isValid) {
			try {
				DateUtils.getDateFromString(source, REQUEST_TIME_FORMAT);
			} catch (ParseException e) {
				isValid = false;
			}
		}
		
		if(!isValid) {
			if(invalidColumns == null) {
				invalidColumns = new ArrayList<>();
			}
			invalidColumns.add(columnName);
		}
		
		return invalidColumns;
	}
	
}
