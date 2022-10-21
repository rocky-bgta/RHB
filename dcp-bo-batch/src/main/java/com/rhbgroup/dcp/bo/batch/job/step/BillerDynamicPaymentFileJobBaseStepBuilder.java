
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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.sql.DataSource;

import com.rhbgroup.dcp.bo.batch.job.repository.BillDynamicPaymentConfigOutboundRepositoryImpl;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.BillerDynamicPaymentFileJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BillerDynamicPaymentOutboundConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BillerDynamicPaymentOutboundDetail;
import com.rhbgroup.dcp.bo.batch.job.model.BillerDynamicPaymentOutboundTxn;
import com.rhbgroup.dcp.bo.batch.job.model.BoBillerTemplateConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BoBillerTemplateTagConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BoBillerTemplateTagFieldConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BoBillerTemplateConfigRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BoBillerTemplateTagConfigRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.BoBillerTemplateTagFieldConfigRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.vo.FileTemplate;

@Component
@Lazy
public class BillerDynamicPaymentFileJobBaseStepBuilder extends BaseStepBuilder{

	private static final Logger logger = Logger.getLogger(BillerDynamicPaymentFileJobBaseStepBuilder.class);
	private static final String STEP_NAME="BillerDynamicPaymentFileJob";
	private  static final String TEMPLATE_SIX="Template_06";
	private  static final String TEMPLATE_SEVEN="Template_07";

	@Autowired
	private BillerDynamicPaymentFileJobConfigProperties configProperties;
	
	@Autowired
	private BoBillerTemplateConfigRepositoryImpl boBillerTemplateConfigRepositoryImpl;
	
	@Autowired
	private BoBillerTemplateTagConfigRepositoryImpl boBillerTemplateTagConfigRepositoryImpl;
	
	@Autowired
	private BoBillerTemplateTagFieldConfigRepositoryImpl boBillerTemplateTagFieldConfigRepositoryImpl;

	@Autowired
	BillDynamicPaymentConfigOutboundRepositoryImpl billDynamicPaymentConfigOutboundRepositoryImpl;

    @Value(BATCH_SYSTEM_FOLDER_OUTPUT_DIRECTORY)
    private String outputFolderFullPath;
    

    @Autowired
    @Qualifier(STEP_NAME+".ItemReader")
    private ItemReader<BillerDynamicPaymentOutboundTxn> itemReader;
    
    @Autowired
    @Qualifier(STEP_NAME+".ItemProcessor")
    private ItemProcessor<BillerDynamicPaymentOutboundTxn, BillerDynamicPaymentOutboundDetail> itemProcessor;
    
    @Autowired
    @Qualifier(STEP_NAME+".ItemWriter")
    private ItemWriter<BillerDynamicPaymentOutboundDetail> itemWriter;
	
	
	@Autowired
	@Qualifier("BillDynamicPaymentConfigOutboundQueue")
	private Queue<BillerDynamicPaymentOutboundConfig> queue ;
	
	protected double hashTotal;
    protected String billerCode="";
	protected BillerDynamicPaymentOutboundConfig billerConfig;
	protected static final String JOB_NAME="BillerDynamicPaymentFileJob";

	private String processingDateStr = "";
	private String processingTimeStr="";
	private int trailerCount;
	private double trailerAmount;
	private BigDecimal hashA;
	private int hashB;
	private int hashC;
	protected Date batchProcessingDate;
	protected Date fileNameSystemDate;
	protected String txnTime;
	protected FileTemplate fileTemplate = new FileTemplate();;
	
	@Override
    @Bean
    public Step buildStep() {		
		return getDefaultStepBuilder(STEP_NAME).<BillerDynamicPaymentOutboundTxn,BillerDynamicPaymentOutboundDetail>chunk(100)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }

