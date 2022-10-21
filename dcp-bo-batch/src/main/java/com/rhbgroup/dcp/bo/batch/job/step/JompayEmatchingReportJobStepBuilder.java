package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_PAYMENT_METHOD_CREDIT_CARD;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_PAYMENT_METHOD_CURRENT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_PAYMENT_METHOD_DEBIT_CARD;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_PAYMENT_METHOD_SAVINGS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.JompayEmatchingParameter.JOMPAY_OUTBOUND_APP_ID_CARD;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.JompayEmatchingParameter.JOMPAY_OUTBOUND_APP_ID_CURRENT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.JompayEmatchingParameter.JOMPAY_OUTBOUND_APP_ID_SAVINGS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.JompayEmatchingParameter.JOMPAY_OUTBOUND_TXT_FILE;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

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
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileFooterCallback;
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
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.JompayEmatchingReportOutDetail;
import com.rhbgroup.dcp.bo.batch.job.model.JompayEmatchingReportPaymentTxn;

@Component
@Lazy
public class JompayEmatchingReportJobStepBuilder extends BaseStepBuilder{
	
	static final Logger logger = Logger.getLogger(JompayEmatchingReportJobStepBuilder.class);
	static final String STEP_NAME="JompayEmatchingReportJobStep";
	
    @Value("${job.jompayematchingreportjob.bankcodeibg}")
	private String RHB_IBG_CODE;
    
    @Value("${job.jompayematchingreportjob.chunksize}")
    private int chunksize;
    
    @Value("${job.jompayematchingreportjob.name}")
    private String targetFileName;
    
    @Value("${job.jompayematchingreportjob.namedateformat}")
    private String targetFileDateFormat;
    
    @Value("${job.jompayematchingreportjob.detailcolumns}")
    private String fileContentColumns;
    
	@Value("${job.jompayematchingreportjob.detailnames}")
    private String fileContentNames;

    
    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
    
    @Autowired
    @Qualifier("JompayEmatchingReportJobStep.ItemReader")
    private ItemReader<JompayEmatchingReportPaymentTxn> itemReader;
    
    @Autowired
    @Qualifier("JompayEmatchingReportJobStep.ItemProcessor")
    private ItemProcessor<JompayEmatchingReportPaymentTxn, JompayEmatchingReportOutDetail> itemProcessor;
    
    @Autowired
    @Qualifier("JompayEmatchingReportJobStep.ItemWriter")
    private ItemWriter<JompayEmatchingReportOutDetail> itemWriter;
    
    double totalTxnAmount=0.00;
    int totalCount=0;
    String processingDateStr;
    String batchSystemDate;
	//TODO item reader, query database
	@Bean("JompayEmatchingReportJobStep.ItemReader")
    @StepScope
	public JdbcPagingItemReader<JompayEmatchingReportPaymentTxn> jompayReader (
			@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) {
		JdbcPagingItemReader<JompayEmatchingReportPaymentTxn> databaseReader = new JdbcPagingItemReader<>();
		String logMsg = "";
		try {
			databaseReader.setDataSource(dataSource);
			databaseReader.setPageSize(chunksize);
			batchSystemDate=stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			Date processingDate = BatchUtils.getProcessingDate(batchSystemDate, BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT) ;
			processingDateStr = DateUtils.formatDateString(processingDate, targetFileDateFormat);
			logMsg = String.format("JompayEmatchingReportJobStep Item Reader Getting records for item readers batch.System.Date=%s, processingDateStr=%s",
					batchSystemDate, processingDateStr );
			logger.info(logMsg);
			String txnDateStr = DateUtils.formatDateString(processingDate, DEFAULT_JOB_PARAMETER_DATE_FORMAT);
			logMsg = String.format("JompayEmatchingReportJobStep Item Reader txnDateStr=%s",txnDateStr);
			logger.info(logMsg);
			totalTxnAmount=0.00;
			totalCount=0;
			Map parameters = new HashMap<String, String>();
			parameters.put("txnDateStr", txnDateStr);
			logMsg = String.format("JompayEmatchingReportJobStep Item Reader building parameter:%s", parameters );
			logger.info(logMsg);

			PagingQueryProvider queryProvider = createQueryProvider();
			databaseReader.setQueryProvider(queryProvider);
			databaseReader.setParameterValues(parameters);
			databaseReader.setRowMapper(new BeanPropertyRowMapper<>(JompayEmatchingReportPaymentTxn.class));
			logger.info("JompayEmatchingReportJobStep Item Reader building data reader");
		}catch(Exception ex) {
			logMsg = String.format("JompayEmatchingReportJobStep Item Reader exception:%s", ex.getMessage());
			logger.info(logMsg);
			logger.error(ex);
		}
		return databaseReader;
	}
	
