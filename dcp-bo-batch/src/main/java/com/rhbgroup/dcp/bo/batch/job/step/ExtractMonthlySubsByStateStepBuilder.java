package com.rhbgroup.dcp.bo.batch.job.step;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractMonthlySubsByStateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.ExtractMonthlySubsByState;
import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.*;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobPropertyFile.BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_JOB_PARAMETER_REPORT_FILES_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ReportJobContextParameter.REPORT_JOB_PARAMETER_REPORT_TARGET_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;

@Component
@Lazy
public class ExtractMonthlySubsByStateStepBuilder extends BaseStepBuilder {
    static final Logger logger = Logger.getLogger(ExtractMonthlySubsByStateStepBuilder.class);

    private static final String STEPNAME = "ExtractMonthlySubsByStateStep";
    private static final String PROCESS_DATE = "ProcessDate";
    private static final String OUTPUT_FILE_LIST = "output.file.list";
    private static final Integer CHUNK_SIZE = 10000;

    private ExtractMonthlySubsByStateJobConfigProperties.ReportConfig reportConfigProp;

    @Autowired
    private ExtractMonthlySubsByStateJobConfigProperties jobConfigProp;

    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;

    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<ExtractMonthlySubsByState> itemReader;

    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<ExtractMonthlySubsByState, ExtractMonthlySubsByState> itemProcessor;

    @Autowired
    @Qualifier(STEPNAME + ".CsvItemWriter")
    private ItemWriter<ExtractMonthlySubsByState> csvItemWriter;

    @Override
    @Bean
    public Step buildStep() {
        CompositeItemWriter<ExtractMonthlySubsByState> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(csvItemWriter));

        logger.debug("buildStep()");
        return getDefaultStepBuilder(STEPNAME).<ExtractMonthlySubsByState, ExtractMonthlySubsByState>chunk(CHUNK_SIZE)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(compositeItemWriter)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<ExtractMonthlySubsByState> extractMonthlySubsByStateJobReader(@Value("#{stepExecution}") StepExecution stepExecution
            , DataSource dataSource) throws BatchException {
        logger.debug("extractMonthlySubsByStateJobReader()");
        logger.debug("    dataSource: " + dataSource);

        // No choice, have to do it here..
        String reportId = stepExecution.getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_REPORT_ID_KEY);
        Optional<ExtractMonthlySubsByStateJobConfigProperties.ReportConfig> reportConfigOpt =
                Optional.ofNullable(jobConfigProp.getBnmsubs().get(reportId));

