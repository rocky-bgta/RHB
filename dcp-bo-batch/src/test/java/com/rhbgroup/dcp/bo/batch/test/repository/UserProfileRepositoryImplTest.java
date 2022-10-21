package com.rhbgroup.dcp.bo.batch.test.repository;

import javax.sql.DataSource;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.UncategorizedScriptException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.repository.UserProfileRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

import lombok.Getter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BatchTestConfigHSQL.class,UserProfileRepositoryImpl.class})
@ActiveProfiles("test")
public class UserProfileRepositoryImplTest extends BaseJobTest {
	private static final Logger logger = Logger.getLogger(UserProfileRepositoryImplTest.class);
	
	@Autowired
	private DataSource dataSourceDCP;

	@Autowired
	private UserProfileRepositoryImpl userProfileRepositoryImpl;

	@MockBean
	@Getter
	private JdbcTemplate jdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testUpdateCIS() {
//		assertEquals(1, userProfileRepositoryImpl.updateUserStatusCISNo("00000123456", "I"));

		when(getJdbcTemplate().update(Mockito.anyString(), Mockito.any(Object.class))).thenReturn(1);
		userProfileRepositoryImpl.updateUserStatusCISNo("00000123456", "I");
		verify(getJdbcTemplate()).update("update TBL_USER_PROFILE set USER_STATUS=? where CIS_NO=?", new Object[] {"I", "00000123456"});
	}

	@Test
	public void testPositiveGetTblUserProfile() throws BatchException {
		List<Map<String, Object>> extractUserProfiles = new ArrayList<>();
		Map<String, Object> extractUserProfile1 = new HashMap<>();
		extractUserProfile1.put("ID", "1");
		extractUserProfile1.put("CIS_NO", "00000123456");
		extractUserProfile1.put("USER_STATUS", "A");

		Map<String, Object> extractUserProfile2 = new HashMap<>();
		extractUserProfile2.put("ID", "2");
		extractUserProfile2.put("CIS_NO", null);
		extractUserProfile2.put("USER_STATUS", "E");

		Map<String, Object> extractUserProfile3 = new HashMap<>();
		extractUserProfile3.put("ID", "3");
		extractUserProfile3.put("CIS_NO", "00000123458");
		extractUserProfile3.put("USER_STATUS", "I");

		extractUserProfiles.add(extractUserProfile1);
		extractUserProfiles.add(extractUserProfile2);
		extractUserProfiles.add(extractUserProfile3);

		when(getJdbcTemplate().queryForList(Mockito.anyString())).thenReturn(extractUserProfiles);
		List<Map<String, Object>> result = userProfileRepositoryImpl.getUserProfiles();
		assertEquals(3, result.size());
		verify(getJdbcTemplate()).queryForList("SELECT TOP 10 ID, CIS_NO, USER_STATUS FROM TBL_USER_PROFILE");
	}

	@Test(expected = BatchException.class)
	public void testNegativeGetTblUserProfile() throws BatchException {
		when(getJdbcTemplate().queryForList(Mockito.anyString())).thenThrow(UncategorizedScriptException.class);
		userProfileRepositoryImpl.getUserProfiles();
		verify(getJdbcTemplate()).queryForList("SELECT TOP 10 ID, CIS_NO, USER_STATUS FROM TBL_USER_PROFILE");
	}
	
	@Before
	public void setup() {
		//insertRecord();
	}
	
	@After
	public void cleanup() {
		//deleteRecord();
	}
	
	private void insertRecord() {
		String insertSQL="INSERT INTO TBL_USER_PROFILE (ID,CIS_NO,USER_STATUS) values (1,'00000123456','A')";
//		jdbcTemplate.setDataSource(dataSourceDCP);
//		int row = jdbcTemplate.update(insertSQL);
		getJdbcTemplate().setDataSource(dataSourceDCP);
		int row = getJdbcTemplate().update(insertSQL);
		logger.info(String.format("insert %s into TBL_USER_PROFILE", row));		
	}
	
	private void deleteRecord() {
		String deleteSQL="DELETE FROM TBL_USER_PROFILE ";
//		jdbcTemplate.setDataSource(dataSourceDCP);
//		int row = jdbcTemplate.update(deleteSQL);
		getJdbcTemplate().setDataSource(dataSourceDCP);
		int row = getJdbcTemplate().update(deleteSQL);
		logger.info(String.format("delete %s from TBL_USER_PROFILE", row));		
	}
}