package com.rhbgroup.dcp.bo.batch.job.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlServerPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractandUpdateMcaInterestRateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateExchangeRate;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateMcaInterestRate;
import com.rhbgroup.dcp.bo.batch.job.model.CurrencyRateConfig;
import com.rhbgroup.dcp.bo.batch.job.model.MCATermInterestRateConfig;

@Component
@Lazy
public class BatchUpdateMcaInterestRateRepositoryImpl extends BaseRepositoryImpl {

static final Logger logger = Logger.getLogger(BatchUpdateMcaInterestRateRepositoryImpl.class);
private static final String DATE_FORMAT="yyyy-MM-dd HH:mm:ss";

	@Autowired
	private ExtractandUpdateMcaInterestRateJobConfigProperties configProperties;
	
	@Qualifier("dataSourceDCP")
	@Autowired
	DataSource dataSourceDCP;
	
	public Integer addBatchUpdateMCAInterestRateStaging(BatchUpdateMcaInterestRate batchUpdateMCAIntrstRate) throws BatchException {
		try {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("INSERT INTO tbl_batch_update_mca_interest_rate (");
			stringBuilder.append("job_execution_id, mca_term_interest_rate_id, code, tenure,");
			stringBuilder.append("interest_rate,is_processed,created_time,updated_time) ");
			stringBuilder.append("VALUES (?,?,?,?,?,?,?,?)");

			Object[] parameter = new Object[] { 
					batchUpdateMCAIntrstRate.getJobExecutionId(),
					batchUpdateMCAIntrstRate.getMcaTermInterestrateId(),
					batchUpdateMCAIntrstRate.getCode(),
					batchUpdateMCAIntrstRate.getTenure(),
					batchUpdateMCAIntrstRate.getInterestRate(),
					batchUpdateMCAIntrstRate.isProcessed(),
					batchUpdateMCAIntrstRate.getCreatedTime(), 
					batchUpdateMCAIntrstRate.getUpdatedTime()};

			return jdbcTemplate.update(stringBuilder.toString(), parameter);
		} catch (Exception e) {
			logger.error("Exception", e);
			String errorMessage = String.format("Error happened while inserting new record to tbl_batch_update_mca_interest_rate values [%s] ", batchUpdateMCAIntrstRate);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}
	public Integer updateDCPCurrencyRateConfig(CurrencyRateConfig currencyRateConfig) throws BatchException {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("UPDATE TBL_CURRENCY_RATE_CONFIG SET ");
		stringBuilder.append("buy_tt=?,");
		stringBuilder.append("unit=?,");
		stringBuilder.append("updated_time=?,");
		stringBuilder.append("updated_by=? ");
		stringBuilder.append("WHERE id=? ");
		stringBuilder.append("AND code=?");

		try {
			String currentDate = DateUtils.formatDateString(new Date(), DATE_FORMAT);
			
			Object[] parameter = new Object[] {
					currencyRateConfig.getBuyTt(),
					currencyRateConfig.getUnit(),
					currentDate,
					configProperties.getBatchCode(),
					currencyRateConfig.getId(),
					currencyRateConfig.getCode()
			};
			logger.info(String.format("Updating TBL_CURRENCY_RATE_CONFIG: %s", Arrays.toString(parameter)));
			
			jdbcTemplate.setDataSource(dataSourceDCP);
			return jdbcTemplate.update(stringBuilder.toString(), parameter);
			
		} catch(Exception e) {
			String errorMessage = String.format("Error happened while updating record to TBL_CURRENCY_RATE_CONFIG values [%s] ", currencyRateConfig);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		} finally {
			jdbcTemplate.setDataSource(dataSource);
		}

	}
	public Integer updateBatchUpdateMcaInterestRate(BatchUpdateMcaInterestRate batchUpdateMCAIntrstRate) throws BatchException {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("UPDATE TBL_BATCH_UPDATE_MCA_INTEREST_RATE SET ");
		stringBuilder.append("JOB_EXECUTION_ID=?,");
		stringBuilder.append("MCA_TERM_INTEREST_RATE_ID=?,");
		stringBuilder.append("CODE=?,");
		stringBuilder.append("TENURE=?,");
		stringBuilder.append("INTEREST_RATE=?,");
		stringBuilder.append("IS_PROCESSED=?,");
		stringBuilder.append("CREATED_TIME=?,");
		stringBuilder.append("UPDATED_TIME=? ");
		stringBuilder.append(" WHERE ID=? ");
		stringBuilder.append("AND CODE=?");

		try {
			String currentDate = DateUtils.formatDateString(new Date(), DATE_FORMAT);
			
			Object[] parameter = new Object[] { 
					batchUpdateMCAIntrstRate.getJobExecutionId(),
					batchUpdateMCAIntrstRate.getMcaTermInterestrateId(),
					batchUpdateMCAIntrstRate.getCode(),
					batchUpdateMCAIntrstRate.getTenure(),
					batchUpdateMCAIntrstRate.getInterestRate(),
					batchUpdateMCAIntrstRate.isProcessed(),
					currentDate,currentDate,
					batchUpdateMCAIntrstRate.getId(), 
					batchUpdateMCAIntrstRate.getCode() 
			};
			
			logger.info(String.format("Updating TBL_BATCH_UPDATE_MCA_INTEREST_RATE: %s", Arrays.toString(parameter)));

			return jdbcTemplate.update(stringBuilder.toString(), parameter);

		} catch(Exception e) {
			String errorMessage = String.format("Error happened while updating record to TBL_BATCH_UPDATE_MCA_INTEREST_RATE values [%s] ", batchUpdateMCAIntrstRate);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		}
	}

	public Integer updateMcaTermInterestRateConfig(BatchUpdateMcaInterestRate batchUpdateMcaInterestRate) throws BatchException {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("UPDATE TBL_MCA_TERM_INTEREST_RATE_CONFIG SET ");
		stringBuilder.append("INTEREST_RATE=?,");
		stringBuilder.append("CREATED_TIME=?,");
		stringBuilder.append("UPDATED_TIME=?,");
		stringBuilder.append("UPDATED_BY=?" );
		stringBuilder.append(" WHERE CURRENCY_CODE=? ");
		stringBuilder.append("AND TENURE=?");

		try {
			String currentDate = DateUtils.formatDateString(new Date(), DATE_FORMAT);
			
			Object[] parameter = new Object[] {
					batchUpdateMcaInterestRate.getInterestRate(),
					currentDate,currentDate,
					configProperties.getBatchCode(),
					batchUpdateMcaInterestRate.getCode(),
					batchUpdateMcaInterestRate.getTenure()
			};
			logger.info(String.format("Updating TBL_MCA_TERM_INTEREST_RATE_CONFIG: %s", stringBuilder.toString()));
			logger.info(String.format("Updating TBL_MCA_TERM_INTEREST_RATE_CONFIG: %s", Arrays.toString(parameter)));

			jdbcTemplate.setDataSource(dataSourceDCP);
			return jdbcTemplate.update(stringBuilder.toString(), parameter);
			
		} catch(Exception e) {
			String errorMessage = String.format("Error happened while updating record to TBL_MCA_TERM_INTEREST_RATE_CONFIG values [%s] ", batchUpdateMcaInterestRate);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		} finally {
			jdbcTemplate.setDataSource(dataSource);
		}
	}
	
	public Integer updateCurrencyRateConfigData() throws BatchException {
		
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("UPDATE TBL_CURRENCY_RATE_CONFIG SET ");
		stringBuilder.append("INTEREST_RATE=? ");
		stringBuilder.append(" WHERE CODE=?  ");
		
		Object[] parameter =null;
		int rtn = 0;
		List<MCATermInterestRateConfig> mcaTermInterestRateConfig=null;
		try {
		    mcaTermInterestRateConfig=getMCATermInterestValue();
		    for(MCATermInterestRateConfig mcaterminstRate :mcaTermInterestRateConfig)
		    {
			if(mcaterminstRate!=null) {
			   parameter = new Object[] {
					   mcaterminstRate.getInterestRate(),
					   mcaterminstRate.getCurrencyCode()
			};
			logger.info(String.format("Updating TBL_CURRENCY_RATE_CONFIG: %s", Arrays.toString(parameter)));
			
			jdbcTemplate.setDataSource(dataSourceDCP);
			
			}
			rtn=jdbcTemplate.update(stringBuilder.toString(), parameter);
		    }
		    return rtn;
		} catch(Exception e) {
			String errorMessage = String.format("Error happened while updating record to TBL_CURRENCY_RATE_CONFIG values [%s] ", mcaTermInterestRateConfig);
        	logger.error(errorMessage, e);
        	throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, e);
		} finally {
			jdbcTemplate.setDataSource(dataSource);
		}

	}
	
	private List<MCATermInterestRateConfig> getMCATermInterestValue() {
        String sql= "SELECT CURRENCY_CODE, MAX(INTEREST_RATE) as INTEREST_RATE FROM  dcp.dbo.TBL_MCA_TERM_INTEREST_RATE_CONFIG GROUP BY CURRENCY_CODE,TENURE";
	    List<MCATermInterestRateConfig> mcaInterstList=new ArrayList<>();
        List<Map<String,Object>> mcaTerminstRate= jdbcTemplate.queryForList(sql);
	    for(Map row:mcaTerminstRate) {
	    	MCATermInterestRateConfig mcaTermInterestRateConfig=new MCATermInterestRateConfig();
	    	mcaTermInterestRateConfig.setCurrencyCode((String)row.get("CURRENCY_CODE"));
	    	mcaTermInterestRateConfig.setInterestRate((BigDecimal)row.get("INTEREST_RATE"));
	    	 mcaInterstList.add(mcaTermInterestRateConfig);
	    }
	    return mcaInterstList;
	}
	
}
