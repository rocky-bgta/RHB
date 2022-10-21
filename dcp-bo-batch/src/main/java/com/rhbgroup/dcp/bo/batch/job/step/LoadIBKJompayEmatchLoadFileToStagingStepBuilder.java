package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadIBKJompayEmatchJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BatchStagedIBKJompayEmatchingDetailFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BatchStagedIBKJompayEmatchingHeaderFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BatchStagedIBKJompayEmatchingTrailerFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKJompayEmatching;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKJompayEmatchingDetail;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKJompayEmatchingHeader;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKJompayEmatchingTrailer;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchStagedIBKJompayEmatchingRepositoryImpl;

@Component
@Lazy
public class LoadIBKJompayEmatchLoadFileToStagingStepBuilder extends BaseStepBuilder{
	
	private static final Logger logger = Logger.getLogger(LoadIBKJompayEmatchLoadFileToStagingStepBuilder.class);
	private static final String STEP_NAME = "LoadIBKJompayEmatchLoadFileToStagingStep";

	Double debitAmount;
	int recordCount;
	
	@Autowired
	LoadIBKJompayEmatchJobConfigProperties configProperties;
	
	@Autowired
	BatchStagedIBKJompayEmatchingRepositoryImpl batchStagedIBKJompayRepo;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchStagedIBKJompayEmatching> itemReader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchStagedIBKJompayEmatching, BatchStagedIBKJompayEmatching> itemProcessor;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchStagedIBKJompayEmatching> itemWriter;
	
    @Bean(STEP_NAME + ".ItemReader")
    @StepScope
    public FlatFileItemReader<BatchStagedIBKJompayEmatching> loadIBKJompayReader(@Value("#{stepExecution}") StepExecution stepExecution) {
    	debitAmount =0.0;
    	recordCount=0;

		LineTokenizer headerTokenizer = BatchUtils.getFixedLengthTokenizer(configProperties.getHeadernames(), configProperties.getHeadercolumns());
		LineTokenizer detailTokenizer = BatchUtils.getFixedLengthTokenizer(configProperties.getDetailnames(), configProperties.getDetailcolumns());
		LineTokenizer trailerTokenizer = BatchUtils.getFixedLengthTokenizer(configProperties.getTrailernames(), configProperties.getTrailercolumns());
		
		logger.info(String.format("ItemReader [%s.ItemReader] created LineTokenizer", STEP_NAME));

		String headerPrefixPattern  = configProperties.getHeaderprefix() + "*";
		String detailPrefixPattern  = configProperties.getDetailprefix() + "*";
		String trailerPrefixPattern  = configProperties.getTrailerprefix() + "*";
		
		logger.info(String.format("ItemReader [%s.ItemReader] created pattern", STEP_NAME));

		Map<String, LineTokenizer> tokenizerMap = new HashMap<>();
		tokenizerMap.put(headerPrefixPattern, headerTokenizer);
		tokenizerMap.put(detailPrefixPattern, detailTokenizer);
		tokenizerMap.put(trailerPrefixPattern, trailerTokenizer);
		
		logger.info(String.format("ItemReader [%s.ItemReader] created tokenizer Map", STEP_NAME));

		FieldSetMapper<BatchStagedIBKJompayEmatching> headerMapper = new BatchStagedIBKJompayEmatchingHeaderFieldSetMapper();
		FieldSetMapper<BatchStagedIBKJompayEmatching> detailMapper = new BatchStagedIBKJompayEmatchingDetailFieldSetMapper();
		FieldSetMapper<BatchStagedIBKJompayEmatching> trailerMapper = new BatchStagedIBKJompayEmatchingTrailerFieldSetMapper();
		
		logger.info(String.format("ItemReader [%s.ItemReader] created mapper header,detail,trailer", STEP_NAME));
		
		Map<String, FieldSetMapper<BatchStagedIBKJompayEmatching>> fieldSetMapperMap = new HashMap<>();
		fieldSetMapperMap.put(headerPrefixPattern, headerMapper);
		fieldSetMapperMap.put(detailPrefixPattern, detailMapper);
		fieldSetMapperMap.put(trailerPrefixPattern, trailerMapper);
		
		PatternMatchingCompositeLineMapper<BatchStagedIBKJompayEmatching> lineMapper = new PatternMatchingCompositeLineMapper<>();
		lineMapper.setTokenizers(tokenizerMap);
		lineMapper.setFieldSetMappers(fieldSetMapperMap);
		
        FlatFileItemReader<BatchStagedIBKJompayEmatching> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource((String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)));
		reader.setLineMapper(lineMapper);
		
