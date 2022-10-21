package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobPropertyFile.BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.enums.DateRange;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.JompayFailureValidationExtractionJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedJompayFailureTxn;
import com.rhbgroup.dcp.bo.batch.job.rowmapper.BatchStagedJompayFailureTxnRowMapper;

@Component
@Lazy
public class FailedJompayTxnsFromDBToFileStepBuider extends BaseStepBuilder {

	private static final Logger logger = Logger.getLogger(FailedJompayTxnsFromDBToFileStepBuider.class);
	
	private static final String STEP_NAME = "FailedJompayTxnsFromDBToFileStep";
	
	private static final int FIELD_BILLER_CODE_LENGTH = 10;
	private static final int FIELD_PAYMENT_CHANNEL_LENGTH = 5;
	private static final int FIELD_REASON_FOR_FAILURE_LENGTH = 30;
	private static final int FIELD_FROM_BANK_ID_LENGTH = 10;
	
	private static final String FIELD_REQUEST_TIME_FORMAT = "yyMMddHHmmss";
	
	private static final String FILE_HEADER_TEMPLATE = "%s%s\n";
	
	@Value(SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderPath;
	
	@Autowired
	private JompayFailureValidationExtractionJobConfigProperties jobConfigProperties;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<BatchStagedJompayFailureTxn> itemReader;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<BatchStagedJompayFailureTxn, BatchStagedJompayFailureTxn> itemProcessor;
	   
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchStagedJompayFailureTxn> itemWriter;

	@Bean(STEP_NAME + ".ItemReader")
    @StepScope
    public JdbcPagingItemReader<BatchStagedJompayFailureTxn> batchStagedIBGRejectTxnItemReader(@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) throws BatchException {
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		
		// Create the failed transaction file with header
		createInitFile(stepExecution);
		
		LocalDateTime[] fromToDateTimes = getFromDateToDate(stepExecution);
		LocalDateTime fromDateTime = fromToDateTimes[0];
        LocalDateTime toDateTime = fromToDateTimes[1];
		
		// Create the reader to fetch required records from DB
		JdbcPagingItemReader<BatchStagedJompayFailureTxn> jdbcPagingItemReader = new JdbcPagingItemReader<>();
        jdbcPagingItemReader.setDataSource(dataSource);
        jdbcPagingItemReader.setPageSize(jobConfigProperties.getJdbcPagingSize());

        String selectClause = "SELECT BILLER_CODE, PAYMENT_CHANNEL, REASON_FOR_FAILURE, REQUEST_TIME";
        String fromClause = "FROM VW_BATCH_JOMPAY_FAILURE_VALIDATION";
        String whereClause = "WHERE REQUEST_TIME >= :fromDateTime AND REQUEST_TIME < :toDateTime";
        
        toDateTime = toDateTime.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        // SQL select paging query
        SqlServerPagingQueryProvider selectPagingQueryProvider = new SqlServerPagingQueryProvider();
        selectPagingQueryProvider.setSelectClause(selectClause);
        selectPagingQueryProvider.setFromClause(fromClause);
        selectPagingQueryProvider.setWhereClause(whereClause);
        logger.debug(String.format("SQL select paging query [%s %s %s] using parameters FromDate [%s], ToDate [%s]", selectClause, fromClause, whereClause, fromDateTime, toDateTime));
        
        // Sorting keys using in SQL select paging query
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("BILLER_CODE", Order.ASCENDING);
        selectPagingQueryProvider.setSortKeys(sortKeys);
        
        // The parameters used to replace in the SQL select paging query
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("fromDateTime", fromDateTime);
        parameterValues.put("toDateTime", toDateTime);
        
        // Setting the SQL paging select query, parameters used, and row mapper for the reader
        jdbcPagingItemReader.setQueryProvider(selectPagingQueryProvider);
        jdbcPagingItemReader.setParameterValues(parameterValues);
        jdbcPagingItemReader.setRowMapper(new BatchStagedJompayFailureTxnRowMapper());
        
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));
        return jdbcPagingItemReader;
	}
	
	@Bean(STEP_NAME + ".ItemProcessor")
    @StepScope
    public ItemProcessor<BatchStagedJompayFailureTxn, BatchStagedJompayFailureTxn> batchStagedIBGRejectTxnItemProcessor(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
		return new ItemProcessor<BatchStagedJompayFailureTxn, BatchStagedJompayFailureTxn>() {
			@Override
			public BatchStagedJompayFailureTxn process(BatchStagedJompayFailureTxn batchStagedJompayFailureTxn) throws Exception {
				logger.trace(String.format("BatchStagedJompayFailureTxn received by processor [%s]", batchStagedJompayFailureTxn));
				return batchStagedJompayFailureTxn;
			} 
		};
	}
	
	@Bean(STEP_NAME + ".ItemWriter")
    @StepScope
    public ItemWriter<BatchStagedJompayFailureTxn> batchStagedIBGRejectTxnItemWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));
		return new ItemWriter<BatchStagedJompayFailureTxn>() {
			@Override
			public void write(List<? extends BatchStagedJompayFailureTxn> batchStagedJompayFailureTxns) throws IOException {
				logger.debug(String.format("Generate FTP file for total [%d] BatchStagedJompayFailureTxns", batchStagedJompayFailureTxns.size()));
				
				String batchJobName = stepExecution.getJobExecution().getJobParameters().getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY);
	            File targetFolder = Paths.get(outputFolderPath, batchJobName).toFile();

	            if (!targetFolder.exists()) {
	            	logger.debug(String.format("Target folder [%s] doesn't exists, create it", targetFolder));
	            	targetFolder.mkdirs();
	            }
	            
	            Date currentDate = new Date();
	            String batchSystemDate = DateUtils.formatDateString(currentDate, jobConfigProperties.getNameDateFormat());
	            String filename = jobConfigProperties.getName().replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, batchSystemDate);
	            File targetFile = Paths.get(targetFolder.getAbsolutePath(), filename).toFile();
	            
				for(BatchStagedJompayFailureTxn batchStagedJompayFailureTxn : batchStagedJompayFailureTxns) {
					logger.trace(String.format("BatchStagedJompayFailureTxn [%s]", batchStagedJompayFailureTxn));
					StringBuilder strBuilder = new StringBuilder();
					strBuilder.append(StringUtils.rightPad(batchStagedJompayFailureTxn.getBillerCode(), FIELD_BILLER_CODE_LENGTH));
					strBuilder.append(StringUtils.rightPad(batchStagedJompayFailureTxn.getPaymentChannel(), FIELD_PAYMENT_CHANNEL_LENGTH));
					strBuilder.append(DateUtils.formatDateString(batchStagedJompayFailureTxn.getRequestTime(), FIELD_REQUEST_TIME_FORMAT));
					strBuilder.append(StringUtils.rightPad(batchStagedJompayFailureTxn.getReasonForFailure(), FIELD_REASON_FOR_FAILURE_LENGTH));
					strBuilder.append("\n");
					String detail = strBuilder.toString();
					logger.trace(String.format("File detail [%s]", detail));
					
					FileUtils.write(targetFile, detail, Charset.defaultCharset(), true);
				}
				String emptyLine="\n";
				FileUtils.write(targetFile, emptyLine, Charset.defaultCharset(), true);
				logger.debug(String.format("Storing target output filepath [%s] to context", targetFile.getAbsolutePath()));
				stepExecution.getJobExecution().getExecutionContext().putString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY, targetFile.getAbsolutePath());
				stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, DateUtils.formatDateString(currentDate, "yyyy-MM-dd"));
			}
		};
	}
	
	private LocalDateTime[] getFromDateToDate(StepExecution stepExecution) throws BatchException {
		LocalDateTime[] fromToDateTimes = null;
		
		// Getting date info from context
		String batchProcessFromToDateStr = stepExecution.getJobExecution().getJobParameters().getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_FROM_TO_DATE_KEY);
		logger.debug(String.format("Job parameter processing FromDate and ToDate from context [%s]", batchProcessFromToDateStr));
		
		// If dates info found in context proceed here
		if(!StringUtils.isEmpty(batchProcessFromToDateStr)) {
			logger.debug(String.format("Job parameter processing date range [%s]", batchProcessFromToDateStr));
			try {
				fromToDateTimes = DateUtils.getJobParameterFromToDateTimes(batchProcessFromToDateStr);
			} catch (IllegalArgumentException | DateTimeParseException e) {
				String errorMessage = String.format("Error happened while trying to get FromDate and ToDate from job parameters [%s]", batchProcessFromToDateStr);
				logger.error(errorMessage, e);
				throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, e);
			}
			// If dates info not found in context, get the dates info from DB	
		} else {
			String batchSystemDateStr= stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			logger.debug(String.format("Job parameter system date [%s]", batchSystemDateStr));
			try {
				LocalDate batchSystemDate = DateUtils.getLocalDateFromString(batchSystemDateStr, General.DEFAULT_DATE_FORMAT);
				fromToDateTimes = new LocalDateTime[2];
				// Set the time to be starting of the day
				fromToDateTimes[0] = batchSystemDate.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)).atTime(0, 0, 0, 0);
				// Set the time to be close to end of the day
				fromToDateTimes[1] = batchSystemDate.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY)).plusDays(1).atTime(0, 0, 0, 0).minusNanos(1);
				
				if(fromToDateTimes[0].isAfter(fromToDateTimes[1])) {
					fromToDateTimes[0] = fromToDateTimes[0].with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
				}
			} catch (DateTimeParseException e) {
				String errorMessage = String.format("Error happened while trying to get FromDate and ToDate from DB job parameters [%s]", batchProcessFromToDateStr);
				logger.error(errorMessage, e);
				throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE, e);
			}
		}
		
		if(!DateUtils.isValidFromToDateTimes(fromToDateTimes[0], fromToDateTimes[1], DateRange.WEEKLY)) {
			throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR, BatchErrorCode.CONFIG_SYSTEM_ERROR_MESSAGE);
		}
		
		logger.debug(String.format("Final FromDate and ToDate [%s]", Arrays.toString(fromToDateTimes)));
		return fromToDateTimes;
	}
	
	private void createInitFile(StepExecution stepExecution) throws BatchException {
		String batchJobName = stepExecution.getJobExecution().getJobParameters().getString(BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY);
		
		// Getting the target input folder path appened with job name if not exists
        File targetFolder = Paths.get(outputFolderPath, batchJobName).toFile();
        if (!targetFolder.exists()) {
        	logger.debug(String.format("Target folder [%s] doesn't exists, create it", targetFolder));
        	targetFolder.mkdirs();
        }
        
        String batchSystemDate = DateUtils.formatDateString(new Date(), jobConfigProperties.getNameDateFormat());
        String filename = jobConfigProperties.getName().replace(BATCH_JOB_PROPERTY_FILE_NAME_DATETIME_PLACEHOLDER, batchSystemDate);
        
        File targetFile = Paths.get(targetFolder.getAbsolutePath(), filename).toFile();
        if(targetFile.exists()) {
        	String message = String.format("File [%s] exists, remove it and create new later", targetFile.getAbsolutePath());
        	logger.debug(message);
        	FileUtils.deleteQuietly(targetFile);
        }
        
        String fromBankId = StringUtils.rightPad(jobConfigProperties.getBankId(), FIELD_FROM_BANK_ID_LENGTH);
		String header = String.format(FILE_HEADER_TEMPLATE, batchSystemDate, fromBankId);
		
		try {
			logger.debug(String.format("Write header [%s] to the file [%s]", header, targetFile.getAbsolutePath()));
			FileUtils.write(targetFile, header, Charset.defaultCharset(), true);
		} catch (IOException e) {
			String errorMessage = String.format("Failed to write header to the file [%s]", targetFile.getAbsolutePath());
			logger.error(errorMessage, e);
			throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, BatchErrorCode.GENERIC_SYSTEM_ERROR_MESSAGE);
		}
		
		logger.debug(String.format("Storing target output filepath [%s] to context", targetFile.getAbsolutePath()));
		stepExecution.getJobExecution().getExecutionContext().putString(BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY, targetFile.getAbsolutePath());
	}
	
	@Override
	public Step buildStep() {
		logger.info(String.format("Building step [%s]", STEP_NAME));
		
		Step step = getDefaultStepBuilder(STEP_NAME).<BatchStagedJompayFailureTxn, BatchStagedJompayFailureTxn>chunk(jobConfigProperties.getChunkSize())
            .reader(itemReader)
            .processor(itemProcessor)
            .writer(itemWriter)
            .build();
		
		logger.info(String.format("[%s] step build successfully", STEP_NAME));
		return step;
	}
}