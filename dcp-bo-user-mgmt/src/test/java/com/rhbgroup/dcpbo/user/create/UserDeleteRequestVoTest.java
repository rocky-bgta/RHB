package com.rhbgroup.dcpbo.user.create;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UserDeleteRequestVo.class })
public class UserDeleteRequestVoTest {

	@Test
	public void userDeleteRequestVoTest() {

		Integer functionId = 1;
		String username = "handsome";
		String name = "Super Handsome";

		UserDeleteRequestVo userDeleteRequestVo = new UserDeleteRequestVo();
		userDeleteRequestVo.setFunctionId(functionId);
		userDeleteRequestVo.setUsername(username);
		userDeleteRequestVo.setName(name);
		userDeleteRequestVo.toString();

		assertEquals(functionId, userDeleteRequestVo.getFunctionId());
		assertEquals(username, userDeleteRequestVo.getUsername());
		assertEquals(name, userDeleteRequestVo.getName());

	}

}
