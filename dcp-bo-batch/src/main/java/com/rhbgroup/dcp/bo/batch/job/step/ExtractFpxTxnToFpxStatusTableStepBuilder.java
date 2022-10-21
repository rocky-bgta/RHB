package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractandUpdateFpxTransactionStatusJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateFPXStatus;
import com.rhbgroup.dcp.bo.batch.job.model.FPXTxn;
import com.rhbgroup.dcp.bo.batch.job.model.InvestTxn;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchUpdateFpxStatusRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.InvestTxnRepositoryImpl;

@Component
@Lazy
public class ExtractFpxTxnToFpxStatusTableStepBuilder extends BaseStepBuilder {

	static final Logger logger = Logger.getLogger(ExtractFpxTxnToFpxStatusTableStepBuilder.class);
	private static final String STEP_NAME = "ExtractFpxTxnToFpxStatusTableStep";
	@Autowired
	private ExtractandUpdateFpxTransactionStatusJobConfigProperties jobConfigProperties;
	
	@Autowired
	@Qualifier(STEP_NAME + ".ItemReader")
	private ItemReader<FPXTxn> itemReader;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemProcessor")
	private ItemProcessor<FPXTxn, BatchUpdateFPXStatus> itemProcessor;
	@Autowired
	@Qualifier(STEP_NAME + ".ItemWriter")
	private ItemWriter<BatchUpdateFPXStatus> itemWriter;
	@Autowired
	private BatchUpdateFpxStatusRepositoryImpl batchUpdateFpxStatusRepositoryImpl;
	@Autowired
	private InvestTxnRepositoryImpl investTxnRepositoryImpl;
	

	@Override
	@Bean(STEP_NAME)
	public Step buildStep() {
		return getDefaultStepBuilder(STEP_NAME)
				.<FPXTxn, BatchUpdateFPXStatus>chunk(jobConfigProperties.getChunkSize())
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.build();
	}

	
	@Bean(STEP_NAME + ".ItemReader")
	@StepScope
	private JdbcPagingItemReader<FPXTxn> extractFpxTxnJobReader(
			@Value("#{stepExecution}") StepExecution stepExecution, DataSource dataSource) {
		
		logger.info(String.format("Creating ItemReader [%s]", STEP_NAME + ".ItemReader"));
		
		JdbcPagingItemReader<FPXTxn> databaseReader = new JdbcPagingItemReader<>();
		databaseReader.setDataSource(dataSource);
		databaseReader.setPageSize(jobConfigProperties.getJdbcPagingPageSize());

		PagingQueryProvider queryProvider = createQueryProvider();
		databaseReader.setQueryProvider(queryProvider);
		databaseReader.setRowMapper(new BeanPropertyRowMapper<>(FPXTxn.class));
		
        logger.info(String.format("ItemReader [%s] created succesfully", STEP_NAME + ".ItemReader"));

		return databaseReader;
	}

	private PagingQueryProvider createQueryProvider() {
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(date);
		logger.info(String.format("  strDate=%s", strDate));
		SqlServerPagingQueryProvider queryProvider = new SqlServerPagingQueryProvider();
		queryProvider.setSelectClause(
				"SELECT *");
		queryProvider.setFromClause("FROM dcp.dbo.TBL_FPX_TXN"); 
		queryProvider.setWhereClause("WHERE (DATEDIFF(Minute,SELLER_TXN_TIME,GETDATE()))>20 AND \r\n"
				+ "SELLER_TXN_TIME >='"+strDate+"' AND SELLER_TXN_TIME < DATEADD(DAY,1,'"+strDate+"') AND (MAIN_FUNCTION ='TERM_DEPOSIT' AND (TXN_STATUS ='PENDING' OR TXN_STATUS ='SUCCESS')) OR (MAIN_FUNCTION ='CASA' AND TXN_STATUS ='PENDING')");
		queryProvider.setSortKeys(sortByIdAsc());
		
        logger.debug("Generated query : " + queryProvider.toString());
        
		return queryProvider;
	}

	private Map<String, Order> sortByIdAsc() {
		Map<String, Order> sortConfiguration = new HashMap<>();
		sortConfiguration.put("id", Order.ASCENDING);
		return sortConfiguration;
	}

	@Bean(STEP_NAME + ".ItemProcessor")
	@StepScope
	private ItemProcessor<FPXTxn, BatchUpdateFPXStatus> extractFpxTxnJobProcessor() {
		
		logger.info(String.format("Creating ItemProcessor [%s]", STEP_NAME + ".ItemProcessor"));
		
		return fPXTxn -> {
			
			BatchUpdateFPXStatus batchUpdateFPXStatus = new BatchUpdateFPXStatus();
			
			if(fPXTxn.getTxnStatus().equals("SUCCESS")) {
			InvestTxn investTxn =	investTxnRepositoryImpl.getInvestTxnDetail(fPXTxn.getTxnTokenId());
				if(investTxn.getId() == null) {
				batchUpdateFPXStatus =  setValues(fPXTxn);
				}				
			}else if(fPXTxn.getTxnStatus().equals("PENDING")) {
				batchUpdateFPXStatus = setValues(fPXTxn);
			}

			logger.info(String.format("[Processing] BatchUpdateFPXStatus: [%s]", batchUpdateFPXStatus));
			return batchUpdateFPXStatus;
		};
	}
	
