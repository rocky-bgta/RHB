package com.rhbgroup.dcp.bo.batch.job.step.writer;

import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.EPullAutoEnrollmentJobProperties;
import com.rhbgroup.dcp.bo.batch.job.model.DcpFDeStateUpd;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExtractDailyDataParameter.OUTPUT_FILE_LIST_FIRST_TIME_LOGIN;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.*;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;

@Component
public class EPullAutoEnrollmentWriter {
    private static final Logger logger = Logger.getLogger(EPullAutoEnrollmentWriter.class);

    private static final String PROCESS_DATE = "ProcessDate";

    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;

    @Autowired
    EPullAutoEnrollmentJobProperties configProperties;

    @Bean("EPullEnrollmentWriter")
    @StepScope
    public FlatFileItemWriter<DcpFDeStateUpd> ePullAutoEnrollmentJobWriterForTxt(
            @Value("#{stepExecution}") StepExecution stepExecution) throws ParseException {
        logger.debug("ePullAutoEnrollmentJobWriterForTxt");
        logger.debug("stepExecution: " + stepExecution);

        Date processDate = getProcessDate(stepExecution);
        logger.debug("processDate: " + processDate);

        FlatFileItemWriter<DcpFDeStateUpd> flatFileItemWriter = new FlatFileItemWriter<>();
        logger.debug("flatFileItemWriter: " + flatFileItemWriter);

        String filename = configProperties.getTxtProperty("filename");
        String filenameDateFormat = configProperties.getNamedateformat();
        String targetFileNewName = filename.replace("{#date}", DateUtils.formatDateString(processDate, filenameDateFormat));
        logger.debug("targetFileNewName: " + targetFileNewName);

        String jobName = stepExecution.getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
        logger.debug("jobName: " + jobName);

        File targetFileFullPath = Paths.get(outputFolderFullPath,jobName,targetFileNewName).toFile();
        logger.debug("targetFileFullPath: " + targetFileFullPath);

        addOutputFile(stepExecution, targetFileFullPath);

        flatFileItemWriter.setResource(new FileSystemResource(targetFileFullPath.getPath()));
        stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY,
                DateUtils.formatDateString(processDate,DEFAULT_DATE_FORMAT));
        stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY,
                targetFileFullPath.getAbsolutePath());

        LineAggregator<DcpFDeStateUpd> lineAggregator = createExtractDailyFirstTimeLoginLineAggregatorForTxt();
        logger.debug("lineAggregator: " + lineAggregator);
        flatFileItemWriter.setLineAggregator(lineAggregator);

        FlatFileHeaderCallback headerCallback = (writer) -> {
            String header = configProperties.getTxtProperty("header")
                    .replace("{#date}", DateUtils.formatDateString(processDate, COMMON_DATE_HEADER_FORMAT))
                    .replace("{#filler}", String.format("%-178.178s", "eStatement FD Registration"));
            writer.write(header);
        };

        FlatFileFooterCallback footerCallback = (writer) -> {
            int recordCount = 0;
            if(Objects.nonNull(stepExecution.getJobExecution().getExecutionContext().get(RECORD_COUNT))) {
            	recordCount = (int) stepExecution.getJobExecution().getExecutionContext().get(RECORD_COUNT);
            } 
            String footer = configProperties.getTxtProperty("footer")
                    .replace("{#count}", String.format("%010d", recordCount))
                    .replace("{#filler}", String.format("%-180.180s", ""));
            writer.write(footer);
        };

        flatFileItemWriter.setHeaderCallback(headerCallback);
        flatFileItemWriter.setFooterCallback(footerCallback);

        return flatFileItemWriter;
    }

    private void addOutputFile(StepExecution stepExecution, File file) {
        List<File> fileList = (List<File>) stepExecution.getJobExecution().getExecutionContext().get(OUTPUT_FILE_LIST_FIRST_TIME_LOGIN);
        if (fileList == null) {
            fileList = new LinkedList<File>();
            stepExecution.getJobExecution().getExecutionContext().put(OUTPUT_FILE_LIST_FIRST_TIME_LOGIN, fileList);
        }
        fileList.add(file);
    }

    private Date getProcessDate(StepExecution stepExecution) throws ParseException {

        Date processDate = null;
        Date executionContextProcessDate = (Date) stepExecution.getJobExecution().getExecutionContext().get(PROCESS_DATE);
        logger.info("ePullAutoEnrollmentStepBuilder - getProcessDate - executionContextProcessDate: " + executionContextProcessDate);
        if(executionContextProcessDate != null) {
            // continuous
            processDate = executionContextProcessDate;
        } else {
            // retrieve from JobParameter
            String externalDate = stepExecution.getJobParameters().getString(PROCESS_DATE);
            logger.info("ePullAutoEnrollmentStepBuilder - getProcessDate - jobparameter externalDate: " + externalDate);
            if(externalDate !=  null) {
                processDate = new SimpleDateFormat("yyyy-MM-dd").parse(externalDate);
            }

            // retrieve from Config table
            if (processDate == null) {
                String batchSystemDateStr = (String) stepExecution.getJobExecution().getExecutionContext()
                        .get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
                logger.info("ePullAutoEnrollmentStepBuilder - getProcessDate - batchSystemDateStr: " + batchSystemDateStr);
                Date batchSystemDate = DateUtils.getDateFromString(batchSystemDateStr, DEFAULT_DATE_FORMAT);
                processDate = DateUtils.addDays(batchSystemDate, -1);

                stepExecution.getJobExecution().getExecutionContext().put(PROCESS_DATE, processDate);
            }
        }
        logger.info("ePullAutoEnrollmentStepBuilder - getProcessDate - final process date: "+processDate);
        return processDate;
    }

    private LineAggregator<DcpFDeStateUpd> createExtractDailyFirstTimeLoginLineAggregatorForTxt() {
        FormatterLineAggregator<DcpFDeStateUpd> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat(configProperties.getTxtProperty("detailcolumns"));

        FieldExtractor<DcpFDeStateUpd> fieldExtractor = createExtractDailyFirstTimeLoginFieldExtractorForTxt();
        lineAggregator.setFieldExtractor(fieldExtractor);

        return lineAggregator;
    }

    private FieldExtractor<DcpFDeStateUpd> createExtractDailyFirstTimeLoginFieldExtractorForTxt() {
        BeanWrapperFieldExtractor<DcpFDeStateUpd> extractor = new BeanWrapperFieldExtractor<>();
        String[] names = configProperties.getTxtProperty("detailnames").split(",", -1);
        extractor.setNames(names);
        return extractor;
    }
}

