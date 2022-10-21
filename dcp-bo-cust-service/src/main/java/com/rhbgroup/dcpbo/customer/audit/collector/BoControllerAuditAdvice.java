package com.rhbgroup.dcpbo.customer.audit.collector;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rhbgroup.dcpbo.customer.contract.BoData;

@Aspect
public class BoControllerAuditAdvice {
	private final static String DEFAULT_SUCCESS_CODE = "10000";
	private final static String DEFAULT_ERROR_CODE = "80000";
	private final static String DEFAULT_ERROR_DESCRIPTION = "Failure";
	private final static String DEFAULT_SUCCESS_DESCRIPTION = "Success";

	private final Logger log = LogManager.getLogger(BoControllerAuditAdvice.class);
	private BoAuditLogQueue boAuditLogger;
	private HttpServletRequest request;
	private AuditAdditionalDataFactory auditAdditionalDataFactory;
	private Class returnType;

	@Autowired
    ConfigErrorInterface configErrorInterface;

	public BoControllerAuditAdvice(BoAuditLogQueue boAuditLogger,
			AuditAdditionalDataFactory auditAdditionalDataFactory) {
		this.boAuditLogger = boAuditLogger;
		this.auditAdditionalDataFactory = auditAdditionalDataFactory;
	}

	@Around("@annotation(BoControllerAudit)")
	public Object messageAuditQueue(ProceedingJoinPoint joinPoint) throws Throwable{

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String eventCode = method.getAnnotation(BoControllerAudit.class).eventCode();
        request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        String[] argsNames = codeSignature.getParameterNames();
        Object[] argsValues = joinPoint.getArgs();
        returnType = ((MethodSignature) codeSignature).getReturnType();
        Object controllerOutput = joinPoint.proceed();
        String auditAdditionalDataRetriever = method.getAnnotation(BoControllerAudit.class).value();
        String jsonString = jsonify(eventCode, argsNames, argsValues, controllerOutput, auditAdditionalDataRetriever);
        log.debug(String.format("Audit payload : %s", jsonString));
        boAuditLogger.send(jsonString);

        return controllerOutput;
	}

	@AfterThrowing(value = "@annotation(BoControllerAudit)", throwing = "e")
	public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
	    try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();
            String eventCode = method.getAnnotation(BoControllerAudit.class).eventCode();
            request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
            String[] argsNames = codeSignature.getParameterNames();
            Object[] argsValues = joinPoint.getArgs();