	private BatchUpdateFPXStatus setValues(FPXTxn fPXTxn) {
		BatchUpdateFPXStatus batchUpdateFPXStatus = new BatchUpdateFPXStatus();
		Date currentDate = new Date();
		batchUpdateFPXStatus.setTxnTokenId(fPXTxn.getTxnTokenId());
		batchUpdateFPXStatus.setMainFunction(fPXTxn.getMainFunction());
		batchUpdateFPXStatus.setSubFunction(fPXTxn.getSubFunction());
		batchUpdateFPXStatus.setBankId(fPXTxn.getBankId());
		batchUpdateFPXStatus.setBuyerName(fPXTxn.getBuyerName());
		batchUpdateFPXStatus.setBuyerEmail(fPXTxn.getBuyerEmail());
		batchUpdateFPXStatus.setSellerBankCode(fPXTxn.getSellerBankCode());
		batchUpdateFPXStatus.setSellerExId(fPXTxn.getSellerExId());
		batchUpdateFPXStatus.setSellerExOrderNo(fPXTxn.getSellerExOrderNo());
		batchUpdateFPXStatus.setSellerId(fPXTxn.getSellerId());
		batchUpdateFPXStatus.setSellerOrderNo(fPXTxn.getSellerOrderNo());
		batchUpdateFPXStatus.setSellerTxnTime(fPXTxn.getSellerTxnTime());
		batchUpdateFPXStatus.setTxnAmount(fPXTxn.getTxnAmount());
		batchUpdateFPXStatus.setTxnStatus(fPXTxn.getTxnStatus());
		batchUpdateFPXStatus.setDebitAuthCode(fPXTxn.getDebitAuthCode());
		batchUpdateFPXStatus.setDebitAuthNo(fPXTxn.getDebitAuthNo());
		batchUpdateFPXStatus.setCreditAuthCode(fPXTxn.getCreditAuthCode());
		batchUpdateFPXStatus.setCreditAuthNo(fPXTxn.getCreditAuthNo());
		batchUpdateFPXStatus.setTxnDescription(fPXTxn.getTxnStatusDescription());
		batchUpdateFPXStatus.setProductDescription(fPXTxn.getProductDescription());
		batchUpdateFPXStatus.setTxnId(fPXTxn.getTxnId());
		batchUpdateFPXStatus.setTxnTime(fPXTxn.getTxnTime());
		batchUpdateFPXStatus.setCreatedTime(currentDate);
		batchUpdateFPXStatus.setUpdatedTime(currentDate);
        
		return batchUpdateFPXStatus;
	}

	@Bean(STEP_NAME + ".ItemWriter")
	@StepScope
	private ItemWriter<BatchUpdateFPXStatus> extractFpxTxnJobWriter(
			@Value("#{stepExecution}") StepExecution stepExecution) {	
		
		logger.info(String.format("Creating ItemWriter [%s]", STEP_NAME + ".ItemWriter"));

		return new ItemWriter<BatchUpdateFPXStatus>() {
			@Override
			public void write(List<? extends BatchUpdateFPXStatus> batchUpdateFPXStatusList) throws Exception {
				logger.info(String.format("Writing total no. of FPX Status list : [%d]", batchUpdateFPXStatusList.size()));
				
	            int failedCount=0;
				String errorMessage = "";

	            long jobExecutionId= stepExecution.getJobExecution().getId().longValue();
            	String message = String.format("%s jobExecutionId=%s", this.getClass().getName(), jobExecutionId);
            	logger.info(message);

				for(BatchUpdateFPXStatus batchUpdateFPXStatus : batchUpdateFPXStatusList) {
					try {
						if(batchUpdateFPXStatus.getMainFunction() != null) {
							batchUpdateFPXStatus.setJobExecutionId(jobExecutionId);
							batchUpdateFpxStatusRepositoryImpl.addBatchUpdateFpxStatusStaging(batchUpdateFPXStatus);
						}

					} catch (Exception e) {
						++failedCount;
						// If exception happened, don't stop the batch, just proceed to next record until all complete processed
						errorMessage = String.format("Error happened while writing to DB for Staged FpxStatus Table [%s], id:%d", batchUpdateFPXStatus, batchUpdateFPXStatus.getId());
						logger.error("Unable to " + e.getLocalizedMessage(), e);
						logger.error(errorMessage, e);

					}
				}
				if(failedCount > 0) {
					stepExecution.getJobExecution().addFailureException(new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, errorMessage ));
				}

			}
		};	

	}
}
