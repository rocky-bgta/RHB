package com.rhbgroup.dcpbo.system.downtime.whitelist.controller;

import static org.mockito.Mockito.when;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.common.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.common.exception.ConfigErrorInterface;
import com.rhbgroup.dcpbo.common.exception.ErrorResponse;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.DowntimeAdhocWhitelistService;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.DeleteDowntimeAdhocWhitelistRequest;
import com.rhbgroup.dcpbo.system.downtime.whitelist.vo.DowntimeAdhocWhitelistResponse;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;
import com.rhbgroup.dcpbo.system.exception.PendingApprovalException;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { DeleteDowntimeAdhocWhitelistControllerTest.class, DowntimeAdhocWhitelistController.class })
@EnableWebMvc
public class DeleteDowntimeAdhocWhitelistControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	DowntimeAdhocWhitelistService downtimeAdhocWhitelistServiceMock;

	@TestConfiguration
	static class Config {
		@Bean
		public CommonException getCommonException() {
			return new CommonException(CommonException.GENERIC_ERROR_CODE, "NA");
		}

		@Bean
		public DowntimeExceptionAdvice getDowntimeExceptionAdvice() {
			return new DowntimeExceptionAdvice();
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
			public ErrorResponse getConfigError(String errorCode) {
				return new ErrorResponse(errorCode, "Invalid date format");
			}
		}
	}

	private DeleteDowntimeAdhocWhitelistRequest request;

	@Before
	public void setup() {

		request = new DeleteDowntimeAdhocWhitelistRequest();
		request.setFunctionId(16);

	}

	@Test
	public void deleteDowntimeAdhocWhitelistRequireApprovalSuccessTest() throws Exception {
		log.debug("deleteDowntimeAdhocWhitelistRequireApprovalSuccessTest()");

		String url = "/bo/system/downtime/adhoc/whitelist/10";
		log.debug("url: {}", url);

		DowntimeAdhocWhitelistResponse response = new DowntimeAdhocWhitelistResponse();
		response.setApprovalId(1);
		ResponseEntity<BoData> responseEntity = new ResponseEntity<BoData>(response, HttpStatus.OK);

		when(downtimeAdhocWhitelistServiceMock.deleteDowntimeAdhocWhitelist(Mockito.anyObject(), Mockito.anyInt(),
				Mockito.anyInt())).thenReturn(responseEntity);

		mockMvc.perform(MockMvcRequestBuilders.delete(url).header("userid", "2").contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(request))).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(1)));
	}

	@Test
	public void deleteDowntimeAdhocWhitelistNoApprovalRequireSuccessTest() throws Exception {
		log.debug("deleteDowntimeAdhocWhitelistNoApprovalRequireSuccessTest()");

		String url = "/bo/system/downtime/adhoc/whitelist/10";
		log.debug("url: {}", url);

		DowntimeAdhocWhitelistResponse response = new DowntimeAdhocWhitelistResponse();
		ResponseEntity<BoData> responseEntity = new ResponseEntity<BoData>(response, HttpStatus.CREATED);

		when(downtimeAdhocWhitelistServiceMock.deleteDowntimeAdhocWhitelist(Mockito.anyObject(), Mockito.anyInt(),
				Mockito.anyInt())).thenReturn(responseEntity);

		mockMvc.perform(MockMvcRequestBuilders.delete(url).header("userid", "2").contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(request))).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(0)));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void deleteDowntimeAdhocWhitelistPendingApprovalExceptionFailTest() throws Exception {
		log.debug("deleteDowntimeAdhocWhitelistPendingApprovalExceptionFailTest()");

		String url = "/bo/system/downtime/adhoc/whitelist/10";
		log.debug("url: {}", url);

		when(downtimeAdhocWhitelistServiceMock.deleteDowntimeAdhocWhitelist(Mockito.anyObject(), Mockito.anyInt(),
				Mockito.anyInt())).thenThrow(PendingApprovalException.class);

		mockMvc.perform(MockMvcRequestBuilders.delete(url).header("userid", "2").contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(request))).andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().isForbidden());
	}

	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
