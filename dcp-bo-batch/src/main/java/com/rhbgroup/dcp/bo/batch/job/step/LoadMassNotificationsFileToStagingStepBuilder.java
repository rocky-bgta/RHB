package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobPropertyFile.BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;
import static java.lang.Long.parseLong;

import java.nio.file.Paths;
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
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadMassNotificationsJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.LoadMassNotificationsDetailMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.LoadMassNotificationsHeaderMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedNotifMass;
import com.rhbgroup.dcp.bo.batch.job.model.LoadMassNotifications;
import com.rhbgroup.dcp.bo.batch.job.model.LoadMassNotificationsDetail;
import com.rhbgroup.dcp.bo.batch.job.model.LoadMassNotificationsHeader;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedNotifMassRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.UserProfileRepositoryImpl;

@Component
@Lazy
public class LoadMassNotificationsFileToStagingStepBuilder extends BaseStepBuilder {
	
	private static final Logger logger = Logger.getLogger(LoadMassNotificationsFileToStagingStepBuilder.class);
	
	private static final String STEP_NAME = "LoadMassNotificationsFileToStagingStep";
	
	private static final String FILE_FOLDER = "dcp_mass_notification_from";
	
	private static final String BATCH_EXECUTION_CONTEXT_INPUT_FILE_FULL_PATH = "inputFolderFullPath";
	
	private static final String BATCH_JOB_PARAMETER_JOB_BATCH_EVENT_CODE_KEY = "eventcode";
	
	private static final int EVENT_CODE_LENGTH = 5;
	
	private static final int MASS_NOTIFICATION_CHARACTERS = 145;
	
	@Value(BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY)
	private String sourceFilePath;
	
	private String sourceFileName;
	
	private String eventCode;
	
	private String content;
	
	@Autowired
	private LoadMassNotificationsJobConfigProperties configProperties;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private FlatFileItemReader<LoadMassNotifications> itemReader;
	
	@Autowired
    @Qualifier(STEP_NAME + ".ItemProcessor")
    private ItemProcessor<LoadMassNotifications, LoadMassNotifications> itemProcessor;
    
	@Autowired
    @Qualifier(STEP_NAME + ".ItemWriter")
    private ItemWriter<LoadMassNotifications> itemWriter;
	
	@Autowired
	private BatchStagedNotifMassRepositoryImpl batchStagedNotifMassRepositoryImpl;
	
	@Autowired
	private UserProfileRepositoryImpl userProfileRepositoryImpl;
	
	// ItemReader
	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	public FlatFileItemReader<LoadMassNotifications> loadMassNotificationsItemReader(@Value("#{stepExecution}") StepExecution stepExecution) throws BatchException {
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		
		// Create the tokenizer to parse specific line in the file, e.g. header, detail
		LineTokenizer headerTokenizer = BatchUtils.getFixedLengthTokenizer(configProperties.getHeaderNames(), configProperties.getHeaderColumns());
		LineTokenizer detailTokenizer = BatchUtils.getDelimiterTokenizer(configProperties.getDetailNames(), configProperties.getDetailDelimiter());
		
		String headerPrefixPattern  = configProperties.getHeaderPrefix() + "*";
		String detailPrefixPattern  = configProperties.getDetailPrefix() + "*";
		
		Map<String, LineTokenizer> tokenizerMap = new HashMap<>();
		tokenizerMap.put(headerPrefixPattern, headerTokenizer);
		tokenizerMap.put(detailPrefixPattern, detailTokenizer);
		logger.debug(String.format("Tokenizer map [%s] created successfully", tokenizerMap));
		
		// Create the fieldmappers to map the line details to object for haeder, detail
		FieldSetMapper<LoadMassNotifications> headerMapper = new LoadMassNotificationsHeaderMapper();
		FieldSetMapper<LoadMassNotifications> detailMapper = new LoadMassNotificationsDetailMapper();
		
		Map<String, FieldSetMapper<LoadMassNotifications>> fieldSetMapperMap = new HashMap<>();
		fieldSetMapperMap.put(headerPrefixPattern, headerMapper);
		fieldSetMapperMap.put(detailPrefixPattern, detailMapper);
		logger.debug(String.format("FieldSetMapper map [%s] created successfully", fieldSetMapperMap));
		
		// Create the linemapper that hold the tokenizers and fieldmappers
		PatternMatchingCompositeLineMapper<LoadMassNotifications> lineMapper = new PatternMatchingCompositeLineMapper<>();
		lineMapper.setTokenizers(tokenizerMap);
		lineMapper.setFieldSetMappers(fieldSetMapperMap);
		logger.debug(String.format("Line mapper [%s] created successfully", lineMapper));
		
		// Default current date
		Date processDate = getProcessDate(stepExecution, 0, null);
		
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		sourceFileName = configProperties.getName();
		String sourceFileNameNew = sourceFileName.replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, 
				DateUtils.formatDateString(processDate, configProperties.getNameDateFormat()));			
		String inputFolderFullPath = Paths.get(sourceFilePath, FILE_FOLDER, sourceFileNameNew).toAbsolutePath().toString();
		Resource resource = resolver.getResource("file:" + inputFolderFullPath);
		logger.debug(String.format("Resource [%s] used in ItemReader", resource));
		
