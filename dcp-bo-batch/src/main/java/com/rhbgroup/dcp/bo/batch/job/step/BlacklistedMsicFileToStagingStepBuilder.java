package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.BlacklistedMsicJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BlacklistedDetailFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BlacklistedHeaderFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BlacklistedMsicTrailerFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedMsicConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BlacklistedMsic;
import com.rhbgroup.dcp.bo.batch.job.model.BlacklistedMsicDetail;
import com.rhbgroup.dcp.bo.batch.job.model.BlacklistedMsicHeader;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedMsicConfigRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.MsicConfigRepositoryImpl;

@Component
@Lazy
public class BlacklistedMsicFileToStagingStepBuilder extends BaseStepBuilder {

	static final Logger logger = Logger.getLogger(BlacklistedMsicFileToStagingStepBuilder.class);
	static final String STEP_NAME = "BlacklistedMsicFileToStagingStep";
	private int recordCount = 0;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private List<BlacklistedMsicDetail> blacklistedMsicDetails = new ArrayList<>();
	private boolean isPassValidation = false;
	private Timestamp processDate;
	private Timestamp now = new Timestamp(System.currentTimeMillis());
	@Autowired
	private BlacklistedMsicJobConfigProperties blacklistedMsicConfigProperties;

	@Autowired
	BatchStagedMsicConfigRepositoryImpl batchStagedMsicConfigRepositoryImpl;
	@Autowired
	MsicConfigRepositoryImpl msicConfigRepositoryImpl;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BlacklistedMsic> itemReader;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BlacklistedMsic, BlacklistedMsic> itemProcessor;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BlacklistedMsic> itemWriter;

	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	public FlatFileItemReader<BlacklistedMsic> loadBlacklistedMsicReader(
			@Value("#{stepExecution}") StepExecution stepExecution) {

		LineTokenizer headerTokenizer = BatchUtils.getDelimiterTokenizer(
				blacklistedMsicConfigProperties.getHeadernames(),
				blacklistedMsicConfigProperties.getHeaderDelimiter());
		LineTokenizer detailTokenizer = BatchUtils.getDelimiterTokenizer(
				blacklistedMsicConfigProperties.getDetailnames(),
				blacklistedMsicConfigProperties.getDetailDelimiter());
		LineTokenizer trailerTokenizer = BatchUtils.getDelimiterTokenizer(
				blacklistedMsicConfigProperties.getTrailernames(),
				blacklistedMsicConfigProperties.getTrailerDelimiter());

		logger.info(String.format("ItemReader [%s] created LineTokenizer", STEP_NAME));

		String headerPrefixPattern = blacklistedMsicConfigProperties.getHeaderprefix() + "*";
		String detailPrefixPattern = blacklistedMsicConfigProperties.getDetailprefix() + "*";
		String trailerPrefixPattern = blacklistedMsicConfigProperties.getTrailerprefix() + "*";
		logger.info(String.format("ItemReader [%s] created pattern", STEP_NAME));

		Map<String, LineTokenizer> tokenizerMap = new HashMap<>();
		tokenizerMap.put(headerPrefixPattern, headerTokenizer);
		tokenizerMap.put(detailPrefixPattern, detailTokenizer);
		tokenizerMap.put(trailerPrefixPattern, trailerTokenizer);
		logger.info(String.format("ItemReader [%s] created tokenizer Map", STEP_NAME));

		FieldSetMapper<BlacklistedMsic> headerMapper = new BlacklistedHeaderFieldSetMapper();
		FieldSetMapper<BlacklistedMsic> detailMapper = new BlacklistedDetailFieldSetMapper();
		FieldSetMapper<BlacklistedMsic> trailerMapper = new BlacklistedMsicTrailerFieldSetMapper();
		logger.info(String.format("ItemReader [%s] created mapper header,detail,trailer", STEP_NAME));

		Map<String, FieldSetMapper<BlacklistedMsic>> fieldSetMapperMap = new HashMap<>();
		fieldSetMapperMap.put(headerPrefixPattern, headerMapper);
		fieldSetMapperMap.put(detailPrefixPattern, detailMapper);
		fieldSetMapperMap.put(trailerPrefixPattern, trailerMapper);

		PatternMatchingCompositeLineMapper<BlacklistedMsic> lineMapper = new PatternMatchingCompositeLineMapper<>();
		lineMapper.setTokenizers(tokenizerMap);
		lineMapper.setFieldSetMappers(fieldSetMapperMap);

		FlatFileItemReader<BlacklistedMsic> reader = new FlatFileItemReader<>();
		reader.setResource(new FileSystemResource((String) stepExecution.getJobExecution().getExecutionContext()
				.get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)));
		reader.setLineMapper(lineMapper);
		logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
		return reader;
	}

	@Bean(STEP_NAME + ".ItemProcessor")
	@StepScope
	public ItemProcessor<BlacklistedMsic, BlacklistedMsic> loadBlacklistedMsicProcessor(
			@Value("#{stepExecution}") StepExecution stepExecution) {
		return new ItemProcessor<BlacklistedMsic, BlacklistedMsic>() {
			@Override
			public BlacklistedMsic process(BlacklistedMsic blacklistedMsic) throws Exception {
				if (blacklistedMsic instanceof BlacklistedMsicHeader) {
					logger.debug("processing header");
					processLoadBlacklistedMsicHeader((BlacklistedMsicHeader) blacklistedMsic);
				}
				if (blacklistedMsic instanceof BlacklistedMsicDetail) {
					logger.debug("processing detail");
					BlacklistedMsicDetail detail = processLoadBlacklistedMsicDetail(
							(BlacklistedMsicDetail) blacklistedMsic);
					blacklistedMsicDetails.add(detail);
					return detail;
				}
				return null;

			}

			private BlacklistedMsicHeader processLoadBlacklistedMsicHeader(
					BlacklistedMsicHeader blacklistedMsicHeader)  {
				logger.info(String.format("processing record header %s", blacklistedMsicHeader));
				try {
					Date parsedDate = dateFormat.parse(blacklistedMsicHeader.getCreationDate());
					processDate = new java.sql.Timestamp(parsedDate.getTime());
				} catch (ParseException e) {
					logger.warn(e);
				}

				return blacklistedMsicHeader;

			}
		};
	}

	private BlacklistedMsicDetail processLoadBlacklistedMsicDetail(BlacklistedMsicDetail blacklistedMsicDetail)
			 {
		recordCount++;
		logger.debug(String.format("processing record=%s, msic code=%s, description=%s", recordCount,
				blacklistedMsicDetail.getMsicCode(), blacklistedMsicDetail.getDescription()));
		isPassValidation = true;
		return blacklistedMsicDetail;
	}

	@Bean(STEP_NAME + ".ItemWriter")
	@StepScope
	public ItemWriter<BlacklistedMsic> loadBlacklistedMsicWriter(
			@Value("#{stepExecution}") StepExecution stepExecution) {
		return new ItemWriter<BlacklistedMsic>() {
			@Override
			public void write(List<? extends BlacklistedMsic> blacklistedMsicList) throws Exception {
				Long jobExecutionId = stepExecution.getJobExecution().getId();
				String fileFullPath = stepExecution.getJobExecution().getExecutionContext()
						.getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
				String fileName = new File(fileFullPath).getName();

				logger.info(String.format("Record into DB job exec id=%s ,file name=%s", jobExecutionId, fileName));
				logger.info(String.format("BlacklistedMsicDetails size=%s", blacklistedMsicDetails.size()));
				logger.info(String.format("isPassValidation=%s", isPassValidation));
				if (isPassValidation) {
					List<BatchStagedMsicConfig> batchStagedMsicConfigs = new ArrayList<>();
					for (BlacklistedMsicDetail blacklistedMsicDetail : blacklistedMsicDetails) {
						BatchStagedMsicConfig batchStagedMsicConfig = createBatchStageMsicConfig(
								blacklistedMsicDetail);
						batchStagedMsicConfig.setJobExecutionId(jobExecutionId);
						batchStagedMsicConfig.setFileName(fileName);
						logger.debug(String.format(
								"Inserting Batch Staged Notification Raw Record job exec id=%s, object [%s] to DB",
								jobExecutionId, batchStagedMsicConfig));
						batchStagedMsicConfigs.add(batchStagedMsicConfig);
					}
					if (!batchStagedMsicConfigs.isEmpty()) {
						int deleted = batchStagedMsicConfigRepositoryImpl.deleteBatchStageMsicConfig();
						logger.debug("Deleted batchstaged msic config"+deleted);
						int inserted = batchStagedMsicConfigRepositoryImpl
								.addBatchStageMsicConfig(batchStagedMsicConfigs);
						logger.debug("added batchstaged msic config"+inserted);
					}

				}
			}
		};
	}

	private BatchStagedMsicConfig createBatchStageMsicConfig(BlacklistedMsicDetail blacklistedMsicDetail) {
		BatchStagedMsicConfig batchStagedMsicConfig = new BatchStagedMsicConfig();
		batchStagedMsicConfig.setMsicId(blacklistedMsicDetail.getMsicCode());
		batchStagedMsicConfig.setMsic(blacklistedMsicDetail.getMsic());
		batchStagedMsicConfig.setDescription(blacklistedMsicDetail.getDescription());
		batchStagedMsicConfig.setAccountType(blacklistedMsicDetail.getAccountType());

		if(blacklistedMsicDetail.getIslamicIndicator().equals("Y")){
			batchStagedMsicConfig.setIslamicCompliance(Boolean.TRUE);

		}else {
			batchStagedMsicConfig.setIslamicCompliance(Boolean.FALSE);
		}

		if(blacklistedMsicDetail.getStatus()!=null) {
			batchStagedMsicConfig.setStatus(blacklistedMsicDetail.getStatus());
		}else {
			batchStagedMsicConfig.setStatus(" ");
		}
		batchStagedMsicConfig.setProcessed(Boolean.TRUE);
		batchStagedMsicConfig.setProcessDate(processDate);
		batchStagedMsicConfig.setCreatedTime(now);
		return batchStagedMsicConfig;
	}

	@Override
	@Bean(name = STEP_NAME)
	public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME)
				.<BlacklistedMsic, BlacklistedMsic>chunk(blacklistedMsicConfigProperties.getChunkSize())
				.reader(itemReader).processor(itemProcessor).writer(itemWriter).build();

	}
}