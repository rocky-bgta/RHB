package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobPropertyFile.BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExtractDailyDataParameter.OUTPUT_FILE_LIST_FIRST_TIME_LOGIN;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
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

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractDailyFirstTimeLoginJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.ExtractDailyFirstTimeLogin;

@Component
@Lazy
public class ExtractDailyFirstTimeLoginStepBuilder  extends BaseStepBuilder {
    static final Logger logger = Logger.getLogger(ExtractDailyFirstTimeLoginStepBuilder.class);

    private static final String STEPNAME = "ExtractDailyFirstTimeLogin";
    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final String PROCESS_DATE = "ProcessDate";

    @Autowired
    private ExtractDailyFirstTimeLoginJobConfigProperties configProperties;

    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;

    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<ExtractDailyFirstTimeLogin> itemReader;

    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<ExtractDailyFirstTimeLogin, ExtractDailyFirstTimeLogin> itemProcessor;

    @Autowired
    @Qualifier(STEPNAME + ".CsvItemWriter")
    private ItemWriter<ExtractDailyFirstTimeLogin> csvItemWriter;

    @Autowired
    @Qualifier(STEPNAME + ".txtItemWriter")
    private ItemWriter<ExtractDailyFirstTimeLogin> txtItemWriter;
    
    private int recordCount;

    @Override
    @Bean
    public Step buildStep() {
    	CompositeItemWriter<ExtractDailyFirstTimeLogin> compositeItemWriter = new CompositeItemWriter<ExtractDailyFirstTimeLogin>();
    	compositeItemWriter.setDelegates(Arrays.asList(csvItemWriter, txtItemWriter));
    	
    	logger.debug("buildStep()");
        return getDefaultStepBuilder(STEPNAME).<ExtractDailyFirstTimeLogin, ExtractDailyFirstTimeLogin>chunk(configProperties.getChunksize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(compositeItemWriter)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<ExtractDailyFirstTimeLogin> extractDailyFirstTimeLoginJobReader(@Value("#{stepExecution}") StepExecution stepExecution
            , DataSource dataSource) throws ParseException {
    	logger.debug("extractDailyFirstTimeLoginJobReader()");
    	logger.debug("    dataSource: " + dataSource);
    	
        JdbcPagingItemReader<ExtractDailyFirstTimeLogin> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSource);
        databaseReader.setPageSize(configProperties.getJdbcpagingpagesize());

        PagingQueryProvider queryProvider = createQueryProvider(stepExecution);
        databaseReader.setQueryProvider(queryProvider);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(ExtractDailyFirstTimeLogin.class));
        logger.debug("    queryProvider: " + queryProvider);
        logger.debug("    databaseReader: " + databaseReader);
        return databaseReader;
    }

    private PagingQueryProvider createQueryProvider(StepExecution stepExecution) throws ParseException {
    	Date processDate = getProcessDate(stepExecution);
    	logger.debug("createQueryProvider  processDate: " + processDate);
    	
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
    	logger.debug("createQueryProvider simpleDateFormat: " + simpleDateFormat);

    	String whereClause = "where audit_timestamp >= '" + simpleDateFormat.format(processDate) +
                "' AND audit_timestamp < dateadd(day, 1, '" + simpleDateFormat.format(processDate) + "')";
    	logger.debug("createQueryProvider whereClause: " + whereClause);

        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM vw_batch_extract_daily_first_login_data");
        queryProvider.setWhereClause(whereClause);
        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("date", Order.ASCENDING);
        sortConfiguration.put("time", Order.ASCENDING);
        return sortConfiguration;
    }

    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<ExtractDailyFirstTimeLogin, ExtractDailyFirstTimeLogin> extractDailyFirstTimeLoginJobProcessor() {
        return extractDailyFirstTimeLogin -> {
            recordCount++;
            return extractDailyFirstTimeLogin;
        };
    }

    @Bean(STEPNAME + ".CsvItemWriter")
    @StepScope
    public FlatFileItemWriter<ExtractDailyFirstTimeLogin> extractDailyFirstTimeLoginJobWriterForCsv(@Value("#{stepExecution}") StepExecution stepExecution) throws ParseException {
    	logger.debug("extractDailyFirstTimeLoginJobWriterForCsv()");
    	logger.debug("    stepExecution: " + stepExecution);
    	
    	Date processDate = getProcessDate(stepExecution);
        logger.debug("    processDate: " + processDate);
    	
        FlatFileItemWriter<ExtractDailyFirstTimeLogin> flatFileItemWriter=new FlatFileItemWriter<ExtractDailyFirstTimeLogin>();
        logger.debug("    flatFileItemWriter: " + flatFileItemWriter);

		String filename = configProperties.getCsvProperty("filename");
		String filenameDateFormat = configProperties.getNamedateformat();
        String targetFileNewName = filename.replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, DateUtils.formatDateString(processDate, filenameDateFormat));
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

        LineAggregator<ExtractDailyFirstTimeLogin> lineAggregator = createExtractDailyFirstTimeLoginLineAggregatorForCsv();
        logger.debug("    lineAggregator: " + lineAggregator);
        flatFileItemWriter.setLineAggregator(lineAggregator);
        
        flatFileItemWriter.setHeaderCallback(new CsvHeaderCallback());

