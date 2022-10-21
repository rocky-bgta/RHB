package com.rhbgroup.dcp.bo.batch.framework.repository;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.job.model.BoOlaRegion;
import com.rhbgroup.dcp.bo.batch.job.model.BoOlaToken;
import com.rhbgroup.dcp.bo.batch.job.model.Region;

@Component
public class OlaRepositoryImpl extends BaseRepositoryImpl {

	private static final Logger logger = Logger.getLogger(OlaRepositoryImpl.class);

	public void insertStagedData(Date batchProcessingDate)  {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = dateFormat.format(batchProcessingDate);
		logger.info(String.format("  strDate=%s", strDate));

		int rows = deleteBoOlaTokenData();
		logger.info(String.format("Deleted bo ola rows count = %s", rows));

		int olarows = insertBoOlaToken(strDate);
		logger.info(String.format("Successfully inserted into database TBL_BO_OLA_TOKEN = %s", olarows));
	}


	public int deleteBoOlaTokenData(){
		String sql = "TRUNCATE TABLE dcpbo.dbo.TBL_BO_OLA_TOKEN";
		logger.info(String.format("  sql delete Bo Ola=%s", sql));
		int rowAffected = getJdbcTemplate().update(sql);

		return rowAffected;
	}

	public int insertBoOlaToken(String date) {
		int row =0;

		try {

			String sql = "INSERT INTO dcpbo.dbo.TBL_BO_OLA_TOKEN"
					+"(TOKEN, DEVICE_ID, NAME, PRODUCT_TYPE, PRODUCT_CODE, MOBILE_NO, EMAIL, ID_TYPE, ID_NO, DATE_OF_BIRTH,"
					+ " NATIONALITY, USERNAME, SECRET_PHRASE, KEYED_PASSWORD1, KEYED_PASSWORD2, GENDER, RACE, MARITAL_STATUS,"
					+ " PR_STATUS, PR_COUNTRY, PR_ID_NO, CITIZENSHIP, ADDRESS_LINE1, ADDRESS_LINE2, ADDRESS_LINE3, CITY, STATE,"
					+ " POSTCODE, COUNTRY, PURPOSE_OF_ACCT_OPENING, EMPLOYER_INSTITUTIONAL_REF_ID, SOURCE_OF_WEALTH, SOURCE_OF_FUND,"
					+ " EMPLOYMENT_STATUS, OCCUPATION, SECTOR, NATURE_OF_BUSINESS, COMPANY_NAME, WORK_ADDRESS_LINE1, WORK_ADDRESS_LINE2,"
					+ " WORK_ADDRESS_LINE3, WORK_CITY, WORK_STATE, WORK_POSTCODE, WORK_COUNTRY, MONTHLY_INCOME, ACCOUNT_STATE, ACCOUNT_BRANCH,"
					+ " IS_MSA_HOME_DELIVERY_ALLOWED, IS_MSA_OFFICE_DELIVERY_ALLOWED, ACTIVATION_OPTION, MSA_ACTIVATION_MODE, IS_EKYC_USER, IS_ACTIVE,"
					+ " ACCOUNT_NO, STATUS, ASSESSMENT_RISK_LEVEL, ASSESSMENT_RISK_SCORE, AUDIT_ADDITIONALDATA, IS_TNC_ACCEPTED, IS_CONSENT_RHB_GROUP_ACCEPTED,"
					+ " IS_ETB_CUSTOMER, IS_ETB_CASA_USER, ADDRESS_TYPE, DIVISION_CODE, DEBIT_CARD_NO, TXN_STATUS_CODE, TXN_STATUS, REF_ID, CHANNEL, SEGMENT_CODE,"
					+ " AML_SCREENING_RESULT, API_STATUS_CODE, IS_ISLAMIC, API_STATUS_DESC, IP_ADDRESS, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY)"
					+ " SELECT TOKEN, DEVICE_ID, NAME, PRODUCT_TYPE, PRODUCT_CODE, MOBILE_NO, EMAIL, ID_TYPE, ID_NO, DATE_OF_BIRTH, NATIONALITY, USERNAME, SECRET_PHRASE,"
					+ " KEYED_PASSWORD1, KEYED_PASSWORD2, GENDER, RACE, MARITAL_STATUS, PR_STATUS, PR_COUNTRY, PR_ID_NO, CITIZENSHIP, ADDRESS_LINE1, ADDRESS_LINE2, ADDRESS_LINE3,"
					+ " CITY, STATE, POSTCODE, COUNTRY, PURPOSE_OF_ACCT_OPENING, EMPLOYER_INSTITUTIONAL_REF_ID, SOURCE_OF_WEALTH, SOURCE_OF_FUND, EMPLOYMENT_STATUS, OCCUPATION, SECTOR,"
					+ " NATURE_OF_BUSINESS, COMPANY_NAME, WORK_ADDRESS_LINE1, WORK_ADDRESS_LINE2, WORK_ADDRESS_LINE3, WORK_CITY, WORK_STATE, WORK_POSTCODE, WORK_COUNTRY, "
					+ "ISNULL(MONTHLY_INCOME,0) AS MONTHLY_INCOME, ACCOUNT_STATE, ACCOUNT_BRANCH, IS_MSA_HOME_DELIVERY_ALLOWED, IS_MSA_OFFICE_DELIVERY_ALLOWED,"
					+ " CASE WHEN ACTIVATION_OPTION IS NULL THEN 'Branch' WHEN ACTIVATION_OPTION = 'MSA' THEN 'Rider' ELSE ACTIVATION_OPTION END, MSA_ACTIVATION_MODE,"
					+ " ISNULL(IS_EKYC_USER,0) AS IS_EKYC_USER, IS_ACTIVE, ACCOUNT_NO, STATUS, ASSESSMENT_RISK_LEVEL, ASSESSMENT_RISK_SCORE, AUDIT_ADDITIONALDATA,"
					+ " ISNULL(IS_TNC_ACCEPTED,0) AS IS_TNC_ACCEPTED, ISNULL(IS_CONSENT_RHB_GROUP_ACCEPTED,0) AS IS_CONSENT_RHB_GROUP_ACCEPTED, ISNULL(IS_ETB_CUSTOMER,0) AS IS_ETB_CUSTOMER,"
					+ " ISNULL(IS_ETB_CASA_USER,0) AS IS_ETB_CASA_USER, ADDRESS_TYPE, DIVISION_CODE, DEBIT_CARD_NO, TXN_STATUS_CODE, TXN_STATUS, REF_ID, CHANNEL,"
					+ " ISNULL(SEGMENT_CODE,0) AS SEGMENT_CODE, AML_SCREENING_RESULT, API_STATUS_CODE, IS_ISLAMIC, API_STATUS_DESC, IP_ADDRESS, CREATED_TIME, CREATED_BY, UPDATED_TIME, UPDATED_BY"
					+ " FROM dcp.dbo.TBL_OLA_TOKEN WHERE (STATUS ='C' OR STATUS ='I') AND UPDATED_TIME > =? AND UPDATED_TIME < DATEADD(DAY,1,?)";

			logger.info(String.format("  sql=%s", sql));
			row = getJdbcTemplate().update(sql,date,date);
		} catch (Exception e) {
			logger.error(e);
		}
		return row;
	}



