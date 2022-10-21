package com.rhbgroup.dcp.bo.batch.job.repository;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BankDownTime;

@Component
@Lazy
public class BankDowntimeRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BankDowntimeRepositoryImpl.class);

	@Qualifier("dataSourceDCP")
	@Autowired
	DataSource dataSourceDCP;

	// Get bank detail based on bank_id
	public BankDownTime getBankDetails(Integer bankId) {

		jdbcTemplate.setDataSource(dataSourceDCP);
		String logMsg = "";
		BankDownTime bankDownTime = new BankDownTime();
		
		String sql = "SELECT * FROM TBL_BANK_DOWNTIME WHERE BANK_ID = ?";
		logMsg = String.format("SQL Statement = %s", sql);
		logger.info(logMsg);
		try {
			bankDownTime = (BankDownTime) jdbcTemplate.queryForObject(sql, new Object[] {bankId},
					new BeanPropertyRowMapper<BankDownTime>(BankDownTime.class));
		} catch (DataAccessException e) {
			logger.error(e.getMessage());
		}
		return bankDownTime;
	}

}
