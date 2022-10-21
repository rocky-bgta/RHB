package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobContextParameter.BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BillerPaymentOutbound.STEP_EXECUTION_STATUS;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT; 
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_JOB_PARAMETER_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.SystemFolder.BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY; 

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

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

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.BillerPaymentFileJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundDetail;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundTxn;

@Component
@Lazy
public class BillerPaymentFileJobBaseStepBuilder extends BaseStepBuilder{

	private static final Logger logger = Logger.getLogger(BillerPaymentFileJobBaseStepBuilder.class);
	private static final String STEP_NAME="BillerPaymentFileJob";
	
	@Autowired
	private BillerPaymentFileJobConfigProperties configProperties;
	
    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
    

    @Autowired
    @Qualifier(STEP_NAME+".ItemReader")
    private ItemReader<BillerPaymentOutboundTxn> itemReader;
    
    @Autowired
    @Qualifier(STEP_NAME+".ItemProcessor")
    private ItemProcessor<BillerPaymentOutboundTxn, BillerPaymentOutboundDetail> itemProcessor;
    
    @Autowired
    @Qualifier(STEP_NAME+".ItemWriter")
    private ItemWriter<BillerPaymentOutboundDetail> itemWriter;
	
	
	@Autowired
	@Qualifier("BillPaymentConfigOutboundQueue")
	private Queue<BillerPaymentOutboundConfig> queue ;
	
	protected double hashTotal;
    protected String billerCode="";
	protected BillerPaymentOutboundConfig billerConfig;
	protected static final String JOB_NAME="BillerPaymentFileJob";

	private String processingDateStr = "";
	private int trailerCount;
	private double trailerAmount;
	private BigDecimal hashA;
	private int hashB;
	protected Date batchProcessingDate;
	protected Date fileNameSystemDate;
	
