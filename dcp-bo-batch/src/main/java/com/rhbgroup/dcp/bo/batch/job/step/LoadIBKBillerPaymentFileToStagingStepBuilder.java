package com.rhbgroup.dcp.bo.batch.job.step;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadIBKBillerPaymentJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BatchStagedIBKPaymentTxnDetailMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BatchStagedIBKPaymentTxnHeaderMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BatchStagedIBKPaymentTxnTrailerMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxn;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnDetail;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnHeader;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnTrailer;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBKPaymentTxnRepositoryImpl;

@Component
@Lazy
public class LoadIBKBillerPaymentFileToStagingStepBuilder extends BaseStepBuilder {
	
	private static final Logger logger = Logger.getLogger(LoadIBKBillerPaymentFileToStagingStepBuilder.class);
	
	private static final String STEP_NAME = "LoadIBKBillerPaymentFileToStagingStep";
	
	private static final String TXN_TIME_FORMAT = "HHmmss";
	
	private static final String EMTPY_IBK_BILLER_PAYMENT_FILE_PATH = "classpath:batch/input/LoadIBKBillerPaymentJob/EmptyIBKBillerPaymentFile.txt";
	
	@Autowired
	private LoadIBKBillerPaymentJobConfigProperties configProperties;
    
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private FlatFileItemReader<BatchStagedIBKPaymentTxn> itemReader;
	
	@Autowired
    @Qualifier(STEP_NAME + ".ItemProcessor")
    private ItemProcessor<BatchStagedIBKPaymentTxn,BatchStagedIBKPaymentTxn> itemProcessor;
    
	@Autowired
    @Qualifier(STEP_NAME + ".ItemWriter")
    private ItemWriter<BatchStagedIBKPaymentTxn> itemWriter;
	
	@Autowired
	private BatchStagedIBKPaymentTxnRepositoryImpl batchStagedIBKPaymentTxnRepository;
	
	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	public FlatFileItemReader<BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxnItemReader(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		
		// Create the tokenizer to parse specific line in the file, e.g. header, detail, trailer
		LineTokenizer headerTokenizer = BatchUtils.getFixedLengthTokenizer(configProperties.getHeaderNames(), configProperties.getHeaderColumns());
		LineTokenizer detailTokenizer = BatchUtils.getFixedLengthTokenizer(configProperties.getDetailNames(), configProperties.getDetailColumns());
		LineTokenizer trailerTokenizer = BatchUtils.getFixedLengthTokenizer(configProperties.getTrailerNames(), configProperties.getTrailerColumns());
		
		String headerPrefixPattern  = configProperties.getHeaderPrefix() + "*";
		String detailPrefixPattern  = configProperties.getDetailPrefix() + "*";
		String trailerPrefixPattern  = configProperties.getTrailerPrefix() + "*";
		
		Map<String, LineTokenizer> tokenizerMap = new HashMap<>();
		tokenizerMap.put(headerPrefixPattern, headerTokenizer);
		tokenizerMap.put(detailPrefixPattern, detailTokenizer);
		tokenizerMap.put(trailerPrefixPattern, trailerTokenizer);
		logger.debug(String.format("Tokenizer map [%s] created successfully", tokenizerMap));
		
		// Create the fieldmappers to map the line details to object for haeder, detail, trailer
		FieldSetMapper<BatchStagedIBKPaymentTxn> headerMapper = new BatchStagedIBKPaymentTxnHeaderMapper();
		FieldSetMapper<BatchStagedIBKPaymentTxn> detailMapper = new BatchStagedIBKPaymentTxnDetailMapper();
		FieldSetMapper<BatchStagedIBKPaymentTxn> trailerMapper = new BatchStagedIBKPaymentTxnTrailerMapper();
		
		Map<String, FieldSetMapper<BatchStagedIBKPaymentTxn>> fieldSetMapperMap = new HashMap<>();
		fieldSetMapperMap.put(headerPrefixPattern, headerMapper);
		fieldSetMapperMap.put(detailPrefixPattern, detailMapper);
		fieldSetMapperMap.put(trailerPrefixPattern, trailerMapper);
		logger.debug(String.format("FieldSetMapper map [%s] created successfully", fieldSetMapperMap));
		
		// Create the linemapper that hold the tokenizers and fieldmappers
		PatternMatchingCompositeLineMapper<BatchStagedIBKPaymentTxn> lineMapper = new PatternMatchingCompositeLineMapper<>();
		lineMapper.setTokenizers(tokenizerMap);
		lineMapper.setFieldSetMappers(fieldSetMapperMap);
		logger.debug(String.format("Line mapper [%s] created successfully", lineMapper));
		
		Resource resource = null;
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		
		// Retrieve back the input file path that saved in previous step
		if(!stepExecution.getJobExecution().getExecutionContext().containsKey(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)) {
			logger.warn("No input file generated in previous step, skip the ItemReader");
			// Pass in empty file for the read to process so that it don't break the entire job
			resource = resolver.getResource(EMTPY_IBK_BILLER_PAYMENT_FILE_PATH);	
		} else {
			String inputFilePath = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
			logger.debug(String.format("Input filepath [%s] fetched from context", inputFilePath));
			resource = resolver.getResource("file:" + inputFilePath);
		}
		logger.debug(String.format("Resource [%s] used in ItemReader", resource));

		// Create the reader that hold the linemapper and also target with the input file
		FlatFileItemReader<BatchStagedIBKPaymentTxn> reader = new FlatFileItemReader<>();
		reader.setLineMapper(lineMapper);
		reader.setResource(resource);
		
		logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
        return reader;
	}
	
