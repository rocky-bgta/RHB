package com.rhbgroup.dcpbo.customer.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { CommonExceptionTest.class, CommonException.class })
public class CommonExceptionTest {
	
	@Test
	public void testCommonExceptionTest() {
	
		CommonException CommonException=new CommonException("100", "error");
		CommonException.toMap();
		assertEquals("100", CommonException.getErrorCode());
		assertEquals("error", CommonException.getErrorDesc());
		
		CommonException=new CommonException("100", "error",HttpStatus.BAD_REQUEST);
		assertEquals("100", CommonException.getErrorCode());
		assertEquals("error", CommonException.getErrorDesc());
		assertEquals(HttpStatus.BAD_REQUEST, CommonException.getHttpStatus());

	}

}
