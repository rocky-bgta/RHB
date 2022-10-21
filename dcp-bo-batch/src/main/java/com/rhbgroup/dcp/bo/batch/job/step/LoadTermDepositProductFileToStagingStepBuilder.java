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
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadTermDepositJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.LoadTermDepositDetailFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.LoadTermDepositHeaderFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.LoadTermDepositTrailerFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedDepositProduct;
import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDeposit;
import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDepositDetail;
import com.rhbgroup.dcp.bo.batch.job.model.LoadTermDepositHeader;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedDepositProductRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.DepositProductRepositoryImpl;

@Component
@Lazy
public class LoadTermDepositProductFileToStagingStepBuilder extends BaseStepBuilder {

	static final Logger logger = Logger.getLogger(LoadTermDepositProductFileToStagingStepBuilder.class);
	static final String STEP_NAME = "LoadTermDepositFileToStagingStep";
	private int recordCount = 0;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private List<LoadTermDepositDetail> termDepositRateDetails = new ArrayList<>();
	private boolean isPassValidation = false;
	private Timestamp processDate;
	private Timestamp now = new Timestamp(System.currentTimeMillis());
	private Timestamp endDate;
	@Autowired
	private LoadTermDepositJobConfigProperties loadTermDepositJobConfigProperties;

