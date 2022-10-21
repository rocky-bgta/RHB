package com.rhbgroup.dcp.bo.batch.job.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.sql.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BatchBillerPaymentConfig;
import com.rhbgroup.dcp.bo.batch.job.model.BatchBillerPaymentFile;
import com.rhbgroup.dcp.bo.batch.job.model.BillerPaymentOutboundConfig;

@Component
public class BatchBillerPaymentFileRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BatchBillerPaymentFileRepositoryImpl.class);
	@Autowired
	private BillPaymentConfigOutboundRepositoryImpl billConfigOutboundRepo;
	@Autowired
	BillerCategoryRepositoryImpl billerCategoryRepositoryImpl;

	public void getBillerPaymentConfig(Date batchProcessingDate){
		logger.info("getBillerpamentConfig in BatchBillerPaymentFileRepository");
		List<BillerPaymentOutboundConfig> listOfBillerConfig = billConfigOutboundRepo.getActiveBillerConfigOutBound();
		logger.debug("list of biller config records: "+listOfBillerConfig);
		createBillerPaymentFile(listOfBillerConfig,batchProcessingDate);
	}

	public int createBillerPaymentFile(List<BillerPaymentOutboundConfig> listOfBillerConfig,Date batchProcessingDate) {
		
		logger.info("Inserting records into PaymentFile");
		List<BatchBillerPaymentFile> batchBillerPaymentFiles = new ArrayList<>();
		String fileGeneratedPath = "empty";
		String errorMessage = "empty";
		String createdBy = "admin";
		String updatedBy = "admin";

		java.sql.Date todaysDate = new java.sql.Date(new java.util.Date().getTime());
		Calendar c = Calendar.getInstance();
		c.setTime(todaysDate);
		c.add(Calendar.DATE, -1);

		long time = c.getTimeInMillis();

		Timestamp now = new Timestamp(System.currentTimeMillis());
		for (BillerPaymentOutboundConfig records : listOfBillerConfig) {
			BatchBillerPaymentFile batchBillerPaymentFile = new BatchBillerPaymentFile();
			batchBillerPaymentFile.setBillerPaymentConfigId(records.getId());
			batchBillerPaymentFile.setBillerCode(records.getBillerCode());
			batchBillerPaymentFile.setBillerName(records.getBillerAccName());
			String categoryName = billerCategoryRepositoryImpl
					.findCategoryNameByCategoryId(records.getCategoryId());
			logger.debug("biller category:billerCategory");
			batchBillerPaymentFile.setBillerCategory(categoryName);
			batchBillerPaymentFile.setFileGeneratedPath(fileGeneratedPath);
			batchBillerPaymentFile.setFileGenerated(false);
			batchBillerPaymentFile.setFileDelivered(false);
			batchBillerPaymentFile.setError(false);
			batchBillerPaymentFile.setErrorMessage(errorMessage);
			batchBillerPaymentFile.setFileDate(batchProcessingDate);
			batchBillerPaymentFile.setCreatedTime(now);
			batchBillerPaymentFile.setCreatedBy(createdBy);
			batchBillerPaymentFile.setUpdatedTime(now);
			batchBillerPaymentFile.setUpdatedBy(updatedBy);
			batchBillerPaymentFiles.add(batchBillerPaymentFile);
		}
		logger.debug("list of payment files:"+batchBillerPaymentFiles);
		jdbcTemplate.setDataSource(dataSource);
		String sql = "INSERT INTO TBL_BATCH_BILLER_PAYMENT_FILE"
				+ "(biller_payment_config_id, biller_code, biller_name, biller_category, file_generated_path, is_file_generated, is_file_delivered, is_error, error_message, file_date, created_time, created_by, updated_time, updated_by)"
				+ " VALUES " + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		int[] row = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				BatchBillerPaymentFile batchBillerPaymentFile = batchBillerPaymentFiles.get(i);
				ps.setLong(1, batchBillerPaymentFile.getBillerPaymentConfigId());
				ps.setString(2, batchBillerPaymentFile.getBillerCode());
				ps.setString(3, batchBillerPaymentFile.getBillerName());
				ps.setString(4, batchBillerPaymentFile.getBillerCategory());
				ps.setString(5, batchBillerPaymentFile.getFileGeneratedPath());
				ps.setBoolean(6, batchBillerPaymentFile.isFileGenerated());
				ps.setBoolean(7, batchBillerPaymentFile.isFileDelivered());
				ps.setBoolean(8, batchBillerPaymentFile.isError());
				ps.setString(9, batchBillerPaymentFile.getErrorMessage());
				ps.setDate(10, batchBillerPaymentFile.getFileDate());
				ps.setTimestamp(11, batchBillerPaymentFile.getCreatedTime());
				ps.setString(12, batchBillerPaymentFile.getCreatedBy());
				ps.setTimestamp(13, batchBillerPaymentFile.getUpdatedTime());
				ps.setString(14, batchBillerPaymentFile.getUpdatedBy());
			}

			@Override
			public int getBatchSize() {
				return batchBillerPaymentFiles.size();
			}
		});
		return row.length;

	}
	java.util.Date date = new java.util.Date();
	String modifiedDate= new SimpleDateFormat("yyyy-MM-dd").format(date);

	public int updateBatchBillerPaymentFilePath(BatchBillerPaymentConfig billerPaymentConfig, int isGenerated,Date batchProcessingDate) {
    	int impacted=0;
    	String updateSQL="UPDATE TBL_BATCH_BILLER_PAYMENT_FILE SET FILE_GENERATED_PATH=?, IS_FILE_GENERATED=?, UPDATED_BY=?, UPDATED_TIME=?"
    			+" WHERE BILLER_CODE=? AND CAST(FILE_DATE AS DATE)=?";
    	impacted=getJdbcTemplate().update(updateSQL,billerPaymentConfig.getFtpFolder(),isGenerated, billerPaymentConfig.getUpdatedBy(), billerPaymentConfig.getUpdatedTime(), billerPaymentConfig.getBillerCode(),batchProcessingDate);
    	logger.debug(String.format("BatchBillerPaymentFiles update path in DB impacted row [%s]", impacted));
    	return impacted;
    }
}