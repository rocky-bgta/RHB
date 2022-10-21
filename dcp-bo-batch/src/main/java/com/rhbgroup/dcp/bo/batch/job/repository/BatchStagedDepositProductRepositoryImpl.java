package com.rhbgroup.dcp.bo.batch.job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedDepositProduct;
import com.rhbgroup.dcp.bo.batch.job.model.StagedDepositProducts;

@Component
@Lazy
public class BatchStagedDepositProductRepositoryImpl extends BaseRepositoryImpl {
	private static final Logger logger = Logger.getLogger(BatchStagedDepositProductRepositoryImpl.class);

	public int addBatchBatchStageDepositProduct(List<BatchStagedDepositProduct> batchStagedDepositProducts) {
		String sql = "INSERT INTO TBL_BATCH_STAGED_DEPOSIT_PRODUCT"
				+ "(job_execution_id,deposit_type,product_code,product_name,tenure"
				+ ",interest_rate,is_islamic,promo_end_date,is_processed,file_name,process_date,created_time)"
				+ " VALUES " + "(?,?,?,?,?,?,?,?,?,?,?,?)";
		jdbcTemplate.setDataSource(dataSource);
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				BatchStagedDepositProduct batchStagedDepositProduct = batchStagedDepositProducts.get(i);
				ps.setLong(1, batchStagedDepositProduct.getJobExecutionId());
				ps.setString(2, batchStagedDepositProduct.getDepositType());
				ps.setString(3, batchStagedDepositProduct.getProductCode());
				ps.setString(4, batchStagedDepositProduct.getProductName());
				ps.setLong(5, batchStagedDepositProduct.getTenure());
				ps.setDouble(6, batchStagedDepositProduct.getInterestRate());
				ps.setString(7, batchStagedDepositProduct.getIslamic());
				ps.setTimestamp(8, batchStagedDepositProduct.getPromoEndDate());
				ps.setString(9, batchStagedDepositProduct.getProcessed());
				ps.setString(10, batchStagedDepositProduct.getFileName());
				ps.setTimestamp(11, batchStagedDepositProduct.getProcessDate());
				ps.setTimestamp(12, batchStagedDepositProduct.getCreatedTime());
			}

			@Override
			public int getBatchSize() {
				return batchStagedDepositProducts.size();
			}
		});
		return row.length;
	}

	public int deleteBatchStagedDepositProductData(){
		String sql = "TRUNCATE TABLE TBL_BATCH_STAGED_DEPOSIT_PRODUCT";
		logger.info(String.format("  sql delete batch staged deposit product=%s", sql));
		return jdbcTemplate.update(sql);
	}

	public int findBatchStagedDepositProductFileLoaded(String fileName) throws BatchException {
		int row = 0;
		try {
			String sql = "SELECT COUNT(ID) FROM TBL_BATCH_STAGED_DEPOSIT_PRODUCT WHERE file_name=?";
			row = getJdbcTemplate().queryForObject(sql, new Object[] { fileName }, Integer.class);
		} catch (Exception ex) {
			logger.error(String.format("Exception checking file loaded TBL_BATCH_STAGED_DEPOSIT_PRODUCT, %s ", ex));
			throw new BatchException(BatchErrorCode.DB_SYSTEM_ERROR, BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE, ex);
		}
		return row;
	}

	public List<StagedDepositProducts> getStagedDepositProducts() {
		String sql = "SELECT stagedproducts.product_code,stagedproducts.interest_rate,stagedproducts.promo_end_date,stagedproducts.product_name,\r\n"
				+ "products.product_code FROM dcpbo.dbo.TBL_BATCH_STAGED_DEPOSIT_PRODUCT stagedproducts JOIN dcp.dbo.TBL_DEPOSIT_PRODUCT products ON stagedproducts.product_code=products.product_code; ";
		logger.info(String.format("get staged deposit products sql=%s", sql));
		List<StagedDepositProducts>	stagedDepositProducts = jdbcTemplate.query(sql, new BeanPropertyRowMapper(StagedDepositProducts.class));
		return stagedDepositProducts;
	}
}