		// Replace sourceFileName with sourceFileNameNew
		sourceFileName = sourceFileNameNew;
		stepExecution.getJobExecution().getExecutionContext().putString(BATCH_EXECUTION_CONTEXT_INPUT_FILE_FULL_PATH, inputFolderFullPath);
		
		// Create the reader that hold the linemapper and also target with the input file
		FlatFileItemReader<LoadMassNotifications> reader = new FlatFileItemReader<>();
		reader.setLineMapper(lineMapper);
		reader.setResource(resource);
		
		logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
		
		return reader;
	}
	
	// ItemProcessor
	@Bean(STEP_NAME + ".ItemProcessor")
	@StepScope
	public ItemProcessor<LoadMassNotifications, LoadMassNotifications> loadMassNotificationsItemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
		return new ItemProcessor<LoadMassNotifications, LoadMassNotifications>() {

			@Override
			public LoadMassNotifications process(LoadMassNotifications item) throws Exception {
				if (item instanceof LoadMassNotificationsHeader) { // Header
					int eventCodeLength = 0;
					if (null != stepExecution.getJobParameters().getParameters().get(BATCH_JOB_PARAMETER_JOB_BATCH_EVENT_CODE_KEY)) {
						eventCodeLength = stepExecution.getJobParameters().getParameters().get(BATCH_JOB_PARAMETER_JOB_BATCH_EVENT_CODE_KEY).getValue().toString().length();
					} else if (null != ((LoadMassNotificationsHeader) item).getEventCode()) {
						eventCodeLength = ((LoadMassNotificationsHeader) item).getEventCode().length();
					}
					// Check if eventCode length is 5-digit
					if (eventCodeLength == EVENT_CODE_LENGTH) {
						processLoadMassNotificationsHeader((LoadMassNotificationsHeader) item, stepExecution);
					} else {
						stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, "Event code length not met."));
					}
				} else { // Details
					processLoadMassNotificationsDetail((LoadMassNotificationsDetail) item, stepExecution);
					// Validating content length
					// If exceed MASS_NOTIFICATION_CHARACTERS then add failure
					if (content.length() > MASS_NOTIFICATION_CHARACTERS) {
						stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.FIELD_VALIDATION_ERROR, 
								"Error while validating detail record. Content exceed 145 characters."));
					} else {
						return item;
					}
				}
				return null;
			}
			
		};
	}
	
	// ItemWriter
	@Bean(STEP_NAME + ".ItemWriter")
	@StepScope
	public ItemWriter<LoadMassNotifications> loadMassNotificationsItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
		return new ItemWriter<LoadMassNotifications>() {

			@Override
			public void write(List<? extends LoadMassNotifications> items) throws Exception {
				int inserted = 0;
				String action = "reading";
				String table = "TBL_USER_PROFILE";
				
				// Check if USER_STATUS='A' from TBL_USER_PROFILE
				try {
					List<Map<String, Object>> activeUserProfilesList = userProfileRepositoryImpl.getActiveUserProfiles();
					
					if (activeUserProfilesList.size() > 0) {
						// Insert into TBL_BATCH_STAGED_NOTIF_MASS
						for (Map<String, Object> userProfile : activeUserProfilesList) {
							long jobExecutionId = stepExecution.getJobExecution().getId();
							long userProfileId = parseLong(userProfile.get("ID").toString());
							BatchStagedNotifMass batchStagedNotifMass = createBatchStagedNotifMass(jobExecutionId, userProfileId);
							logger.trace(String.format("Inserting BatchStagedNotifMass object [%s] to DB", batchStagedNotifMass));
							action = "inserting";
							table = "TBL_BATCH_STAGED_NOTIF_MASS";
							inserted += batchStagedNotifMassRepositoryImpl.addRecordBatchStagedNotifMass(batchStagedNotifMass);
						}
					}
				} catch(BatchException be) {
					// If exception happened, don't stop the batch, just proceed to next record until all complete processed
					String errorMessage = String.format("Error happened while [%s] record in DB [%s]", action, table);
					logger.error(errorMessage, be);
					stepExecution.getJobExecution().addFailureException(be);
				} catch(Exception e) {
					String errorMessage = String.format("Error happened while creating ItemWriter [%s]", STEP_NAME);
					logger.error(errorMessage, e);
					stepExecution.getJobExecution().addFailureException(e);
				}
				
				logger.info(String.format("Batch Insert TBL_BATCH_STAGED_NOTIF_MASS [%s]", inserted));
			}
			
		};
	}
	
	private void processLoadMassNotificationsHeader(LoadMassNotificationsHeader header, StepExecution stepExecution) {
		logger.trace(String.format("LoadMassNotificationsHeader [%s]", header));
		logger.trace(String.format("LoadMassNotificationsHeader eventCode [%s]", header.getEventCode()));
		eventCode = header.getEventCode();
		
		stepExecution.getExecutionContext().putString("eventCode", header.getEventCode());
	}
	
	private void processLoadMassNotificationsDetail(LoadMassNotificationsDetail detail, StepExecution stepExecution) {
		logger.trace(String.format("LoadMassNotificationsDetail [%s]", detail));
		logger.trace(String.format("LoadMassNotificationsDetail content length [%s]", detail.getContent().length()));
		this.content = detail.getContent();
		
		stepExecution.getExecutionContext().putString("content", detail.getContent());
	}
	
	private BatchStagedNotifMass createBatchStagedNotifMass(long jobExecutionId, long userId) throws BatchException {
		BatchStagedNotifMass batchStagedNotifMass = new BatchStagedNotifMass();
		
		batchStagedNotifMass.setJobExecutionId(jobExecutionId);
		batchStagedNotifMass.setFileName(sourceFileName);
		batchStagedNotifMass.setEventCode(eventCode);
		batchStagedNotifMass.setContent(this.content);
		batchStagedNotifMass.setUserId(userId);
		batchStagedNotifMass.setProcessed(false);
		Date now = new Date();
		batchStagedNotifMass.setCreatedTime(now);
		batchStagedNotifMass.setCreatedBy(configProperties.getBatchCode());
		batchStagedNotifMass.setUpdatedTime(now);
		batchStagedNotifMass.setUpdatedBy(configProperties.getBatchCode());
		
		return batchStagedNotifMass;
	}

	@Override
	public Step buildStep() {
		logger.info(String.format("Building step [%s]", STEP_NAME));
		
		Step step = getDefaultStepBuilder(STEP_NAME).<LoadMassNotifications, LoadMassNotifications>chunk(configProperties.getChunkSize())
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.build();
		
		logger.info(String.format("[%s] step build successfully", STEP_NAME));
		return step;
	}

}
