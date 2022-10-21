package com.rhbgroup.dcp.bo.batch.framework.utils;

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

import com.rhbgroup.dcp.bo.batch.framework.config.properties.BoLoginConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.RestTemplateConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.ExtractAndUpdateExchangeRateJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.model.EAIExchangeRateResponse;
import com.rhbgroup.dcp.bo.batch.job.model.EAIFpxStatusResponse;
import com.rhbgroup.dcp.bo.batch.job.model.EAIInterestRateResponse;
import com.rhbgroup.dcp.bo.batch.job.model.EAISessionTokenResponse;

public final class FPXUtils {
	private static final Logger logger = Logger.getLogger(FPXUtils.class);
	
	@Autowired
	private static HttpComponentsClientHttpRequestFactory clientRequestFactory;
	
	private FPXUtils() {
		throw new IllegalStateException("Utility Class");
	}
	
	public static EAISessionTokenResponse getSessionToken(String username, String password, String restAPILogin, RestTemplate restTemplate) throws JSONException {
		
		// Header
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		// Body
		JSONObject requestBody = new JSONObject();
		requestBody.put("username", username);
		requestBody.put("password", password);
		
		// Request
		HttpEntity<String> requestEntity = new HttpEntity<String>(requestBody.toString(), header);
		
		// Response
		ResponseEntity<String> responseEntity = restTemplate.exchange(restAPILogin, HttpMethod.POST, requestEntity, String.class);
		
		logger.info(String.format("Request EAI Login response code [%s]", responseEntity.getStatusCode()));
		return JsonUtils.jsonToObject(responseEntity.getBody(), EAISessionTokenResponse.class);
	}
	
	public static EAIFpxStatusResponse getEAIFPXStatus(long txnToken,String txnStatus,String restAPILink, String sessionToken, RestTemplate restTemplate) {
		// Header
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		header.set("Authorization", "Bearer " + sessionToken);
		
		// Body
		JSONObject requestBody = new JSONObject();
		requestBody.put("txnToken", txnToken);
		requestBody.put("txnStatus", txnStatus);
		
		// Request
		HttpEntity<String> requestEntity = new HttpEntity<String>(requestBody.toString(), header);
		
		// Response
		ResponseEntity<String> responseEntity = restTemplate.exchange(restAPILink, HttpMethod.POST, requestEntity, String.class);
		logger.info(String.format("Request EAI Exchange rate response code[%s], response body %s",responseEntity.getStatusCode(), responseEntity.getBody()));
		return JsonUtils.jsonToObject(responseEntity.getBody(), EAIFpxStatusResponse.class);
	}

}
