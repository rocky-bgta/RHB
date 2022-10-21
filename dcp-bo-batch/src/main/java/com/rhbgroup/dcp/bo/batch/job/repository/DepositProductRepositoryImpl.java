package com.rhbgroup.dcp.bo.batch.job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.DepositProduct;
import com.rhbgroup.dcp.bo.batch.job.model.StagedDepositProducts;

@Component
@Lazy
public class DepositProductRepositoryImpl extends BaseRepositoryImpl {
	private static final Logger logger = Logger.getLogger(DepositProductRepositoryImpl.class);
	@Autowired
	private BatchStagedDepositProductRepositoryImpl batchStagedDepositProductRepositoryImpl;

	public void getDepositProducts() {
		List<StagedDepositProducts> listOfSatgedProducts = batchStagedDepositProductRepositoryImpl
				.getStagedDepositProducts();
		logger.debug("list of staged deposit products" + listOfSatgedProducts);
		updateDepositProduct(listOfSatgedProducts);

	}

	private int updateDepositProduct(List<StagedDepositProducts> records) {
		
		String sql = "UPDATE dcp.dbo.TBL_DEPOSIT_PRODUCT SET INTEREST_RATE=?, PROMO_END_DATE=?, UPDATED_TIME=?, UPDATED_BY=? WHERE PRODUCT_CODE =?;";
		logger.debug("updated deposit products" + sql);
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Timestamp updatedTime = new Timestamp(System.currentTimeMillis());
				String updatedBy = "Admin";
				StagedDepositProducts depositProducts = records.get(i);
				ps.setDouble(1, depositProducts.getInterestRate());
				ps.setTimestamp(2, depositProducts.getPromoEndDate());
				ps.setTimestamp(3, updatedTime);
				ps.setString(4, updatedBy);
				ps.setString(5, depositProducts.getProductCode());
			}

			@Override
			public int getBatchSize() {

				return records.size();
			}
		});
		return row.length;
	}

}