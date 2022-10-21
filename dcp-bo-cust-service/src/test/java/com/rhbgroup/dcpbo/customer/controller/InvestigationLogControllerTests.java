package com.rhbgroup.dcpbo.customer.controller;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.dto.AuditPagination;
import com.rhbgroup.dcpbo.customer.dto.AuditType;
import com.rhbgroup.dcpbo.customer.dto.OperationName;
import com.rhbgroup.dcpbo.customer.dto.Telemetry;
import com.rhbgroup.dcpbo.customer.dto.TelemetryData;
import com.rhbgroup.dcpbo.customer.dto.TelemetryErrorLog;
import com.rhbgroup.dcpbo.customer.dto.TelemetryErrorLogs;
import com.rhbgroup.dcpbo.customer.dto.TelemetryLogPayloadData;
import com.rhbgroup.dcpbo.customer.dto.TelemetryLogPayloadDetails;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.customer.model.DcpTelemetryErrorLog;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import com.rhbgroup.dcpbo.customer.service.DcpTelemetryErrorLogService;
import com.rhbgroup.dcpbo.customer.service.InvestigationLogService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { InvestigationLogControllerTests.class, InvestigationLogControllerTests.Config.class,
		InvestigationLogController.class })
@EnableWebMvc
public class InvestigationLogControllerTests {
	
	public static final int PAGE_SIZE = 15;
	
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	InvestigationLogService investigationLogServiceMock;
	
	@MockBean
	DcpTelemetryErrorLogService dcpTelemetryErrorLogService;
	
	@TestConfiguration
	static class Config {
		@Bean
		public CommonException getCommonException() {
			return new CommonException(CommonException.GENERIC_ERROR_CODE);
		}

		@Bean
		public CommonExceptionAdvice getCommonExceptionAdvice() {
			return new CommonExceptionAdvice();
		}
		
		@Bean
		public ConfigErrorInterface getConfigErrorInterface() {
			return new ConfigInterfaceImpl();
		}
	
		class ConfigInterfaceImpl implements ConfigErrorInterface {
			@Override
			public BoExceptionResponse getConfigError(String errorCode) {
				return new BoExceptionResponse(errorCode, "Invalid date format for fromDate");
			}
		}
	}

	private static Logger logger = LogManager.getLogger(InvestigationLogControllerTests.class);