	public void insertDailySummary(Date date){

		int rows = deleteBatchStagedOlaProduct(date);
		logger.info(String.format("Deleted ola product rows count = %s", rows));

		int regionRows = deleteBatchStagedOlaRegion(date);
		logger.info(String.format("Deleted ola region rows count = %s", regionRows));

		List<BoOlaToken> boOlaTokenList = getboOlaTokenList(date);

		logger.info(String.format("bo ola token list=%s", boOlaTokenList));

		Map<String, Map<String, Long>> multipleFieldsMap = boOlaTokenList.stream()
				.collect(
						Collectors.groupingBy(BoOlaToken::getProductType,
								Collectors.groupingBy(BoOlaToken::getChannel,
										Collectors.counting())));

		logger.info(String.format("Product type summary fields=%s", multipleFieldsMap));

		multipleFieldsMap.forEach((k,v) -> v.forEach((a,b) -> insertOlaProduct(k,a,b,date)));

		logger.info("Successfully inserted into database TBL_BATCH_STAGED_OLA_PRODUCT_TYPE_SUMMARY");

		List<BoOlaRegion> boOlaRegionList = getBoOlaRegionList(date);
		logger.info(String.format("bo ola Region list=%s", boOlaRegionList));

		for(BoOlaRegion boOlaRegion:boOlaRegionList) {
			logger.info(String.format("Account branch=%s", boOlaRegion.getAccountBranch()));
			Region region = getRegion(boOlaRegion.getAccountBranch());
			boOlaRegion.setRegion(region.getRegionName());
			insertOlaSummary(date,boOlaRegion);
		}
		logger.info("Successfully inserted into database TBL_BATCH_STAGED_OLA_REGION_SUMMARY");

	}

