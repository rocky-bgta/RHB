package com.rhbgroup.dcp.bo.batch.framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.bo.batch.framework.vo.ExtractInterestExchangeRateRequest;
import com.rhbgroup.dcp.bo.batch.framework.vo.InterestRate;
import com.rhbgroup.dcp.bo.batch.job.model.BatchUpdateMcaInterestRate;
import com.rhbgroup.dcp.bo.batch.job.model.EAIExchangeRateResponse;
import com.rhbgroup.dcp.bo.batch.job.model.EAIInterestRateResponse;
import com.rhbgroup.dcp.bo.batch.job.model.EAISessionTokenResponse;
import com.rhbgroup.dcp.estatement.model.EPullAutoEnrollmentRequest;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public final class EAIUtils {
	private static final Logger logger = Logger.getLogger(EAIUtils.class);
	private static final String APPLICATION_CONTENT="application/json";
	private static final String CONTENT_TYPE="Content-Type";
	private static final String AUTHORIZATION = "Authorization";
	private static final String BEARER = "Bearer ";

	@Autowired
	private static HttpComponentsClientHttpRequestFactory clientRequestFactory;
	
	private EAIUtils() {
		throw new IllegalStateException("Utility Class");
	}
	
	public static EAISessionTokenResponse getSessionToken(String username, String password, String restAPILogin, RestTemplate restTemplate) throws JSONException {
		
		// Header
		HttpHeaders header = new HttpHeaders();
		header.set(CONTENT_TYPE, APPLICATION_CONTENT);
		
		// Body
		JSONObject requestBody = new JSONObject();
		requestBody.put("username", username);
		requestBody.put("password", password);

		logger.debug("requestBody: " + requestBody.toString());
		// Request
		HttpEntity<String> requestEntity = new HttpEntity<String>(requestBody.toString(), header);
		// Response
		ResponseEntity<String> responseEntity = restTemplate.exchange(restAPILogin, HttpMethod.POST, requestEntity, String.class);
		
		logger.info(String.format("Request EAI Login response code [%s]", responseEntity.getStatusCode()));
		return JsonUtils.jsonToObject(responseEntity.getBody(), EAISessionTokenResponse.class);
	}
	
	public static EAIExchangeRateResponse getEAIExchangeRate(String code, String restAPILink, String sessionToken, RestTemplate restTemplate) {
		// Header
		HttpHeaders header = new HttpHeaders();
		header.set(CONTENT_TYPE, APPLICATION_CONTENT);
		header.set(AUTHORIZATION, BEARER + sessionToken);
		
		// Request
		HttpEntity<String> requestEntity = new HttpEntity<String>(header);
		
		// Response
		String url = restAPILink.replace("{currencyCode}", code);
		ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
		logger.info(String.format("Request EAI Exchange rate [%s], response code[%s], response body %s",code, responseEntity.getStatusCode(), responseEntity.getBody()));
		return JsonUtils.jsonToObject(responseEntity.getBody(), EAIExchangeRateResponse.class);
	}
	public static EAIInterestRateResponse getEAIInterestRate(BatchUpdateMcaInterestRate batchUpdateMcaInterestRate, String restAPILink, String sessionToken, RestTemplate restTemplate) throws JsonProcessingException {
		// Header
		HttpHeaders header = new HttpHeaders();
		header.set(CONTENT_TYPE, APPLICATION_CONTENT);
		header.set(AUTHORIZATION, BEARER + sessionToken);
		List<InterestRate> rate=new ArrayList<InterestRate>();


		InterestRate interestRate=new InterestRate();
		interestRate.setCode(batchUpdateMcaInterestRate.getCode());
		interestRate.setTenure(batchUpdateMcaInterestRate.getTenure());
		rate.add(interestRate);

		ExtractInterestExchangeRateRequest extractInterestExchangeRateRequest=new ExtractInterestExchangeRateRequest();
		extractInterestExchangeRateRequest.setRate(rate);

		ObjectMapper objectMapper=new ObjectMapper();

		// Request
		HttpEntity<String> requestEntity = new HttpEntity<String>(objectMapper.writeValueAsString(extractInterestExchangeRateRequest),header);
		
		// Response
		logger.info(String.format("restAPI body [%s]",objectMapper.writeValueAsString(extractInterestExchangeRateRequest)));

		ResponseEntity<String> responseEntity = restTemplate.exchange(restAPILink, HttpMethod.POST, requestEntity, String.class);
		logger.info(String.format("Request EAI Exchange rate [%s], response code[%s], response body %s",batchUpdateMcaInterestRate.getCode(), responseEntity.getStatusCode(), responseEntity.getBody()));
		return JsonUtils.jsonToObject(responseEntity.getBody(), EAIInterestRateResponse.class);
	}

	public static ResponseEntity<String> updateEpullEnrollment(String userId,
																EPullAutoEnrollmentRequest ePullAutoEnrollmentRequest,
																String restAPILink,
																String sessionToken,
																RestTemplate restTemplate) throws JsonProcessingException , SocketTimeoutException {
		HttpHeaders header = buildCommonHeader(sessionToken);
		header.set("userProfileId", userId);
		logger.info("userProfileId : " +  userId);
		ObjectMapper objectMapper=new ObjectMapper();

		HttpEntity<String> requestEntity = new HttpEntity<String>(objectMapper.writeValueAsString(ePullAutoEnrollmentRequest), header);
		logger.info(String.format("restAPI body [%s]",objectMapper.writeValueAsString(ePullAutoEnrollmentRequest)));
		ResponseEntity<String> responseEntity;
		logger.info("URL :" + restAPILink);
		responseEntity = restTemplate.exchange(restAPILink, HttpMethod.POST, requestEntity, String.class);
		logger.info("Response :" + responseEntity.getBody());
		return responseEntity;
	}

	private static HttpHeaders buildCommonHeader(String sessionToken) {
		HttpHeaders header = new HttpHeaders();
		header.set(CONTENT_TYPE, APPLICATION_CONTENT);
		header.set(AUTHORIZATION, BEARER + sessionToken);
		return header;
	}
}