	@Test
	public void getOperationNameTest() throws Exception {
		logger.debug("getOperationNameTest()");
		logger.debug("    investigationLogServiceMock: " + investigationLogServiceMock);
		
		ArrayList<String> operationNames = new ArrayList<String>( 
	            Arrays.asList("GetAsbDashboard", 
	                          "GetAsbDetails", 
	                          "GetAsbTransactions",
	                          "GetCreditCardDetails",
	                          "GetCreditCardTransactions")); 
		OperationName operationName = new OperationName();
		operationName.setOperationName(operationNames);
		when(investigationLogServiceMock.getOperationNames()).thenReturn(operationName);

		String url = "/bo/investigation/telemetry/operationName";
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.jsonPath("operationName[0]", Matchers.is("GetAsbDashboard")))
				.andExpect(MockMvcResultMatchers.jsonPath("operationName[1]", Matchers.is("GetAsbDetails")))
				.andExpect(MockMvcResultMatchers.jsonPath("operationName[2]", Matchers.is("GetAsbTransactions")))
				.andExpect(MockMvcResultMatchers.jsonPath("operationName[3]", Matchers.is("GetCreditCardDetails")))
				.andExpect(MockMvcResultMatchers.jsonPath("operationName[4]", Matchers.is("GetCreditCardTransactions")));
	}

	@Test
	public void getAuditTypeTest() throws Exception {
		logger.debug("getAuditTypeTest()");
		logger.debug("    investigationLogServiceMock: " + investigationLogServiceMock);
		
		ArrayList<String> auditTypes = new ArrayList<String>( 
	            Arrays.asList("RQI", 
	                          "Adaptorin", 
	                          "Adaptorout",
	                          "WS_RES (additional)",
	                          "WS_REQ (additional)")); 
		AuditType auditType = new AuditType();
		auditType.setAuditType(auditTypes);
		when(investigationLogServiceMock.getAuditTypes()).thenReturn(auditType);

		String url = "/bo/investigation/telemetry/auditType";
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.jsonPath("auditType[0]", Matchers.is("RQI")))
				.andExpect(MockMvcResultMatchers.jsonPath("auditType[1]", Matchers.is("Adaptorin")))
				.andExpect(MockMvcResultMatchers.jsonPath("auditType[2]", Matchers.is("Adaptorout")))
				.andExpect(MockMvcResultMatchers.jsonPath("auditType[3]", Matchers.is("WS_RES (additional)")))
				.andExpect(MockMvcResultMatchers.jsonPath("auditType[4]", Matchers.is("WS_REQ (additional)")));
	}
	
	@Test
	public void getNewLogs() throws Exception {
		logger.debug("getNewLogs()");
		logger.debug("    investigationLogServiceMock: " + investigationLogServiceMock);
		
		List<Telemetry> telemetries = new ArrayList<Telemetry>();
		
		Telemetry telemetry = new Telemetry();
		telemetry.setMessageId("2f738ceb-733f-4f94-ac84-c9c4da2a3b54");
		telemetry.setAuditType("RPO");
		telemetry.setAuditDateTime("2019-01-29 21:29:25.940");
		telemetry.setOperationName("GetLifestyleOffers");
		telemetry.setUsername("james");
		telemetry.setTotalError("3");
		telemetries.add(telemetry);
		
		telemetry = new Telemetry();
		telemetry.setMessageId("dd918156-4a6c-4bfc-86d8-32398be3a4b4");
		telemetry.setAuditType("TCPIP_REQ");
		telemetry.setAuditDateTime("2019-04-24 17:22:11.753");
		telemetry.setOperationName("GetSecretPhrase");
		telemetry.setTotalError("1");
		telemetries.add(telemetry);
		
		TelemetryData data = new TelemetryData();
		data.setData(telemetries);
		when(investigationLogServiceMock.getNewLogs()).thenReturn(data);

		String url = "/bo/investigation/telemetry/newLogs";
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
		.andDo(MockMvcResultHandlers.print())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
		.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].messageId", Matchers.is("2f738ceb-733f-4f94-ac84-c9c4da2a3b54")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].operationName", Matchers.is("GetLifestyleOffers")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].auditType", Matchers.is("RPO")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].username", Matchers.is("james")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].auditDateTime", Matchers.is("2019-01-29 21:29:25.940")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].totalError", Matchers.is("3")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].messageId", Matchers.is("dd918156-4a6c-4bfc-86d8-32398be3a4b4")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].operationName", Matchers.is("GetSecretPhrase")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].auditType", Matchers.is("TCPIP_REQ")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].auditDateTime", Matchers.is("2019-04-24 17:22:11.753")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].username", Matchers.isEmptyOrNullString()))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].totalError", Matchers.is("1")));

	}	

	public void testGetErrorSuccess() throws Exception {
		logger.debug("testGetErrorSuccess()");
		logger.debug("    dcpTelemetryErrorLogService: " + dcpTelemetryErrorLogService);
		
		Integer pageNum = 1;
		String keyword = "ERROR_CODE";
		String fromDate = "2019-04-18T00:00:00+08:00";
		String toDate = "2019-04-19T00:00:00+08:00";
		
		List<DcpTelemetryErrorLog> mockDcpTelemetryErrorLogs = new ArrayList<>();
		for(int i = 0; i < 10; i++) {
			DcpTelemetryErrorLog mockTelemetryErrorLog = new DcpTelemetryErrorLog();
			
			int count = 10 + i;
			String mockAuditDateTime = "2019-04-18T" + Integer.toString(count) + ":15:30.00Z";
			Instant instant = Instant.parse(mockAuditDateTime);
			Timestamp timestamp = Timestamp.from(instant);
			mockTelemetryErrorLog.setAuditDateTime(timestamp);
			
			mockTelemetryErrorLog.setErrorCode("ERROR_CODE_" + Integer.toString(i));
			mockTelemetryErrorLog.setErrorDetails("ERROR_DETAILS_" + Integer.toString(i));
			mockTelemetryErrorLog.setErrorReason("ERROR_REASON_" + Integer.toString(i));
			mockTelemetryErrorLog.setErrorSource("ERROR_SOURCE_" + Integer.toString(i));
			mockTelemetryErrorLog.setMessageId(Integer.toString(i));
			mockTelemetryErrorLog.setOperationName("OPERATION_NAME_" + Integer.toString(i));
			mockDcpTelemetryErrorLogs.add(mockTelemetryErrorLog);
		}
		
		TelemetryErrorLogs telemetryErrorLogs = new TelemetryErrorLogs();
		for(DcpTelemetryErrorLog mockDcpTelemetryErrorLog : mockDcpTelemetryErrorLogs) {
			TelemetryErrorLog telemetryErrorLog = new TelemetryErrorLog();
			telemetryErrorLog.setAuditDateTime(mockDcpTelemetryErrorLog.getAuditDateTime().toString());
			telemetryErrorLog.setErrorCode(mockDcpTelemetryErrorLog.getErrorCode());
			telemetryErrorLog.setErrorDetails(mockDcpTelemetryErrorLog.getErrorDetails());
			telemetryErrorLog.setErrorReason(mockDcpTelemetryErrorLog.getErrorReason());
			telemetryErrorLog.setErrorSource(mockDcpTelemetryErrorLog.getErrorSource());
			telemetryErrorLog.setMessageId(mockDcpTelemetryErrorLog.getMessageId());
			telemetryErrorLog.setOperationName(mockDcpTelemetryErrorLog.getOperationName());
			
			telemetryErrorLogs.addTelemetryErrorLog(telemetryErrorLog);
		}
		
		AuditPagination pagination = new AuditPagination();
		pagination.setPageIndicator("L");
		pagination.setActivityCount(mockDcpTelemetryErrorLogs.size());
		pagination.setPageNum(pageNum);
		Integer totalPageNum = (int) Math.ceil(Double.parseDouble(Long.toString(mockDcpTelemetryErrorLogs.size())) / PAGE_SIZE);
		pagination.setTotalPageNum(totalPageNum);
		
		telemetryErrorLogs.setPagination(pagination);
		
		when(dcpTelemetryErrorLogService.listing(keyword, fromDate, toDate, pageNum)).thenReturn(telemetryErrorLogs);
		
		String url = "/bo/investigation/telemetry/error?keyword=" + keyword + "&pageNum=" + Integer.toString(pageNum) + "&fromDate=" + fromDate + "&toDate=" + toDate;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetErrorFailBothDateEmpty() throws Exception {
		logger.debug("testGetErrorFailBothDateEmpty()");
		logger.debug("    dcpTelemetryErrorLogService: " + dcpTelemetryErrorLogService);
		
		Integer pageNum = 1;
		String keyword = "NullPointerException";
		String fromDate = "";
		String toDate = "";
		
		when(dcpTelemetryErrorLogService.listing(keyword, fromDate, toDate, pageNum)).thenThrow(DateTimeParseException.class);
		
		String url = "/bo/investigation/telemetry/error?keyword=" + keyword + "&pageNum=" + Integer.toString(pageNum) + "&fromDate=" + fromDate + "&toDate=" + toDate;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is4xxClientError());
	}
	
	@Test
	public void getLogsWithAllParametersSuccess() throws Exception {
		logger.debug("getLogsWithAllParametersSuccess()");
		logger.debug("    investigationLogServiceMock: " + investigationLogServiceMock);
		
		Integer pageNum = 1;
		String auditType = "RPO";
		String keyword = "Offers";
		String fromDate = "2019-04-18T00:00:00+08:00";
		String toDate = "2019-04-19T00:00:00+08:00";
		
		List<Telemetry> telemetries = new ArrayList<Telemetry>();
		
		Telemetry telemetry = new Telemetry();
		telemetry.setMessageId("2f738ceb-733f-4f94-ac84-c9c4da2a3b54");
		telemetry.setAuditType("RPO");
		telemetry.setAuditDateTime("2019-01-29 21:29:25.940");
		telemetry.setOperationName("GetLifestyleOffers");
		telemetry.setUsername("james");
		telemetry.setTotalError("3");
		telemetries.add(telemetry);
		
		telemetry = new Telemetry();
		telemetry.setMessageId("dd918156-4a6c-4bfc-86d8-32398be3a4b4");
		telemetry.setAuditType("TCPIP_REQ");
		telemetry.setAuditDateTime("2019-04-24 17:22:11.753");
		telemetry.setOperationName("GetSecretPhrase");
		telemetry.setTotalError("1");
		telemetries.add(telemetry);
		
		TelemetryData data = new TelemetryData();
		data.setData(telemetries);
		
		AuditPagination pagination = new AuditPagination();
		pagination.setPageIndicator("L");
		pagination.setActivityCount(telemetries.size());
		pagination.setPageNum(pageNum);
		Integer totalPageNum = (int) Math.ceil(Double.parseDouble(Long.toString(telemetries.size())) / PAGE_SIZE);
		pagination.setTotalPageNum(totalPageNum);
		
		when(investigationLogServiceMock.getLogs(auditType, keyword, pageNum, fromDate, toDate)).thenReturn(data);

		String url = "/bo/investigation/telemetry?auditType=" + auditType + "&keyword=" + keyword + "&pageNum=" + Integer.toString(pageNum) + "&fromDate=" + fromDate + "&toDate=" + toDate;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
		.andDo(MockMvcResultHandlers.print())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
		.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].messageId", Matchers.is("2f738ceb-733f-4f94-ac84-c9c4da2a3b54")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].operationName", Matchers.is("GetLifestyleOffers")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].auditType", Matchers.is("RPO")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].username", Matchers.is("james")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].auditDateTime", Matchers.is("2019-01-29 21:29:25.940")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].totalError", Matchers.is("3")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].messageId", Matchers.is("dd918156-4a6c-4bfc-86d8-32398be3a4b4")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].operationName", Matchers.is("GetSecretPhrase")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].auditType", Matchers.is("TCPIP_REQ")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].auditDateTime", Matchers.is("2019-04-24 17:22:11.753")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].username", Matchers.isEmptyOrNullString()))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].totalError", Matchers.is("1")));

	}	
	
	@Test
	public void getLogsWithoutAuditTypeSuccess() throws Exception {
		logger.debug("getLogsWithoutAuditTypeSuccess()");
		logger.debug("    investigationLogServiceMock: " + investigationLogServiceMock);
		
		Integer pageNum = 1;
		String auditType = "ALL";
		String keyword = "Offers";
		String fromDate = "2019-04-18T00:00:00+08:00";
		String toDate = "2019-04-19T00:00:00+08:00";
		
		List<Telemetry> telemetries = new ArrayList<Telemetry>();
		
		Telemetry telemetry = new Telemetry();
		telemetry.setMessageId("2f738ceb-733f-4f94-ac84-c9c4da2a3b54");
		telemetry.setAuditType("RPO");
		telemetry.setAuditDateTime("2019-01-29 21:29:25.940");
		telemetry.setOperationName("GetLifestyleOffers");
		telemetry.setUsername("james");
		telemetry.setTotalError("3");
		telemetries.add(telemetry);
		
		telemetry = new Telemetry();
		telemetry.setMessageId("dd918156-4a6c-4bfc-86d8-32398be3a4b4");
		telemetry.setAuditType("TCPIP_REQ");
		telemetry.setAuditDateTime("2019-04-24 17:22:11.753");
		telemetry.setOperationName("GetSecretPhrase");
		telemetry.setTotalError("1");
		telemetries.add(telemetry);
		
		TelemetryData data = new TelemetryData();
		data.setData(telemetries);
		
		AuditPagination pagination = new AuditPagination();
		pagination.setPageIndicator("L");
		pagination.setActivityCount(telemetries.size());
		pagination.setPageNum(pageNum);
		Integer totalPageNum = (int) Math.ceil(Double.parseDouble(Long.toString(telemetries.size())) / PAGE_SIZE);
		pagination.setTotalPageNum(totalPageNum);
		
		when(investigationLogServiceMock.getLogs(auditType, keyword, pageNum, fromDate, toDate)).thenReturn(data);

		String url = "/bo/investigation/telemetry?keyword=" + keyword + "&pageNum=" + Integer.toString(pageNum) + "&fromDate=" + fromDate + "&toDate=" + toDate;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
		.andDo(MockMvcResultHandlers.print())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
		.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].messageId", Matchers.is("2f738ceb-733f-4f94-ac84-c9c4da2a3b54")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].operationName", Matchers.is("GetLifestyleOffers")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].auditType", Matchers.is("RPO")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].username", Matchers.is("james")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].auditDateTime", Matchers.is("2019-01-29 21:29:25.940")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].totalError", Matchers.is("3")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].messageId", Matchers.is("dd918156-4a6c-4bfc-86d8-32398be3a4b4")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].operationName", Matchers.is("GetSecretPhrase")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].auditType", Matchers.is("TCPIP_REQ")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].auditDateTime", Matchers.is("2019-04-24 17:22:11.753")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].username", Matchers.isEmptyOrNullString()))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].totalError", Matchers.is("1")));

	}
	
	@Test
	public void getLogsWithoutOptionalParametersSuccess() throws Exception {
		logger.debug("getLogsWithoutOptionalParametersSuccess()");
		logger.debug("    investigationLogServiceMock: " + investigationLogServiceMock);
		
		Integer pageNum = 1;
		String auditType = "ALL";
		String keyword = "Offers";
		String fromDate = "";
		String toDate = "";
		
		List<Telemetry> telemetries = new ArrayList<Telemetry>();
		
		Telemetry telemetry = new Telemetry();
		telemetry.setMessageId("2f738ceb-733f-4f94-ac84-c9c4da2a3b54");
		telemetry.setAuditType("RPO");
		telemetry.setAuditDateTime("2019-01-29 21:29:25.940");
		telemetry.setOperationName("GetLifestyleOffers");
		telemetry.setUsername("james");
		telemetry.setTotalError("3");
		telemetries.add(telemetry);
		
		telemetry = new Telemetry();
		telemetry.setMessageId("dd918156-4a6c-4bfc-86d8-32398be3a4b4");
		telemetry.setAuditType("TCPIP_REQ");
		telemetry.setAuditDateTime("2019-04-24 17:22:11.753");
		telemetry.setOperationName("GetSecretPhrase");
		telemetry.setTotalError("1");
		telemetries.add(telemetry);
		
		TelemetryData data = new TelemetryData();
		data.setData(telemetries);
		
		AuditPagination pagination = new AuditPagination();
		pagination.setPageIndicator("L");
		pagination.setActivityCount(telemetries.size());
		pagination.setPageNum(pageNum);
		Integer totalPageNum = (int) Math.ceil(Double.parseDouble(Long.toString(telemetries.size())) / PAGE_SIZE);
		pagination.setTotalPageNum(totalPageNum);
		
		when(investigationLogServiceMock.getLogs(auditType, keyword, pageNum, fromDate, toDate)).thenReturn(data);

		String url = "/bo/investigation/telemetry?keyword=" + keyword + "&pageNum=" + Integer.toString(pageNum) + "&fromDate=" + fromDate + "&toDate=" + toDate;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
		.andDo(MockMvcResultHandlers.print())
		.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
		.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].messageId", Matchers.is("2f738ceb-733f-4f94-ac84-c9c4da2a3b54")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].operationName", Matchers.is("GetLifestyleOffers")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].auditType", Matchers.is("RPO")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].username", Matchers.is("james")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].auditDateTime", Matchers.is("2019-01-29 21:29:25.940")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[0].totalError", Matchers.is("3")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].messageId", Matchers.is("dd918156-4a6c-4bfc-86d8-32398be3a4b4")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].operationName", Matchers.is("GetSecretPhrase")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].auditType", Matchers.is("TCPIP_REQ")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].auditDateTime", Matchers.is("2019-04-24 17:22:11.753")))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].username", Matchers.isEmptyOrNullString()))
		.andExpect(MockMvcResultMatchers.jsonPath("data[1].totalError", Matchers.is("1")));

	}
	
	@Test
	public void getLogsMissingMandatoryParametersFail() throws Exception {
		logger.debug("getLogsMissingMandatoryParametersFail()");
		logger.debug("    investigationLogServiceMock: " + investigationLogServiceMock);
		
		Integer pageNum = 1;
		String fromDate = "";
		String toDate = "";

		String url = "/bo/investigation/telemetry?pageNum=" + Integer.toString(pageNum) + "&fromDate=" + fromDate + "&toDate=" + toDate;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
		.andDo(MockMvcResultHandlers.print())
		.andExpect(MockMvcResultMatchers.status().reason(containsString("Required String parameter 'keyword' is not present")))
		.andExpect(MockMvcResultMatchers.status().isBadRequest());
		
	}
	
	@Test
	public void getLogsInvalidDateFormatFail() throws Exception {
		logger.debug("getLogsMissingMandatoryParametersFail()");
		logger.debug("    investigationLogServiceMock: " + investigationLogServiceMock);
		
		String auditType = "ALL";
		String keyword = "Offers";
		Integer pageNum = 1;
		String fromDate = "dfdfdff";
		String toDate = "";
		
		CommonException ce = new CommonException(CommonException.GENERIC_ERROR_CODE,"Invalid date format for fromDate");
		when(investigationLogServiceMock.getLogs(auditType, keyword, pageNum, fromDate, toDate)).thenThrow(ce);

		String url = "/bo/investigation/telemetry?keyword=" + keyword + "&pageNum=" + Integer.toString(pageNum) + "&fromDate=" + fromDate + "&toDate=" + toDate;
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
		.andDo(MockMvcResultHandlers.print())
		.andExpect(MockMvcResultMatchers.jsonPath("errorCode", Matchers.is("80000")))
		.andExpect(MockMvcResultMatchers.jsonPath("errorDesc", Matchers.is("Invalid date format for fromDate")))
		.andExpect(MockMvcResultMatchers.status().is5xxServerError());
	}
		
	@SuppressWarnings("unchecked")
	@Test
	public void testGetErrorFailBothDateNull() throws Exception {
		logger.debug("testGetErrorFailBothDateNull()");
		logger.debug("    dcpTelemetryErrorLogService: " + dcpTelemetryErrorLogService);
		
		Integer pageNum = 1;
		String keyword = "NullPointerException";
		
		when(dcpTelemetryErrorLogService.listing(keyword, null, null, pageNum)).thenThrow(NullPointerException.class);
		
		String url = "/bo/investigation/telemetry/error?keyword=" + keyword + "&pageNum=" + Integer.toString(pageNum);
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
	
	@Test
	public void testGetErrorDetailsSuccess() throws Exception {
		logger.debug("testGetErrorDetailsSuccess()");
		logger.debug("    dcpTelemetryErrorLogService: " + dcpTelemetryErrorLogService);
		
		String messageId = "01e4abd2-f3c1-4668-95af-d1a24be853c7";
		String auditDateTime = "2018-12-06T17:54:23+08:00";
		
		TelemetryErrorLog telemetryErrorLog = new TelemetryErrorLog();
		telemetryErrorLog.setAuditDateTime(auditDateTime);
		telemetryErrorLog.setMessageId(messageId);
		
		when(dcpTelemetryErrorLogService.getTelemetryErrorLogDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(telemetryErrorLog);
		
		String url = "/bo/investigation/telemetry/" + messageId + "/" + auditDateTime + "/errorDetails";
		logger.debug("    url: " + url);
		
		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
	
	@Test
	public void testGetErrorDetailsFailNull() throws Exception {
		logger.debug("testGetErrorDetailsFailNull()");
		logger.debug("    dcpTelemetryErrorLogService: " + dcpTelemetryErrorLogService);
		
		String messageId = "01e4abd2-f3c1-4668-95af-d1a24be853c7";
		String auditDateTime = "2018-12-06T17:54:23+08:00";
		
		when(dcpTelemetryErrorLogService.getTelemetryErrorLogDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(null);
		
		String url = "/bo/investigation/telemetry/" + messageId + "/" + auditDateTime + "/errorDetails";
		logger.debug("    url: " + url);
		
		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}

	@Test
	public void getTelemetryPayloadData() throws Exception {
		logger.debug("getTelemetryPayloadData()");
		logger.debug("    investigationLogServiceMock: " + investigationLogServiceMock);
		
		String messageId = "2342342352352343";
		String auditDateTime = "2019-04-19T00:00:00%2B08:00";
		String payload = "{\"status\":{\"code\":\"80000\",\"statusType\":\"system_error\",\"description\":\"There seems to be a slight issue. Please try again later.\",\"title\":\"Oops!\"},\"data\":{}}";
		String auditParam = "{quick_login=false, op_status=false, referenceId=null, op_status_code=80000}";
		String cisNumber = null;
		String deviceId = "a05d47e9-76fe-4454-9781-399e032de6b2";
		String hostname = "oban";

		TelemetryLogPayloadDetails telemetryPayloadDetails = new TelemetryLogPayloadDetails();
		telemetryPayloadDetails.setPayload(payload);
		telemetryPayloadDetails.setAuditDateTime(auditDateTime);
		telemetryPayloadDetails.setAuditParam(auditParam);
		telemetryPayloadDetails.setCisNumber(cisNumber);
		telemetryPayloadDetails.setDeviceId(deviceId);
		telemetryPayloadDetails.setHostname(hostname);
		
		TelemetryLogPayloadData telemetryPayload = new TelemetryLogPayloadData();
		telemetryPayload.setData(telemetryPayloadDetails);

		when(investigationLogServiceMock.getTelemetryPayloadData(messageId, auditDateTime)).thenReturn(telemetryPayload);
		
		String url = "/bo/investigation/telemetry/" + messageId + "/" + auditDateTime + "/payload";
		logger.debug("    url: " + url);

		mockMvc.perform(MockMvcRequestBuilders.get(url))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.payload", Matchers.is(payload)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.auditDateTime", Matchers.is(auditDateTime)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.auditParam", Matchers.is(auditParam)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.cisNumber", Matchers.is(cisNumber)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.deviceId", Matchers.is(deviceId)))
				.andExpect(MockMvcResultMatchers.jsonPath("$.data.hostname", Matchers.is(hostname)));
	}

}
