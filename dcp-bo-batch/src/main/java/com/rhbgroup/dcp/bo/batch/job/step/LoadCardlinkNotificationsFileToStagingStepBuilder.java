package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
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
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadCardlinkNotificationsJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.LoadCardlinkNotificationDetailFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.LoadCardlinkNotificationHeaderFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.LoadCardlinkNotificationTrailerFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotificationRaw;
import com.rhbgroup.dcp.bo.batch.job.model.LoadCardlinkNotification;
import com.rhbgroup.dcp.bo.batch.job.model.LoadCardlinkNotificationDetail;
import com.rhbgroup.dcp.bo.batch.job.model.LoadCardlinkNotificationHeader;
import com.rhbgroup.dcp.bo.batch.job.model.LoadCardlinkNotificationTrailer;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedNotificationRawRepositoryImpl;

@Component
@Lazy
public class LoadCardlinkNotificationsFileToStagingStepBuilder extends BaseStepBuilder{
	
	static final Logger logger = Logger.getLogger(LoadCardlinkNotificationsFileToStagingStepBuilder.class);
	static final String STEP_NAME="LoadCardlinkNotificationsFileToStagingStep";
	
	private long sumHash;
	private int recordCount;
	private String systemDateHeader;
	private String systemTimeHeader;
	private String eventCode;
	private String keyType;
	private Date processDate;
	private List<LoadCardlinkNotificationDetail> loadCardlinkNotificationDetails = new ArrayList<>();
	private boolean isPassValidation = false;
	
	@Autowired
	LoadCardlinkNotificationsJobConfigProperties configProperties;
	
	@Autowired
	BatchStagedNotificationRawRepositoryImpl batchNotificationRawRepository;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<LoadCardlinkNotification> itemReader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<LoadCardlinkNotification, LoadCardlinkNotification> itemProcessor;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<LoadCardlinkNotification> itemWriter;
	
    @Bean(STEP_NAME + ".ItemReader")
    @StepScope
    public FlatFileItemReader<LoadCardlinkNotification> cardlinkNotificationReader(@Value("#{stepExecution}") StepExecution stepExecution) throws BatchException {
		LineTokenizer headerTokenizer = BatchUtils.getFixedLengthWithOpenRangeTokenizer(configProperties.getHeaderNames(), configProperties.getHeaderColumns());
		LineTokenizer detailTokenizer = BatchUtils.getDelimiterTokenizer(configProperties.getDetailNames(), configProperties.getDetailDelimiter() );
		LineTokenizer trailerTokenizer = BatchUtils.getFixedLengthWithOpenRangeTokenizer(configProperties.getTrailerNames(), configProperties.getTrailerColumns());
		
		logger.info(String.format("ItemReader [%s.ItemReader] created LineTokenizer", STEP_NAME));

		String headerPrefixPattern  = configProperties.getHeaderPrefix() + "*";
		String detailPrefixPattern  = configProperties.getDetailPrefix() + "*";
		String trailerPrefixPattern  = configProperties.getTrailerPrefix() + "*";
		
		logger.info(String.format("ItemReader [%s.ItemReader] created pattern", STEP_NAME));

		Map<String, LineTokenizer> tokenizerMap = new HashMap<>();
		tokenizerMap.put(headerPrefixPattern, headerTokenizer);
		tokenizerMap.put(detailPrefixPattern, detailTokenizer);
		tokenizerMap.put(trailerPrefixPattern, trailerTokenizer);
		
		logger.info(String.format("ItemReader [%s.ItemReader] created tokenizer Map", STEP_NAME));

		FieldSetMapper<LoadCardlinkNotification> headerMapper = new LoadCardlinkNotificationHeaderFieldSetMapper();
		FieldSetMapper<LoadCardlinkNotification> detailMapper = new LoadCardlinkNotificationDetailFieldSetMapper();
		FieldSetMapper<LoadCardlinkNotification> trailerMapper = new LoadCardlinkNotificationTrailerFieldSetMapper();
		
		logger.info(String.format("ItemReader [%s.ItemReader] created mapper header,detail,trailer", STEP_NAME));
		
		Map<String, FieldSetMapper<LoadCardlinkNotification>> fieldSetMapperMap = new HashMap<>();
		fieldSetMapperMap.put(headerPrefixPattern, headerMapper);
		fieldSetMapperMap.put(detailPrefixPattern, detailMapper);
		fieldSetMapperMap.put(trailerPrefixPattern, trailerMapper);

		PatternMatchingCompositeLineMapper<LoadCardlinkNotification> lineMapper = new PatternMatchingCompositeLineMapper<>();
		lineMapper.setTokenizers(tokenizerMap);
		lineMapper.setFieldSetMappers(fieldSetMapperMap);
		
        FlatFileItemReader<LoadCardlinkNotification> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource((String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)));
		reader.setLineMapper(lineMapper);
		