    private PagingQueryProvider createQueryProvider() {
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        String sqlSelect ;
        String fromClause;
        String whereClause;
        sqlSelect ="select channel_id" + 
        		" , channel_status" + 
        		" , application_id" + 
        		" , acct_ctrl1" + 
        		" , acct_ctrl2" + 
        		" , acct_ctrl3" + 
        		" , account_no" + 
        		" , debit_credit_ind" + 
        		" , user_tran_code" + 
        		" , amount" + 
        		" , txn_branch" + 
        		" , txn_date" + 
        		" , txn_time" ; 
        fromClause= " from vw_batch_merged_jompay_ematching"; 
        whereClause= " where txn_date = :txnDateStr ";
        logger.info(String.format("Query tbl payment txn select sql= %s %s %s", sqlSelect , fromClause , whereClause));
        queryProvider.setSelectClause(sqlSelect);
        queryProvider.setFromClause(fromClause);
        queryProvider.setWhereClause(whereClause);
        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }
    
    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("txn_date", Order.ASCENDING);
        return sortConfiguration;
    }    
	//item reader
    
	//TODO item processor, map database record to output line
	@Bean("JompayEmatchingReportJobStep.ItemProcessor")
    @StepScope
	public ItemProcessor<JompayEmatchingReportPaymentTxn, JompayEmatchingReportOutDetail> jompayProcessor(
			  @Value("#{stepExecution}") StepExecution stepExecution) {
        return jompayDetailTxn -> {
			JompayEmatchingReportOutDetail jompayOutDetail=null;
			try {
				logger.info(String.format("JompayEmatchingReportJobStep ItemProcessor account no=%s",jompayDetailTxn.getAccountNo()));
				jompayOutDetail = new JompayEmatchingReportOutDetail();
				jompayOutDetail.setChannelId(jompayDetailTxn.getChannelId());
				jompayOutDetail.setChannelStatus(jompayDetailTxn.getChannelStatus());
				jompayOutDetail.setApplicationId(jompayDetailTxn.getApplicationId());
				jompayOutDetail.setAcctControlOne(jompayDetailTxn.getAcctCtrl1());
				jompayOutDetail.setAcctControlTwo(jompayDetailTxn.getAcctCtrl2());
				jompayOutDetail.setAcctControlThree(jompayDetailTxn.getAcctCtrl3());
				jompayOutDetail.setAccountNo(jompayDetailTxn.getAccountNo());
				jompayOutDetail.setDebitCreditInd(jompayDetailTxn.getDebitCreditInd());
				jompayOutDetail.setUserTransCode(jompayDetailTxn.getUserTranCode());
				String amountStr = StringUtils.leftPad( String.format("%.2f",jompayDetailTxn.getAmount()), 18, "0") ;
				jompayOutDetail.setTransactionAmount(amountStr.concat("+"));
				jompayOutDetail.setTransactionBranch(jompayDetailTxn.getTxnBranch());
				jompayOutDetail.setTransactionDate(jompayDetailTxn.getTxnDate());
				jompayOutDetail.setTransactionTime(jompayDetailTxn.getTxnTime().replaceAll(":", ""));
				totalTxnAmount = totalTxnAmount + jompayDetailTxn.getAmount();
				totalCount = totalCount + 1;
			}catch(Exception ex) {
				logger.info(String.format("JompayEmatchingReportJobStep ItemProcessor account no=%s",jompayDetailTxn.getAccountNo()));
				logger.error(ex);
			}
			return jompayOutDetail;
		};
	}
    
	
	//writer
	@Bean("JompayEmatchingReportJobStepHeader")
	@StepScope
	private FlatFileHeaderCallback getHeaderCallback(@Value("#{stepExecution}") StepExecution stepExecution) {
		return new FlatFileHeaderCallback() {
			@Override
			public void writeHeader(Writer writer) throws IOException {
				logger.info("JompayEmatchingReportJob Step prepare Header");
				try {
					String processingDate = DateUtils.convertDateFormat(processingDateStr, targetFileDateFormat, DEFAULT_JOB_PARAMETER_DATE_FORMAT);
					String systemDate = DateUtils.formatDateString(new Date(), DEFAULT_JOB_PARAMETER_DATE_FORMAT);
					String systemTime = DateUtils.formatDateString(new Date(), "HHmmss");
					String filler;
					filler = StringUtils.rightPad(" ",196, ' ');
					writer.append("0")
							.append("RFEMATCH").append(" ")
							.append(processingDate).append(" ")
							.append(systemDate).append(" ")
							.append(systemTime).append(" ")
							.append(StringUtils.rightPad("DMB E-MATCH",20,' ')).append(filler);
				}catch(ParseException ex) {
					logger.info("JompayEmatchingReportJob Step Header parse exception");
					logger.error(ex);
				}
			}
		};
	}
	
	@Bean("JompayEmatchingReportJobStepFooter")
	@StepScope
	private FlatFileFooterCallback getFooterCallback(@Value("#{stepExecution}")  StepExecution stepExecution) {
	    return new FlatFileFooterCallback() {
	        @Override
	        public void writeFooter(Writer writer) throws IOException {
				String totalAmtStr = String.format("%.2f",totalTxnAmount) ;
	            String filler="";
				writer.append("9")
						.append(StringUtils.leftPad(String.valueOf(totalCount), 18, '0'))
						.append(" ")
						.append(StringUtils.leftPad(totalAmtStr, 18, '0'))
						.append(StringUtils.leftPad(filler, 213, ' '));
	        }
	    };
	}
	
	@Bean("JompayEmatchingReportJobStep.ItemWriter")
	@StepScope
	public FlatFileItemWriter<JompayEmatchingReportOutDetail> jompayWriter(
			@Value("#{stepExecution}") StepExecution stepExecution) {
		FlatFileItemWriter<JompayEmatchingReportOutDetail> flatFileItemWriter = new FlatFileItemWriter();
		String logMsg="";
		try {
			String jobname=stepExecution.getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
			logger.info(String.format("JompayEmatchingReportJobStep Item Writer jobname=%s", jobname));
            flatFileItemWriter.setHeaderCallback(getHeaderCallback(stepExecution));
            flatFileItemWriter.setFooterCallback(getFooterCallback(stepExecution));
            flatFileItemWriter.setLineSeparator("\r\n");
            String targetFileNewName = targetFileName.replace("{#date}", processingDateStr);
            logger.info(String.format("writing file folder=%s, file=%s", outputFolderFullPath, targetFileNewName));
            String outputJobFolder = outputFolderFullPath.concat("\\").concat(jobname);
            String outputFolder = StringUtils.replace(outputJobFolder, "\\", File.separator);
            File outputLocalFolder = new File (outputFolder);
            if(!outputLocalFolder.exists()) {
            	outputLocalFolder.mkdirs();
            	logMsg = String.format("Output folder does not exist, create folder %s", outputFolder);
            	logger.info(logMsg);
            }
            
            logger.info(String.format("writing output folder=%s", outputFolder));
            File targetFileFullPath=Paths.get(outputFolder,targetFileNewName).toFile();
            flatFileItemWriter.setResource(new FileSystemResource(targetFileFullPath.getPath()));
            LineAggregator<JompayEmatchingReportOutDetail> lineAggregator = createBillerLineAggregator();
            flatFileItemWriter.setLineAggregator(lineAggregator);
            stepExecution.getJobExecution().getExecutionContext().put(JOMPAY_OUTBOUND_TXT_FILE, targetFileFullPath.getAbsolutePath());
            logger.info(String.format("Complete writing file=%s",targetFileFullPath.getAbsolutePath() ) );

		}catch(Exception ex) {
			logMsg=String.format("JompayEmatchingReportJobStep Item Writer exception=%s",ex.getMessage());
			logger.info(logMsg);
		}
		return flatFileItemWriter;
	}
	
	private LineAggregator<JompayEmatchingReportOutDetail> createBillerLineAggregator() {
        FormatterLineAggregator<JompayEmatchingReportOutDetail> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat(fileContentColumns);
        FieldExtractor<JompayEmatchingReportOutDetail> fieldExtractor = createBillerFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    private FieldExtractor<JompayEmatchingReportOutDetail> createBillerFieldExtractor() {
        BeanWrapperFieldExtractor<JompayEmatchingReportOutDetail> extractor = new BeanWrapperFieldExtractor();
        String[] names=fileContentNames.split(",", -1);
        extractor.setNames(names);
        return extractor;
    }
	//writer

	@Override
    @Bean(STEP_NAME)
	public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME)
				.<JompayEmatchingReportPaymentTxn, JompayEmatchingReportOutDetail>chunk(1000)
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.build();
	}
}