	@Override
    @Bean
    public Step buildStep() {		
		return getDefaultStepBuilder(STEP_NAME).<BillerPaymentOutboundTxn,BillerPaymentOutboundDetail>chunk(100)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

	//reader
	@Bean(STEP_NAME+".ItemReader")
    @StepScope
	public JdbcPagingItemReader<BillerPaymentOutboundTxn> billerReader (
			@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) {
		JdbcPagingItemReader<BillerPaymentOutboundTxn> databaseReader = new JdbcPagingItemReader<>();
		try {

			databaseReader.setDataSource(dataSource);
			databaseReader.setPageSize(configProperties.getChunkSize());
			
			trailerCount=0;
			trailerAmount=0.00;
			hashTotal=0.00;
			hashA = new BigDecimal(0);
			hashB = 0;
			
			billerConfig = queue.element();

			String message = String.format("Biller payment file reader biller code=%s,account no=%s,name=%s", billerConfig.getBillerCode(), billerConfig.getBillerAccNo(), billerConfig.getBillerAccName() );
			logger.info(message);
			String batchSystemDate=stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT) ;
			processingDateStr = DateUtils.formatDateString(batchProcessingDate ,DEFAULT_JOB_PARAMETER_DATE_FORMAT);
			fileNameSystemDate = batchProcessingDate;
			message=String.format("batch Processing Date =%s, processing Date string=%s",batchProcessingDate,processingDateStr);
			logger.info(message);
			billerCode = billerConfig.getBillerCode();
			message=String.format("Biller payment outbound file query data for biller code=%s", billerCode );
			logger.info(message);
			Map parameters = new HashMap<String, String>();
			parameters.put("billerCode", billerCode);
			parameters.put("txnDate", processingDateStr);
			
			PagingQueryProvider queryProvider = createQueryProvider();
			databaseReader.setQueryProvider(queryProvider);
			databaseReader.setParameterValues( parameters );
			databaseReader.setRowMapper(new BeanPropertyRowMapper<>(BillerPaymentOutboundTxn.class));
		}catch(Exception ex ) {
			stepExecution.getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS,BatchSystemConstant.ExitCode.FAILED);
			String message = String.format("Biller payment item reader exception %s", ex.getMessage()) ;
			logger.info(message);
		}
		return databaseReader;
	}

    private PagingQueryProvider createQueryProvider() {
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        String sqlSelect = "select txn_id as txn_id" + 
        		", isnull(txn_date,'') as txn_date" + 
        		", isnull(txn_amount,'0.00') as txn_amount" + 
        		", isnull(txn_Type,'CR') as txn_Type" + 
        		", isnull(txn_description,'') as txn_description" + 
        		", isnull(biller_ref_no1,'') as bill_ref_no1" + 
        		", isnull(biller_ref_no2 ,'') as bill_ref_no2" + 
        		", isnull(biller_ref_no3 ,'') as bill_ref_no3" +
        		", isnull(txn_time,'') as txn_time ";        		
        String fromClause =" from vw_batch_biller_payment_txn paytxn " 
        		+" join vw_batch_tbl_biller biller on biller.biller_code=paytxn.biller_code ";
        String whereClause= " where biller.biller_code=:billerCode and paytxn.txn_date =:txnDate " ;
        logger.info(String.format("biller payment txn select sql= %s %s %s", sqlSelect , fromClause , whereClause));
        queryProvider.setSelectClause(sqlSelect);
        queryProvider.setFromClause(fromClause);
        queryProvider.setWhereClause(whereClause);
        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }

    private Map<String, Order> sortByIdAsc() {
        Map<String, Order> sortConfiguration = new HashMap<>();
        sortConfiguration.put("txn_id", Order.ASCENDING);
        return sortConfiguration;
    }
    

	//processor
	@Bean(STEP_NAME+".ItemProcessor")
	@StepScope
	public ItemProcessor<BillerPaymentOutboundTxn, BillerPaymentOutboundDetail> billerProcessor(
			  @Value("#{stepExecution}") StepExecution stepExecution) {
        return billerDetail -> {
			BillerPaymentOutboundDetail billerDetOut=null;
			try {
				String message = String.format("biller payment processing txn id= %s, ref1=%s, txn amount=%s", billerDetail.getTxnId(), billerDetail.getBillRefNo1() , billerDetail.getTxnAmount());
				logger.info(message);
				String txnAmount = "";
				String txnId="";
				DecimalFormat decimalFormat = new DecimalFormat("#.00");

				billerDetOut = new BillerPaymentOutboundDetail();
				txnId = StringUtils.leftPad( StringUtils.substring(billerDetail.getTxnId(),billerDetail.getTxnId().length()-6),6,'0');

				billerDetOut.setTxnId(txnId);
				billerDetOut.setTxnDate(billerDetail.getTxnDate());

				double bTxnAmount = Double.parseDouble(billerDetail.getTxnAmount() ) ;
				txnAmount =  StringUtils.leftPad(decimalFormat.format(bTxnAmount),15,'0');
				logger.info(String.format("Before format-txn amount=%s,After format- txn amount=%s",billerDetail.getTxnAmount(),txnAmount));
				billerDetOut.setTxnAmount(txnAmount);

				billerDetOut.setBillRefNo1(billerDetail.getBillRefNo1());
				billerDetOut.setBillRefNo2(billerDetail.getBillRefNo2());
				billerDetOut.setBillRefNo3(billerDetail.getBillRefNo3());
				billerDetOut.setTxnTime(billerDetail.getTxnTime());
				billerDetOut.setFiller(StringUtils.leftPad(" ",18,' '));

				trailerCount = trailerCount+1;
				trailerAmount = trailerAmount + bTxnAmount ;
				computeHash(billerDetail.getBillRefNo1(), bTxnAmount);
				message = String.format("biller payment processing compute hash, txn id= %s, hashA=%s, hashB=%s", billerDetail.getTxnId(), hashA, hashB );
				logger.info(message);
			}
			catch (Exception ex){
				billerDetOut=null;
				stepExecution.getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS,BatchSystemConstant.ExitCode.FAILED);
				String message = String.format("biller payment file processing exception %s", ex.getMessage());
				logger.info(message);
			}
			return billerDetOut;
		};
    }
	
	private void computeHash(String refNo, double dTxnAmount) {
		String lastRefNo = "";
		int sumAscii = 0;

		logger.info(String.format("computing hash ref= %s, txAmount=%s", refNo, dTxnAmount));
		if (refNo.length() > 5) {
			lastRefNo = refNo.substring(refNo.length() - 5);
		} else if (refNo.length() <= 5) {
			lastRefNo = StringUtils.leftPad(refNo, 5, ' ');
		}
		logger.info(String.format("computing lastRefNo=%s", lastRefNo));
		for (char c : lastRefNo.toCharArray()) {
			if(Character.isDigit(c) || Character.isAlphabetic(c)) {
				sumAscii = sumAscii + (int) c;
			}
		}
		
		hashA = hashA.add(BigDecimal.valueOf(sumAscii * dTxnAmount).setScale(6, RoundingMode.HALF_UP));
		hashB = hashB + sumAscii;
		
		logger.info(String.format("computing sumAscii=%s, hashA=%s, hashB=%s, hashTotal=%s", sumAscii, hashA, hashB, hashTotal));
	}
	
	
	@Bean
	@StepScope
	protected FlatFileFooterCallback getFooterCallback(@Value("#{stepExecution}")  StepExecution stepExecution) {
	    return new FlatFileFooterCallback() {
	        @Override
	        public void writeFooter(Writer writer) throws IOException {
	    		if(hashB!=0) {
	    			hashTotal = hashA.doubleValue() / hashB;
	    		}else {
	    			hashTotal = 0.00;
	    		}
	            logger.info(String.format("trailer before-hashTotal=%s, trailerAmount=%s, trailerCount=%s", hashTotal,trailerAmount, trailerCount));		
	            String hashStr=formatHashTotal();
	            String hashTotalStr= StringUtils.right(hashStr,15);
				String totalAmtStr = String.format("%.2f",trailerAmount) ;
	            String trailerAmtStr = StringUtils.right(totalAmtStr, 15);
	            
	            String filler=StringUtils.leftPad(" ",112,' ');       
	            logger.info(String.format("trailer after -hashTotal=%s, trailerAmount=%s, trailerCount=%s", hashTotal,trailerAmount, trailerCount));		
	            stepExecution.getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS,BatchSystemConstant.ExitCode.SUCCESS);
	            writer.append("TY")
		            .append( StringUtils.leftPad(String.valueOf(trailerCount), 8, '0')  )
		            .append( StringUtils.leftPad(trailerAmtStr,15,'0'))
		            .append( StringUtils.leftPad(hashTotalStr ,15,'0' ) )
		            .append( StringUtils.leftPad(filler,112,' ') );
	            
	        }
	    };
	}
	
	@Bean
	@StepScope
	protected FlatFileHeaderCallback getHeaderCallback(@Value("#{stepExecution}") StepExecution stepExecution) {
		return new FlatFileHeaderCallback() {
			@Override
			public void writeHeader(Writer writer) throws IOException {
				String accountNo;
				String accountName;
				String filler;
				logger.info(String.format("header before processing date=%s,account No=%s,account name=%s", processingDateStr, billerConfig.getBillerAccNo() , billerConfig.getBillerAccName() ));
				accountNo = StringUtils.right(billerConfig.getBillerAccNo(), 14);
				accountName = StringUtils.left(billerConfig.getBillerAccName(), 20);
				filler = StringUtils.leftPad(" ",105, ' ');
				logger.info(String.format("header after processing date=%s,account No=%s,account name=%s", processingDateStr, accountNo , accountName ));
				writer.append("H0001")
						.append(processingDateStr)
						.append(StringUtils.leftPad(accountNo,14,'0') )
						.append(StringUtils.rightPad(accountName,20,' '))
						.append(filler);
			}
		};
	}
	
	protected String formatHashTotal() {
		return String.format("%.6f", BatchUtils.roundDecimal(hashTotal,6));
	}
	
	protected String getOutboundFileName() {
		return BatchUtils.generateSourceFileName(billerConfig.getFileNameFormat(), fileNameSystemDate) ;
	}
	//writer
	@Bean(STEP_NAME+".ItemWriter")
	@StepScope
    public FlatFileItemWriter<BillerPaymentOutboundDetail> billerWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
        FlatFileItemWriter<BillerPaymentOutboundDetail> flatFileItemWriter= new FlatFileItemWriter();
        try {
            flatFileItemWriter.setHeaderCallback(getHeaderCallback(stepExecution));
            flatFileItemWriter.setFooterCallback(getFooterCallback(stepExecution));
            String targetFileNewName = getOutboundFileName();
            logger.info(String.format("writing file folder=%s, file=%s", outputFolderFullPath, targetFileNewName));
            String outputFolder = new StringBuffer()
            							.append(outputFolderFullPath)
            							.append("/")
            							.append(JOB_NAME)
            							.toString();
            String targerFilePath = StringUtils.replace(outputFolder, "\\", File.separator);
            logger.info(String.format("writing file full path=%s", targerFilePath));
            File targetFileFullPath=Paths.get(targerFilePath,targetFileNewName).toFile();
            
            flatFileItemWriter.setResource(new FileSystemResource(targetFileFullPath.getPath()));
            stepExecution.getJobExecution().getExecutionContext().putString(BATCH_JOB_CONTEXT_PARAMETER_TEMP_OUT_FILE_FULL_PATH_KEY,
                    targetFileFullPath.getAbsolutePath());

            LineAggregator<BillerPaymentOutboundDetail> lineAggregator = createBillerLineAggregator();
            flatFileItemWriter.setLineAggregator(lineAggregator);        
        }catch(Exception ex){
        	String message = String.format("Exception in writing file %s", ex.getMessage());
        	logger.info(message);
            stepExecution.getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS,BatchSystemConstant.ExitCode.FAILED);
        }
        return flatFileItemWriter;
    }
	
	private LineAggregator<BillerPaymentOutboundDetail> createBillerLineAggregator() {
        FormatterLineAggregator<BillerPaymentOutboundDetail> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat(configProperties.getDetailColumns());
        FieldExtractor<BillerPaymentOutboundDetail> fieldExtractor = createBillerFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

	private FieldExtractor<BillerPaymentOutboundDetail> createBillerFieldExtractor() {
        BeanWrapperFieldExtractor<BillerPaymentOutboundDetail> extractor = new BeanWrapperFieldExtractor();
        String[] names= configProperties.getDetailNames().split(",", -1);
        extractor.setNames(names);
        return extractor;
    }
}