		logger.info(String.format("ItemReader [%s.ItemReader] created succesfully", STEP_NAME));
    	recordCount=0;
        sumHash=0;
        processDate = getProcessDate(stepExecution, configProperties.getDayDiff(), ChronoUnit.DAYS);
        return reader;
    }
    
    @Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<LoadCardlinkNotification, LoadCardlinkNotification> cardlinkNotificationProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        return new ItemProcessor<LoadCardlinkNotification, LoadCardlinkNotification>() {
            @Override
			public LoadCardlinkNotification process(LoadCardlinkNotification cardlinkNotification) throws Exception {
            	if(cardlinkNotification instanceof LoadCardlinkNotificationHeader) {
            		logger.debug("processing header");
            		processCardlinkNotificationHeader((LoadCardlinkNotificationHeader) cardlinkNotification);
            	}else if(cardlinkNotification instanceof LoadCardlinkNotificationDetail) {
            		logger.debug("processing detail");
            		LoadCardlinkNotificationDetail detail= processCardlinkNotificationDetail((LoadCardlinkNotificationDetail)cardlinkNotification);
            		if(Objects.isNull(detail)) {
            			logger.info("detail is null");
    					stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FILE_VALIDATION_ERROR, "Error while validating detail record" ));
            		}
            		loadCardlinkNotificationDetails.add(detail);
            		return detail;
            	}else if(cardlinkNotification instanceof LoadCardlinkNotificationTrailer) {
            		logger.debug("processing trailer");
            		processCardlinkNotificationTrailer((LoadCardlinkNotificationTrailer)cardlinkNotification);
            		return cardlinkNotification;
            	}
				return null;
			}
        };
    }
    
    @Bean(STEP_NAME + ".ItemWriter")
    @StepScope
	public ItemWriter<LoadCardlinkNotification> cardlinkNotificationWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
		return new ItemWriter<LoadCardlinkNotification>() {
			@Override
			public void write(List<? extends LoadCardlinkNotification> loadCardlinkNotificationList) throws Exception {
				long jobExecutionId = stepExecution.getJobExecution().getId();
				String fileFullPath = stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
				String fileName = new File(fileFullPath).getName();
				
				logger.info(String.format("Record into DB job exec id=%s ,file name=%s", jobExecutionId, fileName));
				logger.info(String.format("LoadCardlinkNotificationDetails size=%s", loadCardlinkNotificationDetails .size()));
				logger.info(String.format("isPassValidation=%s", isPassValidation));
				if(isPassValidation) {
					List<BatchStagedNotificationRaw> rawNotfications= new ArrayList<>();
            		for (LoadCardlinkNotificationDetail detail : loadCardlinkNotificationDetails) {
    					BatchStagedNotificationRaw notificationRaw = createBatchNotificationRaw(detail);
    					notificationRaw.setJobExecutionId(jobExecutionId);
    					notificationRaw.setFileName(fileName);
    					logger.debug(String.format("Inserting Batch Staged Notification Raw Record job exec id=%s, object [%s] to DB",
    							jobExecutionId, notificationRaw));
    					rawNotfications.add(notificationRaw);
    				}
            		if(rawNotfications.size() > 0) {
            			int inserted = batchNotificationRawRepository.addBatchNotificationRaw(rawNotfications);
        				logger.info(String.format("Batch Insert notification raw %s", inserted));
            		}
				}
			}
		};
	}
    
	private BatchStagedNotificationRaw createBatchNotificationRaw(LoadCardlinkNotificationDetail detail) {
		BatchStagedNotificationRaw notificationRaw = new BatchStagedNotificationRaw();
		notificationRaw.setProcessDate(DateUtils.formatDateString(processDate, DEFAULT_JOB_PARAMETER_DATE_FORMAT));
		notificationRaw.setEventCode(eventCode);
		notificationRaw.setKeyType(keyType);
		notificationRaw.setData1(systemDateHeader);
		notificationRaw.setData2(systemTimeHeader);
		notificationRaw.setData3(StringUtils.stripStart(detail.getCreditCard(), "0"));
		notificationRaw.setData4(detail.getPaymentDueDate());
		notificationRaw.setData5(detail.getCreditCardType());
		notificationRaw.setData6(detail.getMinAmount());
		notificationRaw.setData7(detail.getOutstandingAmount());
		notificationRaw.setData8(detail.getStatementAmount());
		notificationRaw.setData9(detail.getStatementDate());
		notificationRaw.setProcessed(false);
		notificationRaw.setCreatedTime(new Date());
		notificationRaw.setCreatedBy(configProperties.getBatchCode());
		notificationRaw.setUpdatedTime(new Date());
		notificationRaw.setUpdatedBy(configProperties.getBatchCode());
		return notificationRaw;
	}
    
    private LoadCardlinkNotificationHeader processCardlinkNotificationHeader(LoadCardlinkNotificationHeader header) throws BatchException{
    	logger.info( String.format("processing record header %s", header) );
    	if(!header.getEventCode().equalsIgnoreCase(configProperties.getEventCode())) {
    		logger.error(String.format("Header contains invalid event_code=%s", header.getEventCode()));
    		throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, "Invalid event_code");    		
    	}
    	eventCode = header.getEventCode();
    	keyType = header.getKeyType();
    	systemDateHeader = header.getSystemDate();
    	systemTimeHeader = header.getSystemTime();
    	return null;
    }
    
    private LoadCardlinkNotificationDetail processCardlinkNotificationDetail(LoadCardlinkNotificationDetail detail){
    	recordCount ++;
    	logger.debug( String.format("processing record=%s, credit_card=%s, outstanding amount=%s",recordCount, detail.getCreditCard(), detail.getOutstandingAmount()));
    	if(!StringUtils.isNumeric(detail.getCreditCard())) {
    		logger.warn(String.format("credit_card not numeric %s", detail.getCreditCard()));
    		return null;
    	}
    	
    	try {
    		Date paymentDueDate = DateUtils.getDateFromString(detail.getPaymentDueDate(), DEFAULT_JOB_PARAMETER_DATE_FORMAT);
    		logger.info("paymentDueDate="+paymentDueDate+"-detail.getPaymentDueDate()="+detail.getPaymentDueDate() );
    		long last5Digit = Long.parseLong( StringUtils.right(detail.getCreditCard(), 5));
    		double outstandingAmt = Double.parseDouble(detail.getOutstandingAmount());
    		if(!computeHash(last5Digit,outstandingAmt)) {
    			return null;
    		}
    	}catch( NumberFormatException ex ) {
    		logger.warn(String.format("Exception while validating detail %s", ex ));
    		return null;
    	} catch (ParseException ex) {
    		logger.warn(String.format("Exception while parsing payment due date %s", ex ));
    		return null;
		}
    	return detail;
    }
    
    private void processCardlinkNotificationTrailer(LoadCardlinkNotificationTrailer trailer) throws BatchException {
    	try {
	    	int tHashValue = Integer.parseInt( trailer.getHashValue());
	    	int tRecordCount = Integer.parseInt(trailer.getRecordCount());
	    	int finalSumHash = (int)sumHash;
	    	if(tRecordCount!= recordCount) {
	    		logger.error(String.format("Total record count %s not matched trailer count=%s",  recordCount, tRecordCount ));
	    		throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
	    	}
	    	if(finalSumHash!=tHashValue) {
	    		logger.error(String.format("Hash value %s not matched trailer hash value=%s",  finalSumHash, tHashValue ));
	    		throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE);
	    	}
	    	isPassValidation = true;
    	}catch( Exception ex ) {
    		logger.error(String.format("Exception while validating detail %s",ex));
    		throw new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, BatchErrorCode.FIELD_VALIDATION_ERROR_MESSAGE, ex);
    	}
    }
	
	private boolean computeHash( long last5Digit, double outstandingAmt ) {
		boolean computed=false;
		try {
			int quotient = BigDecimal.valueOf(last5Digit/outstandingAmt).setScale(10, RoundingMode.HALF_UP).intValue();
			double newAmt = quotient * outstandingAmt;
			double result = last5Digit - Double.parseDouble(String.format("%.2f", newAmt));
			int hash = (int)Math.floor(result);
			sumHash += hash;
			computed=true;
		}catch(Exception ex) {
    		logger.error(String.format("Exception while computing hash %s",ex));
    		return computed;
		}
		return computed;
	}

	@Override
	@Bean(name = STEP_NAME)
	public Step buildStep() {
        return getDefaultStepBuilder(STEP_NAME).<LoadCardlinkNotification,LoadCardlinkNotification>chunk(configProperties.getChunkSize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
	}

}