        ExtractMonthlySubsByStateJobConfigProperties.ReportConfig configProperties = reportConfigOpt.orElseThrow(
                () -> new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR,
                        "Report Config error : Please check application properties: " + reportId));
        // Set on global scope.
        setConfigProperties(configProperties);

        JdbcPagingItemReader<ExtractMonthlySubsByState> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(configProperties.getJdbcpagingpagesize());

        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("row_number", Order.ASCENDING);

        logger.debug("Sql View: " + configProperties.getSqlview());
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM " + configProperties.getSqlview());
        queryProvider.setSortKeys(sortConfiguration);

        databaseReader.setQueryProvider(queryProvider);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(ExtractMonthlySubsByState.class));
        logger.debug("    queryProvider: " + queryProvider);
        logger.debug("    databaseReader: " + databaseReader);
        return databaseReader;
    }

    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<ExtractMonthlySubsByState, ExtractMonthlySubsByState> extractMonthlySubsByStateJobProcessor() {
        return extractMonthlySubsByState -> extractMonthlySubsByState;
    }

    @Bean(STEPNAME + ".CsvItemWriter")
    @StepScope
    FlatFileItemWriter<ExtractMonthlySubsByState> extractMonthlySubsByStateJobWriterForCsv(@Value("#{stepExecution}") StepExecution stepExecution) throws ParseException {
        logger.debug("extractMonthlySubsByStateJobWriterForCsv()");
        logger.debug("    stepExecution: " + stepExecution);

        Date processDate = getProcessDate(stepExecution);
        logger.debug("    processDate: " + processDate);

        FlatFileItemWriter<ExtractMonthlySubsByState> flatFileItemWriter=new FlatFileItemWriter<>();
        logger.debug("    flatFileItemWriter: " + flatFileItemWriter);

        String filename = reportConfigProp.getCsvProperty("filename");
        String filenameDateFormat = reportConfigProp.getNamedateformat();
        String targetFileNewName = filename.replace("{#date}", DateUtils.formatDateString(processDate, filenameDateFormat));
        logger.debug("    targetFileNewName: " + targetFileNewName);

        String jobName = stepExecution.getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
        logger.debug("    jobName: " + jobName);

        File targetFileFullPath = Paths.get(outputFolderFullPath,jobName,targetFileNewName).toFile();
        logger.debug("    targetFileFullPath: " + targetFileFullPath);

        addOutputFile(stepExecution, targetFileFullPath);

        flatFileItemWriter.setResource(new FileSystemResource(targetFileFullPath.getPath()));
        stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY,
                DateUtils.formatDateString(processDate,DEFAULT_DATE_FORMAT));
        stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY,
                targetFileFullPath.getAbsolutePath());

        ArrayList<String> reportFileFullPathList= new ArrayList<>();
        reportFileFullPathList.add(targetFileFullPath.getAbsolutePath());

        stepExecution.getJobExecution().getExecutionContext().put(REPORT_JOB_PARAMETER_REPORT_FILES_PATH_KEY, reportFileFullPathList);
        stepExecution.getJobExecution().getExecutionContext().put(REPORT_JOB_PARAMETER_REPORT_TARGET_PATH_KEY, getTargetPath(stepExecution));

        LineAggregator<ExtractMonthlySubsByState> lineAggregator = createExtractMonthlySubsByStateLineAggregatorForCsv();
        logger.debug("    lineAggregator: " + lineAggregator);
        flatFileItemWriter.setLineAggregator(lineAggregator);

        return flatFileItemWriter;
    }

    private String getTargetPath(StepExecution stepExecution) throws ParseException {

        String batchSystemDateStr = (String) stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
        String targetDateStr = DateUtils.convertDateFormat(batchSystemDateStr, DEFAULT_DATE_FORMAT, reportConfigProp.getNamedateformat());
        String targetFileFullPath = reportConfigProp.getCsvProperty("filename").replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, targetDateStr);
        StringBuilder strBuilder = new StringBuilder(reportConfigProp.getFtpfolder());

        return strBuilder.append(BatchSystemConstant.FTP.FTP_SEPARATOR).append(targetFileFullPath).toString();
    }

    private void setConfigProperties(ExtractMonthlySubsByStateJobConfigProperties.ReportConfig reportConfigProp) {
        this.reportConfigProp = reportConfigProp;
    }

    @SuppressWarnings("unchecked")
    private void addOutputFile(StepExecution stepExecution, File file) {
        List<File> fileList = (List<File>) stepExecution.getJobExecution().getExecutionContext().get(OUTPUT_FILE_LIST);
        if (fileList == null) {
            fileList = new LinkedList<>();
            stepExecution.getJobExecution().getExecutionContext().put(OUTPUT_FILE_LIST, fileList);
        }
        fileList.add(file);
    }

    private Date getProcessDate(StepExecution stepExecution) throws ParseException {

        Date processDate = null;
        Date executionContextProcessDate = (Date) stepExecution.getJobExecution().getExecutionContext().get(PROCESS_DATE);
        logger.info("ExtractMonthlySubsByStateStepBuilder - getProcessDate - executionContextProcessDate: " + executionContextProcessDate);
        if(executionContextProcessDate != null) {
            // continuous
            processDate = executionContextProcessDate;
        } else {
            // retrieve from JobParameter
            String externalDate = stepExecution.getJobParameters().getString(PROCESS_DATE);
            logger.info("ExtractMonthlySubsByStateStepBuilder - getProcessDate - jobparameter externalDate: " + externalDate);
            if(externalDate !=  null) {
                processDate = new SimpleDateFormat("yyyy-MM-dd").parse(externalDate);
            }

            // retrieve from Config table
            if (processDate == null) {
                String batchSystemDateStr = (String) stepExecution.getJobExecution().getExecutionContext()
                        .get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
                logger.info("ExtractMonthlySubsByStateStepBuilder - getProcessDate - batchSystemDateStr: " + batchSystemDateStr);
                Date batchSystemDate = DateUtils.getDateFromString(batchSystemDateStr, DEFAULT_DATE_FORMAT);
                processDate = DateUtils.addDays(batchSystemDate, -1);

                stepExecution.getJobExecution().getExecutionContext().put(PROCESS_DATE, processDate);
            }
        }
        logger.info("ExtractMonthlySubsByStateStepBuilder - getProcessDate - final process date: "+ processDate);
        return processDate;
    }

    private LineAggregator<ExtractMonthlySubsByState> createExtractMonthlySubsByStateLineAggregatorForCsv() {
        FormatterLineAggregator<ExtractMonthlySubsByState> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat(reportConfigProp.getCsvProperty("detailcolumns"));

        FieldExtractor<ExtractMonthlySubsByState> fieldExtractor = createExtractMonthlySubsByStateFieldExtractorForCsv();
        lineAggregator.setFieldExtractor(fieldExtractor);

        return lineAggregator;
    }

    private FieldExtractor<ExtractMonthlySubsByState> createExtractMonthlySubsByStateFieldExtractorForCsv() {
        BeanWrapperFieldExtractor<ExtractMonthlySubsByState> extractor = new BeanWrapperFieldExtractor<>();
        String[] names = reportConfigProp.getCsvProperty("detailnames").split(",", -1);
        extractor.setNames(names);
        return extractor;
    }
}