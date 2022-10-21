package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobPropertyFile.BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_TIME_FORMAT;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.PremierCustomerInfoandRMCodeTaggingJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.PremierCustomerInfoandRMCodeTaggingDetailFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.PremierCustomerInfoandRMCodeTaggingDetail;
import com.rhbgroup.dcp.bo.batch.job.repository.PremierCustomerInfoandRMCodeTaggingRepositoryImpl;

@Component
@Lazy
public class PremierCustomerInfoandRMCodeTaggingJobReadFileToStagingStepBuilder extends BaseStepBuilder {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PremierCustomerInfoandRMCodeTaggingJobReadFileToStagingStepBuilder.class);

    @Autowired
    private PremierCustomerInfoandRMCodeTaggingJobConfigProperties configProperties;
    
    @Autowired
    @Qualifier("PremierCustomerInfoandRMCodeTaggingJobReadFileToStaging.ItemReader")
    private ItemReader<PremierCustomerInfoandRMCodeTaggingDetail> itemReader;
    @Autowired
    @Qualifier("PremierCustomerInfoandRMCodeTaggingJobReadFileToStaging.ItemProcessor")
    private ItemProcessor<PremierCustomerInfoandRMCodeTaggingDetail,PremierCustomerInfoandRMCodeTaggingDetail> itemProcessor;
    @Autowired
    @Qualifier("PremierCustomerInfoandRMCodeTaggingJobReadFileToStaging.ItemWriter")
    private ItemWriter<PremierCustomerInfoandRMCodeTaggingDetail> itemWriter;
    @Autowired
    private PremierCustomerInfoandRMCodeTaggingRepositoryImpl premierCustomerInfoandRMCodeTaggingRepositoryImpl;
    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;

    private String processingDate;
    private String systemDate;
    private String systemTime;

    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder("PremierCustomerInfoandRMCodeTaggingJobReadFileToStaging").<PremierCustomerInfoandRMCodeTaggingDetail,PremierCustomerInfoandRMCodeTaggingDetail>chunk(100)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean("PremierCustomerInfoandRMCodeTaggingJobReadFileToStaging.ItemReader")
    @StepScope
    public FlatFileItemReader<PremierCustomerInfoandRMCodeTaggingDetail> premierCustomerInfoandRMCodeTaggingJobReader(@Value("#{stepExecution}") StepExecution stepExecution) {
        FlatFileItemReader<PremierCustomerInfoandRMCodeTaggingDetail> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource((String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)));

        // Ignore header
        reader.setLinesToSkip(1);

        // Initialize header skipped to split line based on fixed length
        LineCallbackHandler skippedLineCallback = line -> {
            processingDate = line.substring(2,12).trim();
            systemDate = line.substring(12,22).trim();
            systemTime = line.substring(22,28).trim();
        };

        reader.setSkippedLinesCallback(skippedLineCallback);

        reader.setLineMapper(getDefaultFixedLengthLineMapper(PremierCustomerInfoandRMCodeTaggingDetail.class
                ,configProperties.getDetailNames()
                ,configProperties.getDetailColumns()
                ,new PremierCustomerInfoandRMCodeTaggingDetailFieldSetMapper()));

        return reader;
    }

    @Bean("PremierCustomerInfoandRMCodeTaggingJobReadFileToStaging.ItemProcessor")
    @StepScope
    public ItemProcessor<PremierCustomerInfoandRMCodeTaggingDetail, PremierCustomerInfoandRMCodeTaggingDetail> premierCustomerInfoandRMCodeTaggingJobProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        return new ItemProcessor<PremierCustomerInfoandRMCodeTaggingDetail, PremierCustomerInfoandRMCodeTaggingDetail>() {
            @Override
            public PremierCustomerInfoandRMCodeTaggingDetail process(PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail) throws Exception {

                String batchSystemDateStr=(String) stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);

                // Use the job process date if availabler
                Map<String, String> jobParameters = dcpBatchApplicationContext.getInitialJobArguments();
                String jobprocessdate = null;
                if(jobParameters.containsKey(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY)){
                    jobprocessdate = jobParameters.get(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY);
                }

                Date batchSystemDate = (!jobParameters.containsKey(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY)) ? DateUtils.getDateFromString(batchSystemDateStr,DEFAULT_DATE_FORMAT) : DateUtils.getDateFromString(jobprocessdate,DEFAULT_JOB_PARAMETER_DATE_FORMAT);

                String targetFileNewName = configProperties.getName().replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, DateUtils.formatDateString(batchSystemDate, configProperties.getNameDateFormat()));

                SimpleDateFormat dateTimeInGMT = new SimpleDateFormat("HHmmss");
                //Setting the time zone
                dateTimeInGMT.setTimeZone(TimeZone.getTimeZone("GMT"));

                premierCustomerInfoandRMCodeTaggingDetail.setFileName(targetFileNewName);

                premierCustomerInfoandRMCodeTaggingDetail.setProcessingDt(processingDate);
                premierCustomerInfoandRMCodeTaggingDetail.setSystemDt(systemDate);
                premierCustomerInfoandRMCodeTaggingDetail.setSystemTime(systemTime);
                premierCustomerInfoandRMCodeTaggingDetail.setCreatedTime(DateUtils.formatDateString(new Date(), DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT));

                return premierCustomerInfoandRMCodeTaggingDetail;
            }
        };
    }

    @Bean("PremierCustomerInfoandRMCodeTaggingJobReadFileToStaging.ItemWriter")
    @StepScope
    public ItemWriter<PremierCustomerInfoandRMCodeTaggingDetail> premierCustomerInfoandRMCodeTaggingJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
    	return new ItemWriter<PremierCustomerInfoandRMCodeTaggingDetail>() {
			@Override
			public void write(List<? extends PremierCustomerInfoandRMCodeTaggingDetail> records) throws Exception {
  				String errorMsg ="";
  				logger.debug("Start writing UT customer");
   				try {
   					String jobExecutionId = stepExecution.getJobExecution().getId().toString();
   					for (PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail: records) {
   						premierCustomerInfoandRMCodeTaggingDetail.setJobExecutionId(jobExecutionId);
   					}
   					int inserted = premierCustomerInfoandRMCodeTaggingRepositoryImpl.addRecordBatch(records);
   					logger.debug(String.format("UT Customer -Inserted rows  %s",inserted));
   				} catch(Exception ex) {
  					errorMsg = String.format("Exception: exception=%s",ex.getMessage());
  					logger.error(errorMsg);
  					throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR,errorMsg,ex);
  				}
			}
  			
  		};
    }
}
