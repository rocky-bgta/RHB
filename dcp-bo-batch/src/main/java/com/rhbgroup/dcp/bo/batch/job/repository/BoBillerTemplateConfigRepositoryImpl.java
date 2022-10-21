package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BoBillerTemplateConfig;

@Component
public class BoBillerTemplateConfigRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BoBillerTemplateConfigRepositoryImpl.class);
   
 	public BoBillerTemplateConfig getBillerTemplateDetail(Integer templateId) {

 		BoBillerTemplateConfig boBillerTemplateConfig = new BoBillerTemplateConfig();
		String logMsg = "";
 		
 		String sql = "SELECT * FROM TBL_BO_BILLER_TEMPLATE_CONFIG WHERE TEMPLATE_ID = ?";
 		logMsg = String.format("SQL Statement = %s", sql);
 		logger.info(logMsg);
 		try {
 			boBillerTemplateConfig = (BoBillerTemplateConfig) jdbcTemplate.queryForObject(sql, new Object[] {templateId},
 					new BeanPropertyRowMapper<BoBillerTemplateConfig>(BoBillerTemplateConfig.class));
 		} catch (DataAccessException e) {
 			logger.error(e.getMessage());
 		}
 		return boBillerTemplateConfig;
 	}
}