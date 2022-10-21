package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.GSTCentralizedFileUpdateDetailFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.GSTCentralizedFileUpdateDetail;
import com.rhbgroup.dcp.bo.batch.job.repository.GSTCentralizedFileUpdateRepositoryImpl;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobPropertyFile.BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;

@Component
@Lazy
public class GSTCentralizedFileUpdateJobReadFileToStagingStepBuilder extends BaseStepBuilder {

    @Value("${job.gstcentralizedfileupadtejob.detailnames}")
    private String fileContentNames;
    @Value("${job.gstcentralizedfileupadtejob.detailcolumns}")
    private String fileContentColumns;
    @Value("${job.gstcentralizedfileupadtejob.name}")
    private String sourceFileName;
    @Value("${job.gstcentralizedfileupadtejob.namedateformat}")
    private String sourceFileDateFormat;

    @Autowired
    @Qualifier("GSTCentralizedFileUpdateJobReadFileToStaging.ItemReader")
    private ItemReader<GSTCentralizedFileUpdateDetail> itemReader;
    @Autowired
    @Qualifier("GSTCentralizedFileUpdateJobReadFileToStaging.ItemProcessor")
    private ItemProcessor<GSTCentralizedFileUpdateDetail,GSTCentralizedFileUpdateDetail> itemProcessor;
    @Autowired
    @Qualifier("GSTCentralizedFileUpdateJobReadFileToStaging.ItemWriter")
    private ItemWriter<GSTCentralizedFileUpdateDetail> itemWriter;
    @Autowired
    private GSTCentralizedFileUpdateRepositoryImpl gstCentralizedFileUpdateRepositoryImpl;
    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;

    private String headerDate;
    private String headerTime;
    private boolean header = true;

    @Bean
    public Step buildStep() {
        return getDefaultStepBuilder("GSTCentralizedFileUpdateJobReadFileToStaging").<GSTCentralizedFileUpdateDetail,GSTCentralizedFileUpdateDetail>chunk(100)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean("GSTCentralizedFileUpdateJobReadFileToStaging.ItemReader")
    @StepScope
    public FlatFileItemReader<GSTCentralizedFileUpdateDetail> gstCentralizedFileUpdateJobReader(@Value("#{stepExecution}") StepExecution stepExecution) {
        FlatFileItemReader<GSTCentralizedFileUpdateDetail> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource((String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_CONTEXT_PARAMETER_TEMP_IN_FILE_FULL_PATH_KEY)));

        // Ignore header
        reader.setLinesToSkip(1);

        // Initialize header skipped to split line based on fixed length
        LineCallbackHandler skippedLineCallback = line -> {
            if(header)
            {
                headerDate = line.substring(10,18).trim();
                headerTime = line.substring(18,24).trim();
                header = false;
            }
        };

        reader.setSkippedLinesCallback(skippedLineCallback);

        reader.setLineMapper(getDefaultFixedLengthLineMapper(GSTCentralizedFileUpdateDetail.class
                ,fileContentNames
                ,fileContentColumns
                ,new GSTCentralizedFileUpdateDetailFieldSetMapper()));

        return reader;
    }

    @Bean("GSTCentralizedFileUpdateJobReadFileToStaging.ItemProcessor")
    @StepScope
    public ItemProcessor<GSTCentralizedFileUpdateDetail, GSTCentralizedFileUpdateDetail> gstCentralizedFileUpdateJobProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
        return new ItemProcessor<GSTCentralizedFileUpdateDetail, GSTCentralizedFileUpdateDetail>() {
            @Override
            public GSTCentralizedFileUpdateDetail process(GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail) throws Exception {

                String batchSystemDateStr=(String) stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);

                // Use the job process date if availabler
                Map<String, String> jobParameters = dcpBatchApplicationContext.getInitialJobArguments();
                String jobprocessdate = null;
                if(jobParameters.containsKey(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY)){
                    jobprocessdate = jobParameters.get(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY);
                }

                Date batchSystemDate = (!jobParameters.containsKey(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY)) ? DateUtils.getDateFromString(batchSystemDateStr,DEFAULT_DATE_FORMAT) : DateUtils.getDateFromString(jobprocessdate,DEFAULT_JOB_PARAMETER_DATE_FORMAT);

                String targetFileNewName = sourceFileName.replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, DateUtils.formatDateString(batchSystemDate, sourceFileDateFormat));

                SimpleDateFormat dateTimeInGMT = new SimpleDateFormat("HHmmss");
                //Setting the time zone
                dateTimeInGMT.setTimeZone(TimeZone.getTimeZone("GMT"));

                gstCentralizedFileUpdateDetail.setFileName(targetFileNewName);

                gstCentralizedFileUpdateDetail.setHdDate(headerDate);
                gstCentralizedFileUpdateDetail.setHdTime(headerTime);

                return gstCentralizedFileUpdateDetail;
            }
        };
    }

    @Bean("GSTCentralizedFileUpdateJobReadFileToStaging.ItemWriter")
    @StepScope
    public ItemWriter<GSTCentralizedFileUpdateDetail> gstCentralizedFileUpdateJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
        return messages -> {
            String jobExecutionId = stepExecution.getJobExecution().getId().toString();
            for (GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail : messages) {

                gstCentralizedFileUpdateDetail.setJobExecutionId(jobExecutionId);
                gstCentralizedFileUpdateRepositoryImpl.addGSTCentralizedFileUpdateStaging(gstCentralizedFileUpdateDetail);
            }
        };
    }
}