		logger.info(String.format("ItemReader [%s.ItemReader] created succesfully", STEP_NAME));
        return reader;
    }
    
    @Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<BatchStagedIBKJompayEmatching, BatchStagedIBKJompayEmatching> loadIBKJompayProcessor() {
        return new ItemProcessor<BatchStagedIBKJompayEmatching, BatchStagedIBKJompayEmatching>() {
            @Override
			public BatchStagedIBKJompayEmatching process(BatchStagedIBKJompayEmatching batchStagedIBKJompay) throws Exception {
            	if(batchStagedIBKJompay instanceof BatchStagedIBKJompayEmatchingHeader) {
            		logger.info("processing header");
            	}else if(batchStagedIBKJompay instanceof BatchStagedIBKJompayEmatchingDetail) {
            		processIBKJompayDetail((BatchStagedIBKJompayEmatchingDetail)batchStagedIBKJompay);
    				return batchStagedIBKJompay;
            	}else if(batchStagedIBKJompay instanceof BatchStagedIBKJompayEmatchingTrailer) {
            		logger.info("processing trailer");
            	}
				return null;
			}
        };
    }
    
	private void processIBKJompayDetail(BatchStagedIBKJompayEmatchingDetail batchStagedIBKJompay) {
		logger.info(String.format("processing details %s",batchStagedIBKJompay));
		if (StringUtils.isBlank(batchStagedIBKJompay.getChannelId())) {
			logger.warn("Column \"channel_id\" should not be null or empty");
		}
		if (StringUtils.isBlank(batchStagedIBKJompay.getChannelStatus())) {
			logger.warn("Column \"channel_status\" should not be null or empty");
		}

		if (StringUtils.isBlank(batchStagedIBKJompay.getApplicationId())) {
			logger.warn("Column \"application_id\" should not be null or empty");
		}

		if (StringUtils.isBlank(batchStagedIBKJompay.getAcctCtrl1())) {
			logger.warn("Column \"acct_ctrl1\" should not be null or empty");
		}

		if (StringUtils.isBlank(batchStagedIBKJompay.getAcctCtrl2())) {
			logger.warn("Column \"acct_ctrl2\" should not be null or empty");
		}

		if (StringUtils.isBlank(batchStagedIBKJompay.getAcctCtrl3())) {
			logger.warn("Column \"acct_ctrl3\" should not be null or empty");
		}

		if (StringUtils.isBlank(batchStagedIBKJompay.getAccountNo())) {
			logger.warn("Column \"account_no\" should not be null or empty");
		}

		if (StringUtils.isBlank(batchStagedIBKJompay.getDebitCreditInd())) {
			logger.warn("Column \"debit_credit_ind\" should not be null or empty");
		}
		if (StringUtils.isBlank(batchStagedIBKJompay.getUserTranCode())) {
			logger.warn("Column \"user_tran_code\" should not be null or empty");
		}
		if (batchStagedIBKJompay.getAmount() == 0) {
			logger.warn("Column \"amount\" should not be null or empty");
		}

		if (StringUtils.isBlank(batchStagedIBKJompay.getTxnBranch())) {
			logger.warn("Column \"txn_branch\" should not be null or empty");
		}
		if (StringUtils.isBlank(batchStagedIBKJompay.getTxnDate())) {
			logger.warn("Column \"txn_date\" should not be null or empty");
		}
		if (StringUtils.isBlank(batchStagedIBKJompay.getTxnTime())) {
			logger.warn("Column \"txn_time\" should not be null or empty");
		}
	}
    @Bean(STEP_NAME + ".ItemWriter")
    @StepScope
	public ItemWriter<BatchStagedIBKJompayEmatching> loadIBKJompayWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
  	  return new ItemWriter<BatchStagedIBKJompayEmatching>() {
			@Override
			public void write(List<? extends BatchStagedIBKJompayEmatching> batchStagedIBKJompayEmatchingList) throws Exception {	
				String jobExecutionId=stepExecution.getJobExecution().getId().toString();
				String fileFullPath = stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
				String fileName = new File(fileFullPath).getName();
				for(BatchStagedIBKJompayEmatching batchStagedIBKJompayEmatching : batchStagedIBKJompayEmatchingList) {
					BatchStagedIBKJompayEmatchingDetail batchStagedIBKJompayEmatchingDet = (BatchStagedIBKJompayEmatchingDetail)batchStagedIBKJompayEmatching;
					batchStagedIBKJompayEmatchingDet.setJobExecutionId(jobExecutionId);
					batchStagedIBKJompayEmatchingDet.setFileName(fileName);
					batchStagedIBKJompayEmatchingDet.setCreatedTime(new Date());
					logger.info(String.format("Inserting Batch Staged IBK Jompay Ematching Record job exec id=%s, object [%s] to DB"
								, jobExecutionId
								, batchStagedIBKJompayEmatching));
					batchStagedIBKJompayRepo.addRecord(batchStagedIBKJompayEmatchingDet);
				}
			}
		};
	}
		
	@Bean
	public Step buildStep() {
        return getDefaultStepBuilder(STEP_NAME).<BatchStagedIBKJompayEmatching,BatchStagedIBKJompayEmatching>chunk(configProperties.getChunksize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
	}

}
