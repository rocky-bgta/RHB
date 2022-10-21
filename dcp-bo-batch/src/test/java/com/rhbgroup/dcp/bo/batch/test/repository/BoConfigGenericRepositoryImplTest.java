package com.rhbgroup.dcp.bo.batch.test.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.BoConfigGeneric;
import com.rhbgroup.dcp.bo.batch.job.repository.BoConfigGenericRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.rowmapper.BoConfigGenericRowMapper;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class BoConfigGenericRepositoryImplTest extends BaseJobTest {

	@Autowired
	private BoConfigGenericRepositoryImpl repository;
	
	@MockBean
	JdbcTemplate mockJdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testPositiveGetUserStatusDaysConfig() throws BatchException {
		int maxLimit = 10;
		
		List<BoConfigGeneric> boConfigGenerics = new ArrayList<>();
		for(int i=0; i<maxLimit; i++) {
			BoConfigGeneric boConfigGeneric = new BoConfigGeneric();
			boConfigGenerics.add(boConfigGeneric);
		}
		
		when(mockJdbcTemplate.query(
				Mockito.anyString(),
				Mockito.any(BoConfigGenericRowMapper.class)))
			.thenReturn(boConfigGenerics);
		
		List<BoConfigGeneric> result = repository.getUserStatusDaysConfig();
		assertEquals(maxLimit, result.size());
	}
	
	@Test
	public void testNegativeGetUserStatusDaysConfig() throws BatchException {
		when(mockJdbcTemplate.query(
			Mockito.anyString(),
			Mockito.any(BoConfigGenericRowMapper.class)))
		.thenThrow(UncategorizedSQLException.class);
		
		expectedEx.expect(BatchException.class);
		expectedEx.expectMessage(Matchers.containsString(BatchErrorCode.DB_SYSTEM_ERROR + ":" + BatchErrorCode.DB_SYSTEM_ERROR_MESSAGE));
		
		repository.getUserStatusDaysConfig();
	}
}
