package com.rhbgroup.dcp.bo.batch.job.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.BoBillerTemplateTagFieldConfig;

@Component
public class BoBillerTemplateTagFieldConfigRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(BoBillerTemplateTagFieldConfigRepositoryImpl.class);
   
 	public List<BoBillerTemplateTagFieldConfig> getBillerTemplateTagFieldDetail(Integer templateTagId) {

 		List<BoBillerTemplateTagFieldConfig> boBillerTemplateTagFieldConfig = new ArrayList<BoBillerTemplateTagFieldConfig>();
		String logMsg = "";
 		
 		String sql = "SELECT * FROM TBL_BO_BILLER_TEMPLATE_TAG_FIELD_CONFIG WHERE TEMPLATE_TAG_ID = ?";
 		logMsg = String.format("SQL Statement = %s", sql);
 		logger.info(logMsg);
 		try {
 			boBillerTemplateTagFieldConfig = jdbcTemplate.query(sql, new Object[] {templateTagId},
 					new BeanPropertyRowMapper(BoBillerTemplateTagFieldConfig.class));
 		} catch (DataAccessException e) {
 			logger.error(e.getMessage());
 		}
 		return boBillerTemplateTagFieldConfig;
 	}
}