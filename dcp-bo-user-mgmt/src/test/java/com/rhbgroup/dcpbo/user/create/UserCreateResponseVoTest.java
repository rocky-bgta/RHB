package com.rhbgroup.dcpbo.user.create;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { UserCreateResponseVo.class })
public class UserCreateResponseVoTest {

	@Test
	public void userCreateRequestVoTest() {

		Integer approvalId = 30;
		String isWritten = "N";

		UserCreateResponseVo userCreateResponseVo = new UserCreateResponseVo();
		userCreateResponseVo.setApprovalId(approvalId);
		userCreateResponseVo.setIsWritten(isWritten);
		userCreateResponseVo.toString();

		assertEquals(approvalId, userCreateResponseVo.getApprovalId());
		assertEquals(isWritten, userCreateResponseVo.getIsWritten());
	}
}
