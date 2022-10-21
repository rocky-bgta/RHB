package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_TIME_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_INPUT_DIRECTORY;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.BranchCodeUpdateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.BranchCodeUpdateFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.BranchCodeUpdate;
import com.rhbgroup.dcp.bo.batch.job.repository.BranchCodeUpdateRepositoryImpl;
import org.apache.log4j.Logger;
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

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Lazy
public class BranchCodeUpdateStagingStepBuilder extends BaseStepBuilder {

    static final Logger logger = Logger.getLogger(BranchCodeUpdateStagingStepBuilder.class);

    private static final String STEPNAME = "BranchCodeUpdateStaging";
    private static final String HEADER_RECORD_TYPE = "HT";
    private static final String FOOTER_RECORD_TYPE = "TR";
    private static final int RECORD_TYPE_LENGTH = 2;
    private static final int HEADER_DATE_START_LENGTH = 23;
    private static final int HEADER_DATE_END_LENGTH = 31;
    private static final int HEADER_TIME_START_LENGTH = 31;
    private static final int HEADER_TIME_END_LENGTH = 37;

    @Autowired
    private BranchCodeUpdateJobConfigProperties configProperties;
    @Autowired
    private BranchCodeUpdateRepositoryImpl branchCodeUpdateRepository;

    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<BranchCodeUpdate> itemReader;
    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<BranchCodeUpdate,BranchCodeUpdate> itemProcessor;
    @Autowired
    @Qualifier(STEPNAME + ".ItemWriter")
    private ItemWriter<BranchCodeUpdate> itemWriter;

    private String headerDate;
    private String headerTime;
    private int recordCount=0;

    public static final BranchCodeUpdate checker = new BranchCodeUpdate();

    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder(STEPNAME).<BranchCodeUpdate,BranchCodeUpdate>chunk(configProperties.getChunksize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public FlatFileItemReader<BranchCodeUpdate> branchCodeUpdateJobReader(@Value("#{stepExecution}") StepExecution stepExecution) {
        FlatFileItemReader<BranchCodeUpdate> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource((String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)));

        // Ignore header
        reader.setLinesToSkip(1);

        // Initialize header skipped to split line based on fixed length
        LineCallbackHandler skippedLineCallback = line -> {

            String recordType = line.substring(0,RECORD_TYPE_LENGTH);
            if (recordType.equalsIgnoreCase(HEADER_RECORD_TYPE)) {
                headerDate = line.substring(HEADER_DATE_START_LENGTH, HEADER_DATE_END_LENGTH);
                headerTime = line.substring(HEADER_TIME_START_LENGTH, HEADER_TIME_END_LENGTH);
                checker.setHeaderIsExist(true);
            }
        };

        reader.setSkippedLinesCallback(skippedLineCallback);

        reader.setLineMapper(getDefaultFixedLengthLineMapper(BranchCodeUpdate.class
                ,configProperties.getDetailnames()
                ,configProperties.getDetailcolumns()
                ,new BranchCodeUpdateFieldSetMapper()));
        return reader;
    }

    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<BranchCodeUpdate, BranchCodeUpdate> recordCountProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        return branchCodeUpdate -> {

            if (branchCodeUpdate.getRecordType().equalsIgnoreCase(FOOTER_RECORD_TYPE)){
                checker.setFooterIsExist(true);
                checker.setFooterRecordCount(Integer.parseInt(branchCodeUpdate.getBnmBranchCode()));
            } else
                recordCount++;

            return branchCodeUpdate;
        };
    }

    @Bean(STEPNAME + ".ItemWriter")
    @StepScope
    public ItemWriter<BranchCodeUpdate> branchCodeUpdateJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
        return records -> {
            if (checker.isFooterIsExist() == false || checker.isHeaderIsExist() == false)
                throw new BatchException(BatchErrorCode.FILE_VALIDATION_ERROR, BatchErrorCode.FILE_VALIDATION_ERROR_MESSAGE);

            if (checker.getFooterRecordCount() != recordCount)
                throw new BatchException(BatchErrorCode.FILE_VALIDATION_ERROR, BatchErrorCode.FILE_VALIDATION_ERROR_MESSAGE);

            int jobExecutionId = stepExecution.getJobExecution().getId().intValue();
            String createdTime = new SimpleDateFormat(DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT).format(new Date());
            String fileFullPath =stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY);
            String fileName = new File(fileFullPath).getName();
            for (BranchCodeUpdate record : records) {
                if(!record.getRecordType().equalsIgnoreCase(FOOTER_RECORD_TYPE)) {
                    record.setJobExecutionId(jobExecutionId);
                    record.setFileName(fileName);
                    record.setHdDate(headerDate);
                    record.setHdTime(headerTime);
                    record.setCreatedTime(createdTime);
                    record.setUpdatedTime(createdTime);
                    branchCodeUpdateRepository.addToStaging(record);
                }
            }
        };
    }
}
