package com.rhbgroup.dcp.bo.batch.test.utils;

//import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.BoLoginConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.utils.EAIUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractAndUpdateExchangeRateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.EAIExchangeRateResponse;
import com.rhbgroup.dcp.bo.batch.job.model.EAISessionTokenResponse;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfig.class})
@ActiveProfiles("test")
public class EAIUtilsTest extends BaseJobTest {
	
	private static final Logger logger = Logger.getLogger(EAIUtilsTest.class);
	
	@Autowired
	private BoLoginConfigProperties boLoginProperties;
	
	@Autowired
	private ExtractAndUpdateExchangeRateJobConfigProperties jobConfigProperties;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;
	
	@Test
	public void testStatus200GetSessionToken() throws JSONException {
		logger.info("EAIUtilsTest start testStatus200GetSessionToken");
		RestTemplate restTemplate = PowerMockito.mock(RestTemplate.class);
		ResponseEntity<String> respEntity = new ResponseEntity<String>("{ \"sessionToken\":\"XXXXX\" }", HttpStatus.OK);
	    // Set expectation on mock RestTemplate
	    PowerMockito.when(restTemplate.exchange(
	          Matchers.anyString(), 
	          Matchers.any(HttpMethod.class),
	          Matchers.<HttpEntity<?>> any(),
	          Matchers.any(Class.class)))
	      .thenReturn(respEntity);
	    
		EAISessionTokenResponse response = EAIUtils.getSessionToken(boLoginProperties.getUsername(), 
				boLoginProperties.getPassword(), boLoginProperties.getApi(), restTemplate);
		//assertEquals("200", Integer.toString(response.getStatusCodeValue()));
		assertNotNull(response);
	}
	
	@Test
	public void testStatus401GetSessionToken() throws JSONException {
		logger.info("EAIUtilsTest start testStatus401GetSessionToken");
		RestTemplate restTemplate = PowerMockito.mock(RestTemplate.class);
	    // Set expectation on mock RestTemplate
	    PowerMockito.when(restTemplate.exchange(
	          Matchers.anyString(), 
	          Matchers.any(HttpMethod.class),
	          Matchers.<HttpEntity<?>> any(),
	          Matchers.any(Class.class)))
	      .thenThrow(new RestClientException("401 Unauthorized"));

		expectedEx.expectMessage("401 Unauthorized");
		EAIUtils.getSessionToken("xxx", "xxx", boLoginProperties.getApi(), restTemplate);
	}
	
	@Test
	public void testStatus200GetEAIXchangeRate() throws JSONException {
		logger.info("EAIUtilsTest start testStatus200GetEAIXchangeRate");
		RestTemplate restTemplate = PowerMockito.mock(RestTemplate.class);
		ResponseEntity<String> respEntity = new ResponseEntity<String>("{ \"sessionToken\":\"XXXXX\" }", HttpStatus.OK);
	    // Set expectation on mock RestTemplate
	    PowerMockito.when(restTemplate.exchange(
	          Matchers.anyString(), 
	          Matchers.any(HttpMethod.class),
	          Matchers.<HttpEntity<?>> any(),
	          Matchers.any(Class.class)))
	      .thenReturn(respEntity);
		EAISessionTokenResponse responseSessionToken = EAIUtils.getSessionToken(boLoginProperties.getUsername(), 
				boLoginProperties.getPassword(), boLoginProperties.getApi(), restTemplate);
		
		EAIExchangeRateResponse response = EAIUtils.getEAIExchangeRate("AUD", 
				jobConfigProperties.getRestAPI(), responseSessionToken.getSessionToken(), restTemplate);
		
		//assertEquals("200", Integer.toString(response.getStatusCodeValue()));
		assertNotNull(response);
	}
	
	@Test
	public void testStatus500GetEAIXchangeRate() throws JSONException {
		logger.info("EAIUtilsTest start testStatus500GetEAIXchangeRate");
		RestTemplate restTemplate = PowerMockito.mock(RestTemplate.class);
		ResponseEntity<String> respEntity = new ResponseEntity<String>("{ \"sessionToken\":\"XXXXX\" }", HttpStatus.OK);
	    // Set expectation on mock RestTemplate
	    PowerMockito.when(restTemplate.exchange(
	          Matchers.anyString(), 
	          Matchers.any(HttpMethod.class),
	          Matchers.<HttpEntity<?>> any(),
	          Matchers.any(Class.class)))
	      .thenReturn(respEntity).thenThrow(new RestClientException("500 Internal Server Error"));

		EAISessionTokenResponse responseSessionToken = EAIUtils.getSessionToken(boLoginProperties.getUsername(), 
				boLoginProperties.getPassword(), boLoginProperties.getApi(), restTemplate);
		
		expectedEx.expectMessage("500 Internal Server Error");
		
		EAIUtils.getEAIExchangeRate("ADU", jobConfigProperties.getRestAPI(), responseSessionToken.getSessionToken(), restTemplate);
	}

}