            String jsonString = jsonify(eventCode, argsNames, argsValues, e, "");
            log.error(String.format("Error audit payload : %s", jsonString));
            boAuditLogger.send(jsonString);
        } catch (Exception ex){
	        log.error(ex.getMessage());
	    }
	}

	private String jsonify(String eventCode, String[] argsNames, Object[] argsValues, Object controllerOutput,
			String auditAdditionalDataRetrieverName) {
		Map<String, Object> jsonObj = new HashMap<>();
		Map<String, Object> requestJsonObj = new HashMap<>();
		Map <String, Object> responseBody = new HashMap<>();
        List<HashMap<String, Object>> parameters = new ArrayList<>();
        OffsetDateTime timestamp = OffsetDateTime.now();
		jsonObj.put("eventCode", eventCode);
		jsonObj.put("url", request.getRequestURL().toString());
		jsonObj.put("timestamp", timestamp.toString());
		String ipVal = request.getHeader("X-Forwarded-For");
		if(StringUtils.isNotEmpty(ipVal)) {
			String clientIp = ipVal.split(", ")[0];
			jsonObj.put("ip", clientIp);
		}
		for(int i=0; i<argsNames.length; i++) {
            HashMap<String, Object> parameter = new HashMap<>();
            parameter.put("name", argsNames[i]);
            parameter.put("value", argsValues[i]);
            parameters.add(parameter);
        }
		requestJsonObj.put("parameters", parameters);
        Map<String, String> requestHeaderMap = new HashMap<String, String>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            requestHeaderMap.put(key, value);
        }
        requestJsonObj.put("headers", requestHeaderMap);

        jsonObj.put("userId", requestHeaderMap.get("userid"));
		jsonObj.put("request", requestJsonObj);

		if (controllerOutput instanceof BoData) {
			jsonObj.put("status", DEFAULT_SUCCESS_CODE);
			jsonObj.put("statusDescription", DEFAULT_SUCCESS_DESCRIPTION);
			BoData boData = (BoData) controllerOutput;
			responseBody.put("body", boData);
			jsonObj.put("response", responseBody);
		} else if (controllerOutput instanceof Collection) {
			jsonObj.put("status", DEFAULT_SUCCESS_CODE);
			jsonObj.put("statusDescription", DEFAULT_SUCCESS_DESCRIPTION);
			ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode arrayNode = objectMapper.valueToTree(controllerOutput);
            responseBody.put("body", arrayNode);
            jsonObj.put("response", responseBody);
		} else if (controllerOutput instanceof Throwable) {
			if (controllerOutput instanceof BoException) {
				BoException exception = (BoException) controllerOutput;

                try{
                    BoExceptionResponse boExceptionResponse = configErrorInterface.getConfigError(exception.getErrorCode());
                    exception.setErrorDesc(boExceptionResponse.getErrorDesc());
                } catch (Exception ex) {
				    log.error(ex.getMessage());

                    exception.setErrorCode(DEFAULT_ERROR_CODE);
                    exception.setErrorDesc(DEFAULT_ERROR_DESCRIPTION);
                    exception.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                }

				jsonObj.put("status", exception.getErrorCode());
				jsonObj.put("statusDescription", exception.getErrorDesc());
				responseBody.put("body", exception.toMap());
				jsonObj.put("response", responseBody);
			} else {
				// All other throwable to be defaulted
				jsonObj.put("status", DEFAULT_ERROR_CODE);
				jsonObj.put("statusDescription", DEFAULT_ERROR_DESCRIPTION);
				Throwable e = (Throwable) controllerOutput;
				ObjectMapper objectMapper = new ObjectMapper();
				ObjectNode node = objectMapper.createObjectNode();
				node.put("timestamp", timestamp.toInstant().toEpochMilli());
				node.put("status", DEFAULT_ERROR_CODE);
				node.put("error", DEFAULT_ERROR_DESCRIPTION);
				node.put("exception", e.getClass().toString());
				if(e.getMessage() != null) {
					node.put("message", e.getMessage());
				}
				responseBody.put("body", node);
				jsonObj.put("response", responseBody);
			}
		} else if(!returnType.toGenericString().equals("void")) {
			jsonObj.put("status", DEFAULT_SUCCESS_CODE);
			jsonObj.put("statusDescription", DEFAULT_SUCCESS_DESCRIPTION);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode response = objectMapper.valueToTree(controllerOutput);
			jsonObj.put("response", response);
			log.warn(String.format("Invalid controller return type", controllerOutput.getClass()));
		} else {
			jsonObj.put("status", DEFAULT_SUCCESS_CODE);
			jsonObj.put("statusDescription", DEFAULT_SUCCESS_DESCRIPTION);
			jsonObj.put("response", new HashMap<>());
			log.warn("Controller returns void");
		}

		if (StringUtils.isEmpty(auditAdditionalDataRetrieverName)) {
			jsonObj.put("additionalData", new HashMap<>());
		} else {
			AuditAdditionalDataRetriever auditAdditionalDataRetriever = auditAdditionalDataFactory
					.getService(auditAdditionalDataRetrieverName);
			jsonObj.put("additionalData", auditAdditionalDataRetriever.retrieve());
		}

		String jsonString;
		try {
			jsonString = new ObjectMapper().writeValueAsString(jsonObj);
		} catch (JsonProcessingException e) {
			log.error("{}", e);
			jsonString = "";
		}
		return jsonString;
	}
}
