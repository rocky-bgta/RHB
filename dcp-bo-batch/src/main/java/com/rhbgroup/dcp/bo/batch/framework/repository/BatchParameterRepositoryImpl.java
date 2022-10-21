package com.rhbgroup.dcp.bo.batch.framework.repository;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.model.BatchParameter;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundConfig;
import com.rhbgroup.dcp.bo.batch.job.repository.BillPaymentConfigOutboundRepositoryImpl;

@Component
public class BatchParameterRepositoryImpl extends BaseRepositoryImpl {
	
	@Autowired
	private BillPaymentConfigOutboundRepositoryImpl billConfigOutboundRepo;
	
    public List<BatchParameter> getBatchParametres() {
        List<BatchParameter> batchParameters= new ArrayList<>();
        List<Map<String,Object>> rows = getJdbcTemplate().queryForList("SELECT PARAMETER_KEY, PARAMETER_VALUE FROM TBL_BATCH_CONFIG ");
        if(rows!=null && rows.size()>0) {
            for (Map row : rows) {
                BatchParameter batchParameter = new BatchParameter();
                batchParameter.setName((String) row.get("PARAMETER_KEY"));
                batchParameter.setValue((String) row.get("PARAMETER_VALUE"));
                batchParameters.add(batchParameter);
            }
        }
        return batchParameters;
    }

    @Transactional
    public void updateBatchSystemDate(Date date) throws BatchException {
        int rowaffected=getJdbcTemplate().update("UPDATE TBL_BATCH_CONFIG SET PARAMETER_VALUE=?, UPDATED_TIME=? WHERE PARAMETER_KEY=?",
                DateUtils.formatDateString(date,BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT)
                ,new Date()
                ,BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
        if(rowaffected<=0)
        {
            throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR,BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY+" update failed in batch config");
        }
    }

    public void updateBillerPaymentConfig(BatchParameter batchSystemDate) {
    	
    	List<BillerPaymentOutboundConfig> listBillerConfig = billConfigOutboundRepo.getActiveOrNonInactiveOrDeleteBillerConfigOutbound(batchSystemDate);
		updateBllerConfigBatch(listBillerConfig);
    }
    
	public int updateBllerConfigBatch(List<BillerPaymentOutboundConfig> records) {
		
		long time = System.currentTimeMillis();
        String sql = "UPDATE TBL_BATCH_BILLER_PAYMENT_CONFIG SET IS_REQUIRED_TO_EXECUTE=1,UPDATED_TIME=? WHERE BILLER_CODE=?;";
        
		int [] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				BillerPaymentOutboundConfig billerConfig = records.get(i);
				ps.setDate(1, new java.sql.Date(time));
				ps.setString(2, billerConfig.getBillerCode());
			}
					
			@Override
			public int getBatchSize() {
				return records.size();
			}
		  });
		return row.length;
	}
    
    public void updateIBGRejectLastProcessedValue(String paramValue) throws BatchException {
    	int row = getJdbcTemplate().update("UPDATE TBL_BATCH_CONFIG SET PARAMETER_VALUE=?,UPDATED_TIME=? WHERE PARAMETER_KEY=?",
    			paramValue,
    			new Date(),
    			"ibg.reject.last.processed.success.job.execution.id");
    	if(row <=0) {
            throw new BatchException(BatchErrorCode.CONFIG_SYSTEM_ERROR,"IBGRejectLastProcessedSuccessJobExecutionId - update failed in batch config");

    	}
    }
    
    public BatchParameter getBatchParameter(String batchJobParameter) {  
    	return getJdbcTemplate().query(
    		String.format("SELECT PARAMETER_KEY, PARAMETER_VALUE FROM TBL_BATCH_CONFIG WHERE PARAMETER_KEY = '%s'", batchJobParameter), 
    		new ResultSetExtractor<BatchParameter>() {
				@Override
				public BatchParameter extractData(ResultSet rs) throws SQLException {
					BatchParameter batchParameter = null;
					while(rs.next()) {
						batchParameter = new BatchParameter();
				        batchParameter.setName(rs.getString("PARAMETER_KEY"));
				        batchParameter.setValue(rs.getString("PARAMETER_VALUE"));
					}
					return batchParameter;
				}
    		}
    	);
	}
    
    public int updateBatchParameter(String batchJobParameter, String value) {
    	return getJdbcTemplate().update(
    		String.format("UPDATE TBL_BATCH_CONFIG SET PARAMETER_VALUE = '%s' WHERE PARAMETER_KEY = '%s'", value, batchJobParameter)
    	);
    }
}
