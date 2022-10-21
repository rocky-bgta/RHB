package com.rhbgroup.dcpbo.user.create;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UserPayloadDeleteVo.class })
public class UserPayloadDeleteVoTest {

	@Test
	public void userPayloadDeleteVoTest() {

		Integer userId = 1;
		String username = "leeyawkhang";
		String name = "Lee Yaw Khang";
		String status = "A";
		String email = "email@email.com";

		UserPayloadDeleteVo userPayloadDeleteVo = new UserPayloadDeleteVo();
		userPayloadDeleteVo.setUserId(userId);
		userPayloadDeleteVo.setUsername(username);
		userPayloadDeleteVo.setName(name);
		userPayloadDeleteVo.setStatus(status);
		userPayloadDeleteVo.setEmail(email);
		userPayloadDeleteVo.toString();

		assertEquals(userId, userPayloadDeleteVo.getUserId());
		assertEquals(username, userPayloadDeleteVo.getUsername());
		assertEquals(name, userPayloadDeleteVo.getName());
		assertEquals(status, userPayloadDeleteVo.getStatus());
		assertEquals(email, userPayloadDeleteVo.getEmail());

	}

}
