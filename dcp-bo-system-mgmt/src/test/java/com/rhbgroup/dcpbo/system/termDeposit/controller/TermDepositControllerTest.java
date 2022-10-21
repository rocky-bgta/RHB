package com.rhbgroup.dcpbo.system.termDeposit.controller;

import static org.mockito.Mockito.when;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
import com.rhbgroup.dcpbo.system.downtime.dto.DowntimeAdhoc;
import com.rhbgroup.dcpbo.system.downtime.service.DowntimeAdhocService;
import com.rhbgroup.dcpbo.system.downtime.vo.UpdateDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.exception.AdhocDurationOverlappedException;
import com.rhbgroup.dcpbo.system.exception.DowntimeExceptionAdvice;
import com.rhbgroup.dcpbo.system.termDeposit.dto.TermDepositPlacementConfirmationRequest;
import com.rhbgroup.dcpbo.system.termDeposit.dto.TermDepositPlacementConfirmationResponse;
import com.rhbgroup.dcpbo.system.termDeposit.service.TermDepositPlacementService;



@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { TermDepositControllerTest.class, TermDepositController.class})
@EnableWebMvc
public class TermDepositControllerTest {
	
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	TermDepositPlacementService termDepositPlacementService;
	
	private TermDepositPlacementConfirmationRequest termDepositPlacementConfirmationRequest;
	
	@TestConfiguration
	static class Config {
		@Bean
		public CommonException getCommonException() {
			return new CommonException(CommonException.GENERIC_ERROR_CODE, "NA");
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
	

	private static Logger logger = LogManager.getLogger(TermDepositControllerTest.class);
	
	@Before
    public void setup(){

		termDepositPlacementConfirmationRequest = new TermDepositPlacementConfirmationRequest();
		
		termDepositPlacementConfirmationRequest.setTxnStatus("SUCCESS");
		termDepositPlacementConfirmationRequest.setTxnToken("26722");

    }

	@Test
	public void termDepositControllerSuccessTest() throws Exception {
		logger.debug("termDepositControllerSuccessTest()");
		
		String url = "/bo/system/fpx/term-deposit/placement";
		logger.debug("    url: " + url);
		
		TermDepositPlacementConfirmationResponse termDepositPlacementConfirmationResponse  = new TermDepositPlacementConfirmationResponse();
		termDepositPlacementConfirmationResponse.setCode("10000");
		termDepositPlacementConfirmationResponse.setStatusType("Success");

		ResponseEntity<BoData> responseEntity = new ResponseEntity<BoData>(termDepositPlacementConfirmationResponse, HttpStatus.CREATED);
		
		when(termDepositPlacementService.termDepositPlacement(Mockito.anyObject())).thenReturn(responseEntity);
		
		mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(termDepositPlacementConfirmationRequest)))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("code", Matchers.is("10000")));
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void termDepositControllerFailTest() throws Exception {
		logger.debug("termDepositControllerFailTest()");
		
		String url = "/bo/system/fpx/term-deposit/placement";
		logger.debug("    url: " + url);
		
		when(termDepositPlacementService.termDepositPlacement(Mockito.anyObject())).thenThrow(CommonException.class);
		
		mockMvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON).content(asJsonString(termDepositPlacementConfirmationRequest)))
				.andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
	}
	
	public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
