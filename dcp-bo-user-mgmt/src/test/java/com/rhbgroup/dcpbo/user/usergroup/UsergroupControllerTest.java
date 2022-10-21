package com.rhbgroup.dcpbo.user.usergroup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import com.rhbgroup.dcpbo.user.common.UserGroupRepository;
import com.rhbgroup.dcpbo.user.usergroup.dto.UsergroupVo;
import com.rhbgroup.dcpbo.user.usergroup.list.UsergroupListService;
import com.rhbgroup.dcpbo.user.usergroup.list.dto.UsergroupListVo;

import javax.servlet.http.HttpServletResponse;

@RunWith(SpringRunner.class)
@EnableWebMvc
@AutoConfigureMockMvc
@SpringBootTest(classes = { UsergroupControllerTest.class, UsergroupController.class, UsergroupListService.class,
UsergroupAddService.class})
public class UsergroupControllerTest {

	private static Logger logger = LogManager.getLogger(UsergroupControllerTest.class);

	@MockBean
	UsergroupListService usergroupListServiceMock;

	@MockBean
	UsergroupAddService usergroupAddService;

	@MockBean
    UserGroupRepository userGroupRepositoryMock;

	@Autowired
	UsergroupController usergroupController;

	@Autowired
	MockMvc mockMvc;

	private static final String requestBody = "{\n" +
			"    \"functionId\": 2,\n" +
			"    \"groupName\": \"User Admin Test 17\",\n" +
			"    \"function\": [{\n" +
			"        \"functionId\": 1,\n" +
			"        \"functionName\": \"User\"\n" +
			"    }, {\n" +
			"        \"functionId\": 2,\n" +
			"        \"functionName\": \"User Group\"\n" +
			"    }],\n" +
			"    \"accessType\": \"M\"\n" +
			"}";

	private static final String userId = "1";

	@Test
	public void getUsergroupListTest() {
		logger.debug("getUsergroupListTest()");

		final String KEYWORD = "Admin";

		UsergroupVo usergroup1 = new UsergroupVo();
		usergroup1.setGroupId(1);
		usergroup1.setGroupName("Admin Maker");

		UsergroupVo usergroup2 = new UsergroupVo();
		usergroup2.setGroupId(2);
		usergroup2.setGroupName("Admin Approver");

		UsergroupVo usergroup3 = new UsergroupVo();
		usergroup3.setGroupId(3);
		usergroup3.setGroupName("Call Center Approver");

		List<UsergroupVo> usergroupVos = new ArrayList<>();
		usergroupVos.add(usergroup1);
		usergroupVos.add(usergroup2);
		usergroupVos.add(usergroup3);

		UsergroupListVo usergroupListVo = new UsergroupListVo();
		usergroupListVo.setUsergroup(usergroupVos);
		when(usergroupListServiceMock.getUsergroupList(KEYWORD)).thenReturn(usergroupListVo);

		HttpServletResponse response = mock(HttpServletResponse.class);
		response.setStatus(200);

		UsergroupListVo usergroupList = (UsergroupListVo) usergroupController.getUsergroupList(KEYWORD, response);
		assertNotNull(usergroupList);
		assertEquals(3, usergroupList.getUsergroup().size());
	}

	@Test
	public void workflowOverviewControllerTest() throws Exception {
		UsergroupRequestBody usergroupRequestBody = new UsergroupRequestBody();
		usergroupRequestBody.setGroupName("User Group");
		Usergroup usergroup = new Usergroup();
		usergroup.setIsWritten("Y");

		when(usergroupAddService.postUsergroupService(usergroupRequestBody,"2")).thenReturn(usergroup);

		String url = "/bo/usergroup/" ;
		logger.debug("    url: " +  url);

		mockMvc.perform(MockMvcRequestBuilders.post(url).header("userid","2").contentType(APPLICATION_JSON_UTF8).content(requestBody))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
	}
}
