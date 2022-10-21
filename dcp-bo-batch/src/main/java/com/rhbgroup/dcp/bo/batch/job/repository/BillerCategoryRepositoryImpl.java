package com.rhbgroup.dcp.bo.batch.job.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;

import javax.sql.DataSource;

@Component
public class BillerCategoryRepositoryImpl extends BaseRepositoryImpl {

	@Qualifier("dataSourceDCP")
	@Autowired
	DataSource dataSourceDCP;

	public String findCategoryNameByCategoryId(Integer categoryId) {
		jdbcTemplate.setDataSource(dataSourceDCP);
		String sql = "SELECT CATEGORY_NAME from TBL_BILLER_CATEGORY where ID=?";
		return jdbcTemplate.queryForObject(sql, new Object[] {categoryId}, String.class);
	}

}