	//reader
	@Bean(STEP_NAME+".ItemReader")
    @StepScope
	public JdbcPagingItemReader<BillerDynamicPaymentOutboundTxn> billerReader (
			@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) {
		JdbcPagingItemReader<BillerDynamicPaymentOutboundTxn> databaseReader = new JdbcPagingItemReader<>();
		try {

			databaseReader.setDataSource(dataSource);
			databaseReader.setPageSize(configProperties.getChunkSize());
			
			trailerCount=0;
			trailerAmount=0.00;
			hashTotal=0.00;
			hashA = new BigDecimal(0);
			hashB = 0;
			hashC = 0;

			billerConfig = queue.element();

			String message = String.format("Biller payment file reader biller code=%s,account no=%s,name=%s", billerConfig.getBillerCode(), billerConfig.getBillerAccNo(), billerConfig.getBillerAccName() );
			logger.info(message);
			String batchSystemDate=stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT) ;
			processingDateStr = DateUtils.formatDateString(batchProcessingDate ,DEFAULT_JOB_PARAMETER_DATE_FORMAT);
			processingTimeStr = DateUtils.formatDateString(batchProcessingDate ,"hhmm");

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
			databaseReader.setRowMapper(new RowMapper<BillerDynamicPaymentOutboundTxn>() {
				@Override
				public BillerDynamicPaymentOutboundTxn mapRow(ResultSet rs, int rowNum) throws SQLException {
					ResultSetMetaData resultSetMetaData =rs.getMetaData();
					int columnCount = resultSetMetaData.getColumnCount();
					BillerDynamicPaymentOutboundTxn billerDynamicPaymentOutboundTxn=new BillerDynamicPaymentOutboundTxn();
					for (int i = 1; i <= columnCount; i++ ) {
						String name = resultSetMetaData.getColumnName(i);
						if(rs.getString(name)!=null) {
							billerDynamicPaymentOutboundTxn.getFieldMap().put(name, rs.getString(name));
						}else{
							billerDynamicPaymentOutboundTxn.getFieldMap().put(name, "");

						}
					}
					return billerDynamicPaymentOutboundTxn;
				}
			});

			int templateId = billerConfig.getTemplateId();
			message=String.format("Biller payment outbound file query data for template Id=%s", templateId );
			logger.info(message);
			
	 		BoBillerTemplateConfig boBillerTemplateConfig = boBillerTemplateConfigRepositoryImpl.getBillerTemplateDetail(templateId);
	 		message=String.format("Biller boBillerTemplateConfig =%s", boBillerTemplateConfig );
			logger.info(message);
			
			List<BoBillerTemplateTagConfig> boBillerTemplateTagConfig = boBillerTemplateTagConfigRepositoryImpl.getBillerTemplateTagDetail(boBillerTemplateConfig.getTemplateId());
	 		message=String.format("Biller boBillerTemplateTagConfig =%s", boBillerTemplateTagConfig );
			logger.info(message);
			
			List<BoBillerTemplateTagFieldConfig> boBillerTemplateTagFieldConfigList = new ArrayList<>();
			
			for(BoBillerTemplateTagConfig templateTagConfig:boBillerTemplateTagConfig) {
				List<BoBillerTemplateTagFieldConfig> boBillerTemplateTagFieldConfig = boBillerTemplateTagFieldConfigRepositoryImpl.getBillerTemplateTagFieldDetail(templateTagConfig.getTemplateTagId());
				boBillerTemplateTagFieldConfigList.addAll(boBillerTemplateTagFieldConfig);
			}
			
			message=String.format("Biller boBillerTemplateTagFieldConfig =%s", boBillerTemplateTagFieldConfigList );
			logger.info(message);

			fileTemplate.setTemplateName(boBillerTemplateConfig.getTemplateName());
			fileTemplate.setViewName(boBillerTemplateConfig.getViewName());

			fileTemplate.setTemplateTags(boBillerTemplateTagConfig);
			fileTemplate.setTemplateTagFields(boBillerTemplateTagFieldConfigList);

			
            logger.info(String.format("fileTemplate=%s",fileTemplate.getTemplateName()));

			if(fileTemplate.getTemplateName().equalsIgnoreCase("Template_08") || fileTemplate.getTemplateName().equalsIgnoreCase("Template_09")) {
				queryProvider = createQueryProviderT08();
			}else {
				queryProvider = createQueryProvider();
			}
			databaseReader.setQueryProvider(queryProvider);
			databaseReader.setParameterValues( parameters );
			databaseReader.setRowMapper(new RowMapper<BillerDynamicPaymentOutboundTxn>() {
				@Override
				public BillerDynamicPaymentOutboundTxn mapRow(ResultSet rs, int rowNum) throws SQLException {
					ResultSetMetaData resultSetMetaData =rs.getMetaData();
					int columnCount = resultSetMetaData.getColumnCount();
					BillerDynamicPaymentOutboundTxn billerDynamicPaymentOutboundTxn=new BillerDynamicPaymentOutboundTxn();
					for (int i = 1; i <= columnCount; i++ ) {
						String name = resultSetMetaData.getColumnName(i);
						if(rs.getString(name)!=null) {
							billerDynamicPaymentOutboundTxn.getFieldMap().put(name, rs.getString(name));
						}else{
							billerDynamicPaymentOutboundTxn.getFieldMap().put(name, "");

						}
					}
					return billerDynamicPaymentOutboundTxn;
				}
			});


		}catch(Exception ex ) {
			stepExecution.getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS,BatchSystemConstant.ExitCode.FAILED);
			String message = String.format("Biller payment item reader exception %s", ex.getMessage()) ;
			logger.info(message);
		}
		return databaseReader;
	}

    private PagingQueryProvider createQueryProvider() {
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        String sqlSelect = "select txn_id as txn_id " +
				", biller_account_no as biller_account_no" +
				", biller_account_name as biller_account_name" +
        		", ref_id as ref_id" + 
        		", isnull(txn_date,'') as txn_date" +
				", isnull(txn_time,'') as txn_time" +
				", isnull(txn_year,'') as txn_year" +
				", isnull(txn_amount,'0.00') as txn_amount" +
				", CASE WHEN biller_ref_no1='' THEN policy_no ELSE biller_ref_no1 END as biller_ref_no1"+
        		", isnull(biller_ref_no2 ,'') as biller_ref_no2" +
        		", isnull(biller_ref_no3 ,'') as biller_ref_no3" +
        		", isnull(biller_ref_no4 ,'') as biller_ref_no4" +
        		", isnull(txn_time,'') as txn_time "+ 
        		", isnull(id_no,'') as id_no "+  
        		", isnull(user_address1,'') as user_address1 "+        		
        		", isnull(user_address2,'') as user_address2 "+        		
        		", isnull(user_address3,'') as user_address3 "+        		
        		", isnull(user_address4,'') as user_address4 "+        		
        		", isnull(user_state,'') as user_state "+        		
        		", isnull(user_city,'') as user_city "+
        		", isnull(user_postcode,'') as user_postcode "+
        		", isnull(user_country,'') as user_country "+
        		", isnull(pay_method,'') as pay_method "+        		
        		", ROW_NUMBER() OVER (PARTITION BY paytxn.biller_code ORDER BY txn_date)  as sequence_no";
        String fromClause =" from vw_batch_biller_payment_txn_template paytxn " 
        		+" join vw_batch_tbl_biller biller on biller.biller_code=paytxn.biller_code ";
        String whereClause= " where biller.biller_code=:billerCode and paytxn.txn_date =:txnDate " ;
        logger.info(String.format("biller payment txn select sql= %s %s %s", sqlSelect , fromClause , whereClause));
        queryProvider.setSelectClause(sqlSelect);
        queryProvider.setFromClause(fromClause);
        queryProvider.setWhereClause(whereClause);
        queryProvider.setSortKeys(sortByIdAsc());
        return queryProvider;
    }

    private PagingQueryProvider createQueryProviderT08() {
        SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
        String sqlSelect = "select txn_id as txn_id " +
				", biller_account_no as biller_account_no" +
				", biller_account_name as biller_account_name" +
        		", ref_id as ref_id" +
        		", isnull(txn_date,'') as txn_date" +
				", isnull(txn_time,'') as txn_time" +
				", isnull(txn_year,'') as txn_year" +
				", isnull(txn_amount,'0.00') as txn_amount" +
				", CASE WHEN biller_ref_no1='' THEN policy_no ELSE biller_ref_no1 END as biller_ref_no1"+
        		", isnull(biller_ref_no2 ,'') as biller_ref_no2" +
        		", isnull(biller_ref_no3 ,'') as biller_ref_no3" +
        		", isnull(biller_ref_no4 ,'') as biller_ref_no4" +
        		", isnull(txn_time,'') as txn_time "+
        		", isnull(id_no,'') as id_no "+
        		", isnull(user_address1,'') as user_address1 "+
        		", isnull(user_address2,'') as user_address2 "+
        		", isnull(user_address3,'') as user_address3 "+
        		", isnull(user_address4,'') as user_address4 "+
        		", isnull(user_state,'') as user_state "+
        		", isnull(user_city,'') as user_city "+
        		", isnull(user_postcode,'') as user_postcode "+
        		", isnull(user_country,'') as user_country "+
        		", isnull(pay_method,'') as pay_method "+
        		", ROW_NUMBER() OVER (PARTITION BY paytxn.biller_code ORDER BY txn_date)  as sequence_no";
        String fromClause =" from vw_batch_biller_payment_txn_template_t08 paytxn "
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
	public ItemProcessor<BillerDynamicPaymentOutboundTxn, BillerDynamicPaymentOutboundDetail> billerProcessor(
			  @Value("#{stepExecution}") StepExecution stepExecution) {
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
        return billerDetail -> {
			BillerDynamicPaymentOutboundDetail billerDetOut=null;
			try {
				String message = String.format("biller payment processing txn id= %s, ref1=%s, txn amount=%s", billerDetail.getTxnId(), billerDetail.getBillRefNo1() , billerDetail.getTxnAmount());
				logger.info(message);
				billerDetOut = new BillerDynamicPaymentOutboundDetail();

				List<BoBillerTemplateTagConfig> templateTags = fileTemplate.getTemplateTags();

				List<BoBillerTemplateTagFieldConfig> templateTagFields = fileTemplate.getTemplateTagFields();

				billerDetOut = templateFieldValuesSetCustom(billerDetOut,templateTags,templateTagFields,billerDetail);
				logger.info(String.format("after billerDetOut = %s", billerDetOut ));

				message = String.format("biller payment processing compute hash, txn id= %s, hashA=%s, hashB=%s", billerDetail.getTxnId(), hashA, hashB );
				logger.info(message);
			}
			catch (Exception ex){
				billerDetOut=null;
				stepExecution.getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS,BatchSystemConstant.ExitCode.FAILED);
				String message = String.format("biller payment file processing exception %s", ex);
				logger.info(message);
			}
			return billerDetOut;
		};
    }
	private BillerDynamicPaymentOutboundDetail templateFieldValuesSetCustom(BillerDynamicPaymentOutboundDetail billerDetOut,List<BoBillerTemplateTagConfig> templateTags,List<BoBillerTemplateTagFieldConfig> templateTagFields,BillerDynamicPaymentOutboundTxn billerDetail) {

		double bTxnAmount = Double.parseDouble(billerDetail.getFieldMap().getOrDefault("txn_amount","")) ;

		for(BoBillerTemplateTagConfig boBillerTemplateTagConfig:templateTags) {
			for(BoBillerTemplateTagFieldConfig boBillerTemplateTagFieldConfig:templateTagFields) {
				if(boBillerTemplateTagConfig.getTemplateTagId() == boBillerTemplateTagFieldConfig.getTemplateTagId()
						&& boBillerTemplateTagConfig.getTagName().contains("BODY")) {

					String deafultValue=boBillerTemplateTagFieldConfig.getDefaultValue()!=null?boBillerTemplateTagFieldConfig.getDefaultValue():"";
					String value=billerDetail.getFieldMap().getOrDefault(boBillerTemplateTagFieldConfig.getViewFieldName(), "");

					if(value.length()>boBillerTemplateTagFieldConfig.getLength()){
						value=value.substring(0,boBillerTemplateTagFieldConfig.getLength());
					}

					billerDetOut=billerProcessEngine( billerDetOut,boBillerTemplateTagFieldConfig,
							     bTxnAmount,deafultValue, value);

				}
			}
		}

		txnTime = billerDetail.getFieldMap().getOrDefault("txn_time","");
		trailerCount = trailerCount+1;
		trailerAmount = trailerAmount + bTxnAmount ;

		computeHash(billerDetail.getFieldMap().getOrDefault("biller_ref_no1",""), bTxnAmount);

		logger.info(String.format("before billerDetOut = %s", billerDetOut ));

		return billerDetOut;
	}

	private BillerDynamicPaymentOutboundDetail billerProcessEngine(BillerDynamicPaymentOutboundDetail billerDetOut,
																   BoBillerTemplateTagFieldConfig boBillerTemplateTagFieldConfig,
																   double bTxnAmount,String deafultValue,
																   String value) {
		String detail="";
		switch (boBillerTemplateTagFieldConfig.getValueType()) {
			case "DEFAULT":
				detail=billerProcessEngineDefaultValue( boBillerTemplateTagFieldConfig, deafultValue);
				billerDetOut.setDetail(billerDetOut.getDetail()+detail);
				break;
			case "VIEW":

				  value = dateSetTemplate06(fileTemplate,boBillerTemplateTagFieldConfig,value);

				if(boBillerTemplateTagFieldConfig.getPaddingType().equalsIgnoreCase("LEFT")){
					logger.info(String.format("view = %s", boBillerTemplateTagFieldConfig.getViewFieldName() ));

					if(boBillerTemplateTagFieldConfig.getViewFieldName().equalsIgnoreCase("txn_amount")){
						DecimalFormat decimalFormat = new DecimalFormat("#.00");

						detail = StringUtils.leftPad(
								fileTemplate.getTemplateName().equalsIgnoreCase(TEMPLATE_SIX) || fileTemplate.getTemplateName().equalsIgnoreCase(TEMPLATE_SEVEN)? decimalFormat.format(bTxnAmount).replace(".",""):
										decimalFormat.format(bTxnAmount),
								boBillerTemplateTagFieldConfig.getLength(), boBillerTemplateTagFieldConfig.getPaddingFillValue());

					}else {
						logger.info("out view " );

						detail = StringUtils.leftPad(value,
								boBillerTemplateTagFieldConfig.getLength(), boBillerTemplateTagFieldConfig.getPaddingFillValue());
					}
				}else{
					detail=	StringUtils.rightPad(value,boBillerTemplateTagFieldConfig.getLength(),boBillerTemplateTagFieldConfig.getPaddingFillValue());
				}
				billerDetOut.setDetail(billerDetOut.getDetail()+detail);

				break;
			default:
				break;
		}
        return billerDetOut;
	}

	private String dateSetTemplate06(FileTemplate fileTemplate,BoBillerTemplateTagFieldConfig boBillerTemplateTagFieldConfig,String value) {
		if(fileTemplate.getTemplateName().equalsIgnoreCase(TEMPLATE_SIX)
				&& boBillerTemplateTagFieldConfig.getFieldType().equalsIgnoreCase("Date") && !StringUtils.isBlank(value)){
			value=value.substring(6)+value.substring(4,6)+value.substring(0,4);
			logger.info(String.format("Template_06 date  = %s", value ));
		}
		return value;
	}

	public String billerProcessEngineDefaultValue(BoBillerTemplateTagFieldConfig boBillerTemplateTagFieldConfig,String deafultValue){
		String detail="";
		if(boBillerTemplateTagFieldConfig.getPaddingType().equalsIgnoreCase("LEFT")){
			detail=	StringUtils.leftPad(deafultValue,boBillerTemplateTagFieldConfig.getLength(),boBillerTemplateTagFieldConfig.getPaddingFillValue());
		}else{
			detail=	StringUtils.rightPad(deafultValue,boBillerTemplateTagFieldConfig.getLength(),boBillerTemplateTagFieldConfig.getPaddingFillValue());
		}
		return detail;
	}
	private void computeHash(String refNo, double dTxnAmount) {
		String lastRefNo = "";
		int sumAscii = 0;

		logger.info(String.format("computing hash ref= %s, txAmount=%s", refNo, dTxnAmount));

		if(fileTemplate.getTemplateName().equals(TEMPLATE_SIX)) {
			lastRefNo = refNo.substring(refNo.length() - 7);
			logger.info(String.format("computing lastRefNo for template_06 =%s", lastRefNo));
			hashC = hashC+Integer.parseInt(lastRefNo);
		}else {
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

	}
	
	
	@Bean
	@StepScope
	protected FlatFileFooterCallback getFooterCallback(@Value("#{stepExecution}")  StepExecution stepExecution) {
	    return new FlatFileFooterCallback() {
	        @Override
	        public void writeFooter(Writer writer) throws IOException {
	        	int totalHash = 0;
	        	if(fileTemplate.getTemplateName().equals(TEMPLATE_SIX)) {
	        		int totalAmt = (int) (trailerAmount*100);
	        		totalHash = hashC+ totalAmt+ trailerCount;
	        	}else {
		    		if(hashB!=0) {
		    			hashTotal = hashA.doubleValue() / hashB;
		    		}else {
		    			hashTotal = 0.00;
		    		}
	        	}

	            logger.info(String.format("trailer before-hashTotal=%s, trailerAmount=%s, trailerCount=%s", hashTotal,trailerAmount, trailerCount));		
	            String hashStr=formatHashTotal();
	            String hashTotalStr= StringUtils.right(hashStr,15);
	            String hashTotalT6= StringUtils.right(String.valueOf(totalHash),15);
				String totalAmtStr = String.format("%.2f",trailerAmount) ;
	            String trailerAmtStr = StringUtils.right(totalAmtStr, 15);
	            
	            String filler=StringUtils.leftPad(" ",112,' ');       
	            logger.info(String.format("trailer after -hashTotal=%s, trailerAmount=%s, trailerCount=%s hashTotalT6=%s", hashTotal,trailerAmount, trailerCount,hashTotalT6));
	            stepExecution.getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS,BatchSystemConstant.ExitCode.SUCCESS);
				if(fileTemplate.getTemplateName().equals(TEMPLATE_SEVEN)) {
					 writer.append("3TRL")
			            .append( StringUtils.leftPad(String.valueOf(trailerCount), 6, '0')  )
			            .append( StringUtils.leftPad(trailerAmtStr.replace(".",""),12,'0'))
			            .append( StringUtils.leftPad(hashTotalStr ,20,'0' ) )
			            .append( StringUtils.leftPad(" ",88,' ') );
				}else if(fileTemplate.getTemplateName().equals(TEMPLATE_SIX)){
					 hashTotalT6=hashTotalT6.replace(".","");
					hashTotalT6=	hashTotalT6.length()>=10?hashTotalT6.substring(0,10):hashTotalT6;
					writer.append("2")
							.append( StringUtils.leftPad(String.valueOf(trailerCount), 6, '0')  )
							.append( StringUtils.leftPad(trailerAmtStr.replace(".",""),10,'0'))
							.append( StringUtils.leftPad(hashTotalT6 ,10,'0' ) )
							.append( StringUtils.leftPad(" ",73,' ') );
				}else {
		            writer.append("TY")
		            .append( StringUtils.leftPad(String.valueOf(trailerCount), 8, '0')  )
		            .append( StringUtils.leftPad(trailerAmtStr,15,'0'))
		            .append( StringUtils.leftPad(hashTotalStr ,15,'0' ) )
		            .append( StringUtils.leftPad(filler,112,' ') );	
				}
	            
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
				String totalAmtStr = String.format("%.2f",trailerAmount) ;
				logger.info(String.format("header before processing date=%s,account No=%s,account name=%s", processingDateStr, billerConfig.getBillerAccNo() , billerConfig.getBillerAccName() ));
				accountNo = StringUtils.right(billerConfig.getBillerAccNo(), 14);
				accountName = StringUtils.left(billerConfig.getBillerAccName(), 22);
				String billerCode1 = StringUtils.left(billerConfig.getBillerCode(), 6);
				filler = StringUtils.leftPad(" ",105, ' ');
				logger.info(String.format("header after processing date=%s,account No=%s,account name=%s", processingDateStr, accountNo , accountName ));

				String batchSystemDate=stepExecution.getJobExecution().getExecutionContext().getString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
				try {
					batchProcessingDate = BatchUtils.getProcessingDate(batchSystemDate, DEFAULT_DATE_FORMAT) ;
				} catch (ParseException e) {
					logger.info(String.format("error =%s", e));

				}
				processingDateStr = DateUtils.formatDateString(batchProcessingDate ,DEFAULT_JOB_PARAMETER_DATE_FORMAT);

			int	trailerCount1=billDynamicPaymentConfigOutboundRepositoryImpl.getBillerTransactionCount(billerCode,processingDateStr);
			BigDecimal trailerSum =billDynamicPaymentConfigOutboundRepositoryImpl.getBillerTransactionSum(billerCode,processingDateStr);
			totalAmtStr = trailerSum.toString();

				if(fileTemplate.getTemplateName().equals("Template_05")) {
					processingDateStr = DateUtils.formatDateString(batchProcessingDate ,"yyMMdd");

	            	filler = StringUtils.leftPad(" ",396, ' ');
					writer.append("H")
					.append(StringUtils.rightPad(accountName,50,' '))
					.append(StringUtils.rightPad(processingDateStr,6,' '))
					.append(StringUtils.leftPad(String.valueOf(trailerCount1),5,'0'))
					.append(StringUtils.rightPad(" ",9,' '))
					.append(StringUtils.leftPad(totalAmtStr,15,'0'))
					.append(filler);
	            }
				else if(fileTemplate.getTemplateName().equals(TEMPLATE_SIX)) {
					filler = StringUtils.leftPad(" ",87, ' ');
					writer.append("0")
					.append(processingDateStr.substring(6)+processingDateStr.substring(4,6)+processingDateStr.substring(0,4))
					.append("RHBB"+StringUtils.leftPad(" ",6, ' '))
					.append(filler);
				}
				else if(fileTemplate.getTemplateName().equals(TEMPLATE_SEVEN)) {
					filler = StringUtils.leftPad(" ",73, ' ');
					logger.info(String.format("Template Header=%s", fileTemplate.getTemplateName()));
					
					writer.append("1HDR")
					.append(processingDateStr)
							.append(StringUtils.leftPad(String.valueOf(trailerCount1),6,'0'))
					.append(StringUtils.rightPad("RHBBANK",8," "))
					.append(StringUtils.leftPad("GELEBANK",8,'0'))
					.append(StringUtils.leftPad(processingTimeStr,4,'0'))
					.append(filler);
				}else {
					writer.append("H0001")
					.append(processingDateStr)
					.append(StringUtils.leftPad(accountNo,14,'0') )
					.append(StringUtils.rightPad(accountName,20,' '))
					.append(filler);	
				}
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
    public FlatFileItemWriter<BillerDynamicPaymentOutboundDetail> billerWriter(@Value("#{stepExecution}") StepExecution stepExecution) {
        FlatFileItemWriter<BillerDynamicPaymentOutboundDetail> flatFileItemWriter= new FlatFileItemWriter<>();
        try {
            flatFileItemWriter.setHeaderCallback(getHeaderCallback(stepExecution));
            if(!fileTemplate.getTemplateName().equals("Template_05")) {
                flatFileItemWriter.setFooterCallback(getFooterCallback(stepExecution));
            }            
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

            LineAggregator<BillerDynamicPaymentOutboundDetail> lineAggregator = createBillerLineAggregator();
            flatFileItemWriter.setLineAggregator(lineAggregator);        
        }catch(Exception ex){
        	String message = String.format("Exception in writing file %s", ex.getMessage());
        	logger.info(message);
            stepExecution.getJobExecution().getExecutionContext().put(STEP_EXECUTION_STATUS,BatchSystemConstant.ExitCode.FAILED);
        }
        return flatFileItemWriter;
    }
	
	private LineAggregator<BillerDynamicPaymentOutboundDetail> createBillerLineAggregator() {
        FormatterLineAggregator<BillerDynamicPaymentOutboundDetail> lineAggregator = new FormatterLineAggregator<>();
		if(fileTemplate.getTemplateName().equals(TEMPLATE_SIX)){
			lineAggregator.setFormat("%-100s");
		}else {
			lineAggregator.setFormat("%-112s");
		}
        FieldExtractor<BillerDynamicPaymentOutboundDetail> fieldExtractor = createBillerFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

	private FieldExtractor<BillerDynamicPaymentOutboundDetail> createBillerFieldExtractor() {
        BeanWrapperFieldExtractor<BillerDynamicPaymentOutboundDetail> extractor = new BeanWrapperFieldExtractor<>();
        String[] names= {"detail"};
        extractor.setNames(names);
        return extractor;
    }
}