	public int deleteBatchStagedOlaProduct(Date date){
		String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_OLA_PRODUCT_TYPE_SUMMARY where CAST(OLA_DATE as DATE) = ?";
		logger.info(String.format("  sql delete staged ola product=%s", sql));
		int rowAffected = getJdbcTemplate().update(sql
				, new Object[] {date});

		return rowAffected;
	}

	public int deleteBatchStagedOlaRegion(Date date){
		String sql = "DELETE FROM dcpbo.dbo.TBL_BATCH_STAGED_OLA_REGION_SUMMARY where CAST(OLA_DATE as DATE) = ?";
		logger.info(String.format("  sql delete staged ola region=%s", sql));
		int rowAffected = getJdbcTemplate().update(sql
				, new Object[] {date});

		return rowAffected;
	}

	public List<BoOlaRegion> getBoOlaRegionList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<BoOlaRegion> boOlaRegion;
		String sql =  " SELECT COUNT(*) AS count, a.ACCOUNT_BRANCH,a.CHANNEL,a.ACTIVATION_OPTION FROM dcpbo.dbo.TBL_BO_OLA_TOKEN a WHERE CAST(a.updated_time as DATE)=? GROUP BY a.ACCOUNT_BRANCH,a.CHANNEL,a.ACTIVATION_OPTION";
		logger.info(String.format("get ola product list  sql=%s", sql));
		boOlaRegion = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper(BoOlaRegion.class));
		return boOlaRegion;
	}

	public List<BoOlaToken> getboOlaTokenList(Date date) {
		JdbcTemplate template = getJdbcTemplate();
		List<BoOlaToken> boOlaToken;
		String sql =  " SELECT * FROM dcpbo.dbo.TBL_BO_OLA_TOKEN a WHERE CAST(a.updated_time as DATE)=?";
		logger.info(String.format("get ola product list  sql=%s", sql));
		boOlaToken = template.query(sql,new Object[] {date},
				new BeanPropertyRowMapper(BoOlaToken.class));
		return boOlaToken;
	}

	public Boolean insertOlaProduct(String product,String channel,Long count,Date date) {
		Date now = new Date();
		try {

			getJdbcTemplate().update("INSERT INTO TBL_BATCH_STAGED_OLA_PRODUCT_TYPE_SUMMARY (OLA_DATE, CHANNEL, PRODUCT_TYPE, PRODUCT_COUNT, CREATED_TIME) values (?,?,?,?,?)"
					, new Object[]{
							date
							,channel
							,product
							,count
							,now
					});
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	public Region getRegion(String code) {
		JdbcTemplate template = getJdbcTemplate();
		Region region = new Region();
		String sql =  " SELECT a.region FROM dcp.dbo.TBL_BRANCH a WHERE a.code=? ";
		logger.info(String.format("get branch  list sql=%s", sql));
		try {
			region = template.queryForObject(sql,new Object[] {code},
					new BeanPropertyRowMapper<Region>(Region.class));
		}catch(EmptyResultDataAccessException e) {
			logger.info("region is null");
		}
		return region;
	}


	public Boolean insertOlaSummary(Date date,BoOlaRegion boOlaRegion) {
		Date now = new Date();
		try {

			getJdbcTemplate().update("INSERT INTO TBL_BATCH_STAGED_OLA_REGION_SUMMARY (OLA_DATE, CHANNEL, REGION, BRANCH_CODE, ACTIVATION_OPTION,PRODUCT_COUNT, CREATED_TIME) values (?,?,?,?,?,?,?)"
					, new Object[]{
							date
							,boOlaRegion.getChannel()
							,boOlaRegion.getRegion()
							,boOlaRegion.getAccountBranch()
							,boOlaRegion.getActivationOption()
							,boOlaRegion.getCount()
							,now
					});
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

}
