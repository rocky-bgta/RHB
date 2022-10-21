package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.ExtractSmsOtpNotificationParameter.OUTPUT_FILE_LIST;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.file.transform.LineAggregator;
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
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractSmsOtpNotificationJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.ExtractSmsOtpNotification;

@Component
@Lazy
public class ExtractSmsOtpNotificationStepBuilder  extends BaseStepBuilder {
    static final Logger logger = Logger.getLogger(ExtractSmsOtpNotificationStepBuilder.class);

    private static final String STEPNAME = "ExtractSmsOtpNotification";
    private static final String DATE_FORMAT = "yyyyMM";
    private static final String PROCESS_DATE = "ProcessDate";

    @Autowired
    private ExtractSmsOtpNotificationJobConfigProperties configProperties;
	
	/* TECH-205 : Change to new datasource (dcparchive) for LDCPM4318F - Monthly SMS OTP Notification Count job */
    @Autowired
	@Qualifier("dataSourceDCPArchive")
	private DataSource dataSourceDCPArchive;

    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;

    @Autowired
    @Qualifier(STEPNAME + ".ItemReader")
    private ItemReader<ExtractSmsOtpNotification> itemReader;

    @Autowired
    @Qualifier(STEPNAME + ".ItemProcessor")
    private ItemProcessor<ExtractSmsOtpNotification, ExtractSmsOtpNotification> itemProcessor;

    @Autowired
    @Qualifier(STEPNAME + ".ItemWriter")
    private ItemWriter<ExtractSmsOtpNotification> itemWriter;

    @Override
    @Bean
    public Step buildStep() {
    	logger.debug("buildStep()");
        return getDefaultStepBuilder(STEPNAME).<ExtractSmsOtpNotification, ExtractSmsOtpNotification>chunk(configProperties.getChunksize())
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

    @Bean(STEPNAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<ExtractSmsOtpNotification> extractDailyFirstTimeLoginJobReader(@Value("#{stepExecution}") StepExecution stepExecution
            , DataSource dataSource) throws ParseException {
    	logger.debug("extractDailyFirstTimeLoginJobReader()");
    	logger.debug("dataSource: " + dataSourceDCPArchive);
    	
        JdbcPagingItemReader<ExtractSmsOtpNotification> databaseReader = new JdbcPagingItemReader<>();
        databaseReader.setDataSource(dataSourceDCPArchive);
        databaseReader.setPageSize(configProperties.getJdbcpagingpagesize());

        PagingQueryProvider queryProvider = createQueryProvider(stepExecution);
        databaseReader.setQueryProvider(queryProvider);
        databaseReader.setRowMapper(new BeanPropertyRowMapper<>(ExtractSmsOtpNotification.class));
        logger.debug("    queryProvider: " + queryProvider);
        logger.debug("    databaseReader: " + databaseReader);
        return databaseReader;
    }

    private PagingQueryProvider createQueryProvider(StepExecution stepExecution) throws ParseException {
    	Date processDate = getProcessDate(stepExecution);
    	logger.debug("    processDate: " + processDate);
    	
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
    	logger.debug("    simpleDateFormat: " + simpleDateFormat);

    	String whereClause = "WHERE audit_month='" + simpleDateFormat.format(processDate) + "'";
    	logger.debug("    whereClause: " + whereClause);
    	
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        queryProvider.setSelectClause("SELECT *");
        queryProvider.setFromClause("FROM vw_batch_extract_monthly_sms_by_DCP_csv");
        queryProvider.setWhereClause(whereClause);
        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("audit_month", Order.ASCENDING);
        return sortConfiguration;
    }

    @Bean(STEPNAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<ExtractSmsOtpNotification, ExtractSmsOtpNotification> extractDailyFirstTimeLoginJobProcessor() {
        return extractDailyFirstTimeLogin -> extractDailyFirstTimeLogin;
    }

    @Bean(STEPNAME + ".ItemWriter")
    @StepScope
    public FlatFileItemWriter<ExtractSmsOtpNotification> extractDailyFirstTimeLoginJobWriter(@Value("#{stepExecution}") StepExecution stepExecution) throws ParseException {
    	logger.debug("extractDailyFirstTimeLoginJobWriter()");
    	logger.debug("    stepExecution: " + stepExecution);
			
    	Date processDate = getProcessDate(stepExecution);
        logger.debug("    processDate: " + processDate);
    	
        FlatFileItemWriter<ExtractSmsOtpNotification> flatFileItemWriter=new FlatFileItemWriter<ExtractSmsOtpNotification>();
        logger.debug("    flatFileItemWriter: " + flatFileItemWriter);

		String filename = configProperties.getCsvProperty("filename");
		String filenameDateFormat = configProperties.getNamedateformat();
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

        LineAggregator<ExtractSmsOtpNotification> lineAggregator = createExtractSmsOtpNotificationLineAggregatorForCsv();
        logger.debug("    lineAggregator: " + lineAggregator);
        flatFileItemWriter.setLineAggregator(lineAggregator);
        
        flatFileItemWriter.setHeaderCallback(new CsvHeaderCallback());

        return flatFileItemWriter;
    }
    
    @SuppressWarnings("unchecked")
	private void addOutputFile(StepExecution stepExecution, File file) {
    	List<File> fileList = (List<File>) stepExecution.getJobExecution().getExecutionContext().get(OUTPUT_FILE_LIST);
    	if (fileList == null) {
    		fileList = new LinkedList<File>();
    		stepExecution.getJobExecution().getExecutionContext().put(OUTPUT_FILE_LIST, fileList);
    	}
    	fileList.add(file);
    }
    
    private Date getProcessDate(StepExecution stepExecution) throws ParseException {
        Date processDate = (Date) stepExecution.getJobExecution().getExecutionContext().get(PROCESS_DATE);
        if (processDate == null) {
			String batchSystemDateStr = (String) stepExecution.getJobExecution().getExecutionContext()
					.get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			Date batchSystemDate = DateUtils.getDateFromString(batchSystemDateStr, DEFAULT_DATE_FORMAT);
			processDate = DateUtils.addMonths(batchSystemDate, -1);
			
			stepExecution.getJobExecution().getExecutionContext().put(PROCESS_DATE, processDate);
        }
        
        return processDate;
    }

    private LineAggregator<ExtractSmsOtpNotification> createExtractSmsOtpNotificationLineAggregatorForCsv() {
        FormatterLineAggregator<ExtractSmsOtpNotification> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat(configProperties.getCsvProperty("detailcolumns"));

        FieldExtractor<ExtractSmsOtpNotification> fieldExtractor = createExtractSmsOtpNotificationFieldExtractorForCsv();
        lineAggregator.setFieldExtractor(fieldExtractor);

        return lineAggregator;
    }

    private FieldExtractor<ExtractSmsOtpNotification> createExtractSmsOtpNotificationFieldExtractorForCsv() {
        BeanWrapperFieldExtractor<ExtractSmsOtpNotification> extractor = new BeanWrapperFieldExtractor<ExtractSmsOtpNotification>();
        String[] names = configProperties.getCsvProperty("detailnames").split(",", -1);
        extractor.setNames(names);
        return extractor;
    }

    class CsvHeaderCallback implements FlatFileHeaderCallback {
		@Override
		public void writeHeader(Writer writer) throws IOException {
			writer.write(configProperties.getCsvProperty("header"));
		}
    }
}