        return flatFileItemWriter;
    }

    @Bean(STEPNAME + ".txtItemWriter")
    @StepScope
    public FlatFileItemWriter<ExtractDailyFirstTimeLogin> extractDailyFirstTimeLoginJobWriterForTxt(@Value("#{stepExecution}") StepExecution stepExecution) throws ParseException {
    	logger.debug("extractDailyFirstTimeLoginJobWriterForTxt()");
    	logger.debug("    stepExecution: " + stepExecution);
    	
    	Date processDate = getProcessDate(stepExecution);
        logger.debug("    processDate: " + processDate);
    	
        FlatFileItemWriter<ExtractDailyFirstTimeLogin> flatFileItemWriter=new FlatFileItemWriter<ExtractDailyFirstTimeLogin>();
        logger.debug("    flatFileItemWriter: " + flatFileItemWriter);

		String filename = configProperties.getTxtProperty("filename");
		String filenameDateFormat = configProperties.getNamedateformat();
        String targetFileNewName = filename.replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, DateUtils.formatDateString(processDate, filenameDateFormat));
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

        LineAggregator<ExtractDailyFirstTimeLogin> lineAggregator = createExtractDailyFirstTimeLoginLineAggregatorForTxt();
        logger.debug("    lineAggregator: " + lineAggregator);
        flatFileItemWriter.setLineAggregator(lineAggregator);
        
        flatFileItemWriter.setHeaderCallback(new TxtHeaderCallback(processDate, filenameDateFormat));
        flatFileItemWriter.setFooterCallback(new TxtFooterCallback());

        return flatFileItemWriter;
    }
    
    @SuppressWarnings("unchecked")
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
        logger.info("ExtractDailyFirstTimeLoginStepBuilder - getProcessDate - executionContextProcessDate: " + executionContextProcessDate);
        if(executionContextProcessDate != null) {
            // continuous
            processDate = executionContextProcessDate;
        } else {
            // retrieve from JobParameter
            String externalDate = stepExecution.getJobParameters().getString(PROCESS_DATE);
            logger.info("ExtractDailyFirstTimeLoginStepBuilder - getProcessDate - jobparameter externalDate: " + externalDate);
            if(externalDate !=  null) {
                processDate = (Date)new SimpleDateFormat("yyyy-MM-dd").parse(externalDate);
            }

            // retrieve from Config table
            if (processDate == null) {
                String batchSystemDateStr = (String) stepExecution.getJobExecution().getExecutionContext()
                        .get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
                logger.info("ExtractDailyFirstTimeLoginStepBuilder - getProcessDate - batchSystemDateStr: " + batchSystemDateStr);
                Date batchSystemDate = DateUtils.getDateFromString(batchSystemDateStr, DEFAULT_DATE_FORMAT);
                processDate = DateUtils.addDays(batchSystemDate, -1);

                stepExecution.getJobExecution().getExecutionContext().put(PROCESS_DATE, processDate);
            }
        }
        logger.info("ExtractDailyFirstTimeLoginStepBuilder - getProcessDate - final process date: "+processDate);
        return processDate;
    }

    private LineAggregator<ExtractDailyFirstTimeLogin> createExtractDailyFirstTimeLoginLineAggregatorForCsv() {
        FormatterLineAggregator<ExtractDailyFirstTimeLogin> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat(configProperties.getCsvProperty("detailcolumns"));

        FieldExtractor<ExtractDailyFirstTimeLogin> fieldExtractor = createExtractDailyFirstTimeLoginFieldExtractorForCsv();
        lineAggregator.setFieldExtractor(fieldExtractor);

        return lineAggregator;
    }

    private FieldExtractor<ExtractDailyFirstTimeLogin> createExtractDailyFirstTimeLoginFieldExtractorForCsv() {
        BeanWrapperFieldExtractor<ExtractDailyFirstTimeLogin> extractor = new BeanWrapperFieldExtractor<ExtractDailyFirstTimeLogin>();
        String[] names = configProperties.getCsvProperty("detailnames").split(",", -1);
        extractor.setNames(names);
        return extractor;
    }

    private LineAggregator<ExtractDailyFirstTimeLogin> createExtractDailyFirstTimeLoginLineAggregatorForTxt() {
        FormatterLineAggregator<ExtractDailyFirstTimeLogin> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat(configProperties.getTxtProperty("detailcolumns"));

        FieldExtractor<ExtractDailyFirstTimeLogin> fieldExtractor = createExtractDailyFirstTimeLoginFieldExtractorForTxt();
        lineAggregator.setFieldExtractor(fieldExtractor);

        return lineAggregator;
    }

    private FieldExtractor<ExtractDailyFirstTimeLogin> createExtractDailyFirstTimeLoginFieldExtractorForTxt() {
        BeanWrapperFieldExtractor<ExtractDailyFirstTimeLogin> extractor = new BeanWrapperFieldExtractor<ExtractDailyFirstTimeLogin>();
        String[] names = configProperties.getTxtProperty("detailnames").split(",", -1);
        extractor.setNames(names);
        return extractor;
    }
    
    class CsvHeaderCallback implements FlatFileHeaderCallback {
		@Override
		public void writeHeader(Writer writer) throws IOException {
			writer.write(configProperties.getCsvProperty("header"));
		}
    }
    
    class TxtHeaderCallback implements FlatFileHeaderCallback {
    	private Date processDate;
    	private String filenameDateFormat;

    	public TxtHeaderCallback(Date processDate, String filenameDateFormat) {
    		this.processDate = processDate;
    		this.filenameDateFormat = filenameDateFormat;
    	}

		@Override
		public void writeHeader(Writer writer) throws IOException {
			String header = configProperties.getTxtProperty("header").replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, DateUtils.formatDateString(processDate, filenameDateFormat));
			writer.write(header);
		}
    }
    
    class TxtFooterCallback implements FlatFileFooterCallback {
		@Override
		public void writeFooter(Writer writer) throws IOException {
			String footer = configProperties.getTxtProperty("footer").replace("{#count}", String.format("%06d", recordCount));
			writer.write(footer);
		}
    }
}
