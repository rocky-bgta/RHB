package com.rhbgroup.dcpbo.user.usergroupdelete;

import com.rhbgroup.dcp.util.JsonUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = { UsergroupDeleteFunctionControllerTest.class, UsergroupDeleteFunctionController.class })
@EnableWebMvc
public class UsergroupDeleteFunctionControllerTest {
	@Autowired
	MockMvc mockMvc;

	@MockBean(name = "usergroupDeleteFunctionService")
	UsergroupDeleteFunctionService usergroupDeleteFunctionService;

	private static Logger logger = LogManager.getLogger(UsergroupDeleteFunctionControllerTest.class);

	@Test
	public void testDeleteBoUsergroup() throws Exception {
		logger.debug("testDeleteBoUsergroup()");
		UsergroupDeleteResponseVo response = getUsergroupDeleteResponse();
		BDDMockito.given(usergroupDeleteFunctionService.deleteBoUsergroup(anyInt(), anyObject(), anyInt())).willReturn(response);

		Integer creatorId = 10;
		Integer usergroupId = 1;
		String url = "/bo/usergroup/" + usergroupId + "/delete";
		logger.debug("    url: " + url);
		mockMvc.perform(MockMvcRequestBuilders.put(url).header("userid", creatorId)
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(asJsonString(getUsergroupDeleteRequest())))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
				.andExpect(MockMvcResultMatchers.jsonPath("approvalId", Matchers.is(0)))
				.andExpect(MockMvcResultMatchers.jsonPath("isWritten", Matchers.is("Y")))
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}

	private UsergroupDeleteRequestVo getUsergroupDeleteRequest() {
		UsergroupDeleteRequestVo usergroupDeleteRequestVo = new UsergroupDeleteRequestVo();
		usergroupDeleteRequestVo.setFunctionId(1);
		usergroupDeleteRequestVo.setAccessType("AccessType");
		return usergroupDeleteRequestVo;
	}

	private UsergroupDeleteResponseVo getUsergroupDeleteResponse() {
		UsergroupDeleteResponseVo usergroupDeleteResponseVo = new UsergroupDeleteResponseVo();
		usergroupDeleteResponseVo.setApprovalId(0);
		usergroupDeleteResponseVo.setIsWritten("Y");
		return usergroupDeleteResponseVo;
	}

	private static String asJsonString(final Object obj) {
		return JsonUtil.objectToJson(obj);
	}
}

