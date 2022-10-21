package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BoBillerTemplateTagConfig;

@Component
public class BoBillerTemplateTagConfigRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BoBillerTemplateTagConfigRepositoryImpl.class);
   
 	public List<BoBillerTemplateTagConfig> getBillerTemplateTagDetail(Integer templateId) {

 		List<BoBillerTemplateTagConfig> boBillerTemplateTagConfig = new ArrayList<BoBillerTemplateTagConfig>();
		String logMsg = "";
 		
 		String sql = "SELECT * FROM TBL_BO_BILLER_TEMPLATE_TAG_CONFIG WHERE TEMPLATE_ID = ?";
 		logMsg = String.format("SQL Statement = %s", sql);
 		logger.info(logMsg);
 		try {
 			boBillerTemplateTagConfig = jdbcTemplate.query(sql, new Object[] {templateId},
 					new BeanPropertyRowMapper(BoBillerTemplateTagConfig.class));
 		} catch (DataAccessException e) {
 			logger.error(e.getMessage());
 		}
 		return boBillerTemplateTagConfig;
 	}
}