	@Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<BatchStagedIBKPaymentTxn, BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxnItemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
        return new ItemProcessor<BatchStagedIBKPaymentTxn, BatchStagedIBKPaymentTxn>() {
			@Override
			public BatchStagedIBKPaymentTxn process(BatchStagedIBKPaymentTxn batchStagedIBKPaymentTxn) throws Exception {
				// Header condition
				if (batchStagedIBKPaymentTxn instanceof BatchStagedIBKPaymentTxnHeader) {
					processBatchStagedIBKPaymentTxnHeader((BatchStagedIBKPaymentTxnHeader)batchStagedIBKPaymentTxn, stepExecution);
				// Detail condition
				} else if (batchStagedIBKPaymentTxn instanceof BatchStagedIBKPaymentTxnDetail) {
					BatchStagedIBKPaymentTxnDetail detail = (BatchStagedIBKPaymentTxnDetail)batchStagedIBKPaymentTxn;
					processBatchStagedIBKPaymentTxnDetail(detail, stepExecution);
					logger.trace(String.format("Return BatchStagedIBKPaymentTxnDetail [%s] with updated header info", detail));
					return batchStagedIBKPaymentTxn;
				} else {
					BatchStagedIBKPaymentTxnTrailer trailer = (BatchStagedIBKPaymentTxnTrailer)batchStagedIBKPaymentTxn;
					logger.trace(String.format("BatchStagedIBKPaymentTxnTrailer [%s]", trailer));
				}
				
				return null;
			}
        };
    }

	private void processBatchStagedIBKPaymentTxnHeader(BatchStagedIBKPaymentTxnHeader header, StepExecution stepExecution) throws BatchException {
		logger.trace(String.format("BatchStagedIBKPaymentTxnHeader [%s]", header));
		
		// Validating the header process date
		String processDate = header.getProcessDate();
		try {
			DateUtils.getDateFromString(processDate, General.COMMON_DATE_DATA_FORMAT);
		} catch(ParseException e) {
			String errorMessage = String.format("Error happened while validating header process date [%s] using format [%s]", processDate, General.COMMON_DATE_DATA_FORMAT);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE, e);
		}
		
		// Storing necessary header information to context for reference later in detail section
		logger.trace(String.format("Storing header ProcessDate [%s] BillerAccountName [%s] BillerAccountNo. [%s] to context for reference in detail later", 
			processDate, header.getBillerAccountName(), header.getBillerAccountNo()));
		stepExecution.getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_PROCESS_DATE, processDate);
		stepExecution.getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NAME, header.getBillerAccountName());
		stepExecution.getExecutionContext().putString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NO, header.getBillerAccountNo());
	}
	
	private void processBatchStagedIBKPaymentTxnDetail(BatchStagedIBKPaymentTxnDetail detail, StepExecution stepExecution) throws BatchException {
		logger.trace(String.format("Original BatchStagedIBKPaymentTxnDetail [%s]", detail));
		
		// Validating the detail transaction time
		String txnTime = detail.getTxnTime();
		try {
			logger.trace(String.format("Validating detail TxnTime [%s] against format [%s]", txnTime, TXN_TIME_FORMAT));
			DateUtils.getDateFromString(txnTime, TXN_TIME_FORMAT);
		} catch(ParseException e) {
			String errorMessage = String.format("Error happened while validating detail txn time [%s] using format [%s]", txnTime, General.COMMON_DATE_DATA_FORMAT);
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE, e);
		}
		
		String billerProcessDate = stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_PROCESS_DATE);
		String billerAccountName = stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NAME);
		String billerAccountNo = stepExecution.getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_ACCOUNT_NO);
		logger.trace(String.format("Header ProcessDate [%s] BillerAccountName [%s] BillerAccountNo. [%s] fetched from context", 
				billerProcessDate, billerAccountName, billerAccountNo));
		
		// Setting the header information to the detail object which will be inserted to DB later
		detail.setProcessDate(billerProcessDate);
		detail.setBillerAccountName(billerAccountName);
		detail.setBillerAccountNo(billerAccountNo);
	}
	
    @Bean(STEP_NAME + ".ItemWriter")
    @StepScope
    public ItemWriter<BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxnItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
    	logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
        return new ItemWriter<BatchStagedIBKPaymentTxn>() {
			@Override
			public void write(List<? extends BatchStagedIBKPaymentTxn> batchStagedIBKPaymentTxns) throws Exception {
				String inputFilePath = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
				logger.debug(String.format("Fetching input file path [%s] from context", inputFilePath));
				String fileName = new File(inputFilePath).getName();
				String billerCode = stepExecution.getJobExecution().getExecutionContext().getString(BatchJobContextParameter.LOADIBKBILLERPAYMENT_BILLER_CODE);
				logger.debug(String.format("Fetching biller code [%s] from context", billerCode));
				
				for(BatchStagedIBKPaymentTxn batchStagedIBKPaymentTxn : batchStagedIBKPaymentTxns) {
					BatchStagedIBKPaymentTxnDetail batchStagedIBKPaymentTxnDetail = (BatchStagedIBKPaymentTxnDetail)batchStagedIBKPaymentTxn;
					batchStagedIBKPaymentTxnDetail.setJobExecutionId(stepExecution.getJobExecution().getId().intValue());
					batchStagedIBKPaymentTxnDetail.setFileName(fileName);
					batchStagedIBKPaymentTxnDetail.setCreatedTime(new Date());
					batchStagedIBKPaymentTxnDetail.setBillerCode(billerCode);
					
					logger.trace(String.format("Inserting BatchStagedIBKPaymentTxn object [%s] to DB", batchStagedIBKPaymentTxnDetail));
					batchStagedIBKPaymentTxnRepository.addBatchStagedIBKPaymentTxnToStaging(batchStagedIBKPaymentTxnDetail);
				}
			}
		};
    }
	
	@Override
	@Bean
	public Step buildStep() {
		logger.info(String.format("Building step [%s]", STEP_NAME));
		
		Step step = getDefaultStepBuilder(STEP_NAME)
				.<BatchStagedIBKPaymentTxn,BatchStagedIBKPaymentTxn>chunk(configProperties.getChunkSize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
		
		logger.info(String.format("[%s] step build successfully", STEP_NAME));
		return step;
	}
	
}