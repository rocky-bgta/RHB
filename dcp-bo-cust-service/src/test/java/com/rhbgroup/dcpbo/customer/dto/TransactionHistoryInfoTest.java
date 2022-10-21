package com.rhbgroup.dcpbo.customer.dto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.model.Capsule;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TransactionHistoryInfoTest.class, TransactionHistoryInfo.class })
public class TransactionHistoryInfoTest {
	
	@Autowired
	private TransactionHistoryInfo transactionHistoryInfo;
	
	@Test
	public void testNullPointerFirstKeyAndLastKey() {
		Capsule capsule = new Capsule();
		String message = "{\"pagination\":{\"isLastPage\":\"L\",\"pageCounter\":\"01\"},"
				+ "\"transactionHistory\":["
				+ "{\"txnDate\":\"15022019\",\"description\":\"INSTALMENT\",\"amount\":\"1871.00\"},"
				+ "{\"txnDate\":\"25022019\",\"description\":\"INSTALMENT\",\"amount\":\"1900.00\"},"
				+ "{\"txnDate\":\"25032019\",\"description\":\"INSTALMENT\",\"amount\":\"1900.00\"}]}";
		capsule.updateCurrentMessage(message);
		transactionHistoryInfo.convert(capsule);
		
		assertEquals("", transactionHistoryInfo.getPagination().getFirstKey());
		assertEquals("", transactionHistoryInfo.getPagination().getLastKey());
	}
	
	@Test
	public void testFirstKeyAndLastKey() {
		Capsule capsule = new Capsule();
		String message = "{\"pagination\":{\"firstKey\":\"1\",\"lastKey\":\"1\",\"isLastPage\":\"L\",\"pageCounter\":\"01\"},"
				+ "\"transactionHistory\":["
				+ "{\"txnDate\":\"15022019\",\"description\":\"INSTALMENT\",\"amount\":\"1871.00\"},"
				+ "{\"txnDate\":\"25022019\",\"description\":\"INSTALMENT\",\"amount\":\"1900.00\"},"
				+ "{\"txnDate\":\"25032019\",\"description\":\"INSTALMENT\",\"amount\":\"1900.00\"}]}";
		capsule.updateCurrentMessage(message);
		transactionHistoryInfo.convert(capsule);
		
		assertEquals("1", transactionHistoryInfo.getPagination().getFirstKey());
		assertEquals("1", transactionHistoryInfo.getPagination().getLastKey());
	}

}