	@Autowired
	BatchStagedDepositProductRepositoryImpl batchStagedDepositProductRepositoryImpl;
	@Autowired
	DepositProductRepositoryImpl depositProductRepositoryImpl;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<LoadTermDeposit> itemReader;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<LoadTermDeposit, LoadTermDeposit> itemProcessor;

	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<LoadTermDeposit> itemWriter;

	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	public FlatFileItemReader<LoadTermDeposit> loadTermDepositProductReader(
			@Value("#{stepExecution}") StepExecution stepExecution) throws BatchException {
		LineTokenizer headerTokenizer = BatchUtils.getFixedLengthWithOpenRangeTokenizer(
				loadTermDepositJobConfigProperties.getHeadernames(),
				loadTermDepositJobConfigProperties.getHeadercolumns());
		LineTokenizer detailTokenizer = BatchUtils.getFixedLengthWithOpenRangeTokenizer(
				loadTermDepositJobConfigProperties.getDetailnames(),
				loadTermDepositJobConfigProperties.getDetailcolumns());
		LineTokenizer trailerTokenizer = BatchUtils.getFixedLengthWithOpenRangeTokenizer(
				loadTermDepositJobConfigProperties.getTrailernames(),
				loadTermDepositJobConfigProperties.getTrailercolumns());

		logger.info(String.format("ItemReader [%s] created LineTokenizer", STEP_NAME));

		String headerPrefixPattern = loadTermDepositJobConfigProperties.getHeaderprefix() + "*";
		String detailPrefixPattern = loadTermDepositJobConfigProperties.getDetailprefix() + "*";
		String trailerPrefixPattern = loadTermDepositJobConfigProperties.getTrailerprefix() + "*";
		logger.info(String.format("ItemReader [%s] created pattern", STEP_NAME));

		Map<String, LineTokenizer> tokenizerMap = new HashMap<>();
		tokenizerMap.put(headerPrefixPattern, headerTokenizer);
		tokenizerMap.put(detailPrefixPattern, detailTokenizer);
		tokenizerMap.put(trailerPrefixPattern, trailerTokenizer);
		logger.info(String.format("ItemReader [%s] created tokenizer Map", STEP_NAME));

		FieldSetMapper<LoadTermDeposit> headerMapper = new LoadTermDepositHeaderFieldSetMapper();
		FieldSetMapper<LoadTermDeposit> detailMapper = new LoadTermDepositDetailFieldSetMapper();
		FieldSetMapper<LoadTermDeposit> trailerMapper = new LoadTermDepositTrailerFieldSetMapper();
		logger.info(String.format("ItemReader [%s] created mapper header,detail,trailer", STEP_NAME));

		Map<String, FieldSetMapper<LoadTermDeposit>> fieldSetMapperMap = new HashMap<>();
		fieldSetMapperMap.put(headerPrefixPattern, headerMapper);
		fieldSetMapperMap.put(detailPrefixPattern, detailMapper);
		fieldSetMapperMap.put(trailerPrefixPattern, trailerMapper);

		PatternMatchingCompositeLineMapper<LoadTermDeposit> lineMapper = new PatternMatchingCompositeLineMapper<>();
		lineMapper.setTokenizers(tokenizerMap);
		lineMapper.setFieldSetMappers(fieldSetMapperMap);

		FlatFileItemReader<LoadTermDeposit> reader = new FlatFileItemReader<>();
		reader.setResource(new FileSystemResource((String) stepExecution.getJobExecution().getExecutionContext()
				.get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)));
		reader.setLineMapper(lineMapper);
		logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
		return reader;
	}

	@Bean(STEP_NAME + ".ItemProcessor")
	@StepScope
	public ItemProcessor<LoadTermDeposit, LoadTermDeposit> loadTermDepositProductProcessor(
			@Value("#{stepExecution}") StepExecution stepExecution) {
		return new ItemProcessor<LoadTermDeposit, LoadTermDeposit>() {
			@Override
			public LoadTermDeposit process(LoadTermDeposit loadTermDeposit) throws Exception {
				if (loadTermDeposit instanceof LoadTermDepositHeader) {
					logger.debug("processing header");
					processLoadTermDepositProductHeader((LoadTermDepositHeader) loadTermDeposit);
				}
				if (loadTermDeposit instanceof LoadTermDepositDetail) {
					logger.debug("processing detail");
					LoadTermDepositDetail detail = processLoadTermDepositProductDetail(
							(LoadTermDepositDetail) loadTermDeposit);
					termDepositRateDetails.add(detail);
					return detail;
				}
				return null;

			}

			private LoadTermDepositHeader processLoadTermDepositProductHeader(
					LoadTermDepositHeader loadTermDepositHeader) throws BatchException, ParseException {
				logger.info(String.format("processing record header %s", loadTermDepositHeader));
				try {
					Date parsedDate = dateFormat.parse(loadTermDepositHeader.getFileBatchDate());
					processDate = new java.sql.Timestamp(parsedDate.getTime());
				} catch (ParseException e) {
					logger.warn(e);
				}

				return loadTermDepositHeader;

			}
		};
	}

	private LoadTermDepositDetail processLoadTermDepositProductDetail(LoadTermDepositDetail loadTermDepositDetail)
			throws BatchException {
		recordCount++;
		logger.debug(String.format("processing record=%s, interest_rate=%s, end_date=%s", recordCount,
				loadTermDepositDetail.getInterestRate(), loadTermDepositDetail.getEndDate()));
		isPassValidation = true;
		return loadTermDepositDetail;
	}

	@Bean(STEP_NAME + ".ItemWriter")
	@StepScope
	public ItemWriter<LoadTermDeposit> loadTermDepositProductWriter(
			@Value("#{stepExecution}") StepExecution stepExecution) {
		return new ItemWriter<LoadTermDeposit>() {
			@Override
			public void write(List<? extends LoadTermDeposit> termDepositRateList) throws Exception {
				Long jobExecutionId = stepExecution.getJobExecution().getId();
				String fileFullPath = stepExecution.getJobExecution().getExecutionContext()
						.getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
				String fileName = new File(fileFullPath).getName();

				logger.info(String.format("Record into DB job exec id=%s ,file name=%s", jobExecutionId, fileName));
				logger.info(String.format("TermDepositRateDetails size=%s", termDepositRateDetails.size()));
				logger.info(String.format("isPassValidation=%s", isPassValidation));
				if (isPassValidation) {
					List<BatchStagedDepositProduct> batchStagedDepositProducts = new ArrayList<>();
					for (LoadTermDepositDetail loadTermDepositDetail : termDepositRateDetails) {
						BatchStagedDepositProduct batchStagedDepositProduct = createBatchStageDepositProduct(
								loadTermDepositDetail);
						batchStagedDepositProduct.setJobExecutionId(jobExecutionId);
						batchStagedDepositProduct.setFileName(fileName);
						logger.debug(String.format(
								"Inserting Batch Staged Notification Raw Record job exec id=%s, object [%s] to DB",
								jobExecutionId, batchStagedDepositProduct));
						batchStagedDepositProducts.add(batchStagedDepositProduct);
					}
					if (batchStagedDepositProducts.size() > 0) {
						int inserted = batchStagedDepositProductRepositoryImpl
								.addBatchBatchStageDepositProduct(batchStagedDepositProducts);
						logger.debug("added batchstaged deposit product"+inserted);
					}

				}
			}
		};
	}

	private BatchStagedDepositProduct createBatchStageDepositProduct(LoadTermDepositDetail loadTermDepositDetail) {
		BatchStagedDepositProduct batchStagedDepositProduct = new BatchStagedDepositProduct();
		batchStagedDepositProduct.setDepositType("SAVINGS");
		batchStagedDepositProduct.setProductCode(loadTermDepositDetail.getProductType());
		batchStagedDepositProduct.setProductName(loadTermDepositDetail.getProductDescription());
		batchStagedDepositProduct.setTenure(loadTermDepositDetail.getTenure());
		batchStagedDepositProduct.setInterestRate(loadTermDepositDetail.getInterestRate() / 1000000);
		batchStagedDepositProduct.setIslamic(loadTermDepositDetail.getControl1());
		if (!loadTermDepositDetail.getEndDate().isEmpty()) {
			try {
				Date endDate1 = dateFormat.parse(loadTermDepositDetail.getEndDate());
				endDate = new java.sql.Timestamp(endDate1.getTime());
				batchStagedDepositProduct.setPromoEndDate(endDate);
			} catch (ParseException e) {
				logger.warn(e);
			}
		}
		batchStagedDepositProduct.setProcessed("true");
		batchStagedDepositProduct.setProcessDate(processDate);
		batchStagedDepositProduct.setCreatedTime(now);
		return batchStagedDepositProduct;
	}

	@Override
	@Bean(name = STEP_NAME)
	public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME)
				.<LoadTermDeposit, LoadTermDeposit>chunk(loadTermDepositJobConfigProperties.getChunkSize())
				.reader(itemReader).processor(itemProcessor).writer(itemWriter).build();

	}
}