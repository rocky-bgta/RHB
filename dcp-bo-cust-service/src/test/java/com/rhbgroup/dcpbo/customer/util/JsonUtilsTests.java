package com.rhbgroup.dcpbo.customer.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JsonUtilsTests {
	
	public static final String PATH_REF_ID = "$.request.refId";
	
	public static final String PATH_FROM_ACCOUNT_NO = "$.request.fromAccountNo";
	
	@Test
	public void testPositiveRefIdNotFound() {
		String json = "{\n" + 
				"  \"request\": {\n" + 
				"    \"txnToken\": \"7a0b790d-8021-4276-bb95-c139010c8b6b\",\n" + 
				"    \"secureplus\": {\n" + 
				"      \"responseCode\": \"676389\"\n" + 
				"    }\n" + 
				"  },\n" + 
				"  \"response\": {\n" + 
				"    \"refId\": \"1544068534010118\",\n" + 
				"    \"status\": {\n" + 
				"      \"code\": \"PENDING\"\n" + 
				"    },\n" + 
				"    \"fromAccountNo\": \"21412900306249\",\n" + 
				"    \"nickname\": \"sumi\",\n" + 
				"    \"amount\": 10,\n" + 
				"    \"toBiller\": {\n" + 
				"      \"id\": 13,\n" + 
				"      \"name\": \"Digi Prepaid\",\n" + 
				"      \"iconUrl\": \"/content/images/topups/016_Digi_Prepaid.png\"\n" + 
				"    },\n" + 
				"    \"ref1\": \"0172514013\",\n" + 
				"    \"serviceCharge\": 0,\n" + 
				"    \"gstAmount\": 0,\n" + 
				"    \"transactionTime\": \"2018-12-06T11:55:34+08:00\",\n" + 
				"    \"transactionId\": \"4bd80836-b314-4394-8ca3-625522972003\",\n" + 
				"    \"multiFactorAuth\": \"MANUAL_SIGN\"\n" + 
				"  },\n" + 
				"  \"additionalData\": {\n" + 
				"    \"billerName\": \"Digi Prepaid\",\n" + 
				"    \"billerCode\": \"016\"\n" + 
				"  }\n" + 
				"}";
		
		String refId = JsonUtils.getJsonPathValueWithDefaultNull(json, PATH_REF_ID);
		
		assertEquals(null, refId);
	}
	
	@Test
	public void testPositiveRefIdFound() {
		String json = "{\n" + 
				"  \"request\": {\n" + 
				"    \"txnToken\": \"7a0b790d-8021-4276-bb95-c139010c8b6b\",\n" + 
				"    \"refId\": \"1544068534010118\",\n" + 				
				"    \"secureplus\": {\n" + 
				"      \"responseCode\": \"676389\"\n" + 
				"    }\n" + 
				"  },\n" + 
				"  \"response\": {\n" + 
				"    \"refId\": \"1544068534010118\",\n" + 
				"    \"status\": {\n" + 
				"      \"code\": \"PENDING\"\n" + 
				"    },\n" + 
				"    \"fromAccountNo\": \"21412900306249\",\n" + 
				"    \"nickname\": \"sumi\",\n" + 
				"    \"amount\": 10,\n" + 
				"    \"toBiller\": {\n" + 
				"      \"id\": 13,\n" + 
				"      \"name\": \"Digi Prepaid\",\n" + 
				"      \"iconUrl\": \"/content/images/topups/016_Digi_Prepaid.png\"\n" + 
				"    },\n" + 
				"    \"ref1\": \"0172514013\",\n" + 
				"    \"serviceCharge\": 0,\n" + 
				"    \"gstAmount\": 0,\n" + 
				"    \"transactionTime\": \"2018-12-06T11:55:34+08:00\",\n" + 
				"    \"transactionId\": \"4bd80836-b314-4394-8ca3-625522972003\",\n" + 
				"    \"multiFactorAuth\": \"MANUAL_SIGN\"\n" + 
				"  },\n" + 
				"  \"additionalData\": {\n" + 
				"    \"billerName\": \"Digi Prepaid\",\n" + 
				"    \"billerCode\": \"016\"\n" + 
				"  }\n" + 
				"}";
		
		String refId = JsonUtils.getJsonPathValueWithDefaultNull(json, PATH_REF_ID);
		
		assertEquals("1544068534010118", refId);
	}
	
	@Test
	public void testPositiveFromAccountNumberNotFound() {
		String json = "{\n" + 
				"  \"request\": {\n" + 
				"    \"txnToken\": \"7a0b790d-8021-4276-bb95-c139010c8b6b\",\n" + 
				"    \"refId\": \"1544068534010118\",\n" + 				
				"    \"secureplus\": {\n" + 
				"      \"responseCode\": \"676389\"\n" + 
				"    }\n" + 
				"  },\n" + 
				"  \"response\": {\n" + 
				"    \"refId\": \"1544068534010118\",\n" + 
				"    \"status\": {\n" + 
				"      \"code\": \"PENDING\"\n" + 
				"    },\n" + 
				"    \"fromAccountNo\": \"21412900306249\",\n" + 
				"    \"nickname\": \"sumi\",\n" + 
				"    \"amount\": 10,\n" + 
				"    \"toBiller\": {\n" + 
				"      \"id\": 13,\n" + 
				"      \"name\": \"Digi Prepaid\",\n" + 
				"      \"iconUrl\": \"/content/images/topups/016_Digi_Prepaid.png\"\n" + 
				"    },\n" + 
				"    \"ref1\": \"0172514013\",\n" + 
				"    \"serviceCharge\": 0,\n" + 
				"    \"gstAmount\": 0,\n" + 
				"    \"transactionTime\": \"2018-12-06T11:55:34+08:00\",\n" + 
				"    \"transactionId\": \"4bd80836-b314-4394-8ca3-625522972003\",\n" + 
				"    \"multiFactorAuth\": \"MANUAL_SIGN\"\n" + 
				"  },\n" + 
				"  \"additionalData\": {\n" + 
				"    \"billerName\": \"Digi Prepaid\",\n" + 
				"    \"billerCode\": \"016\"\n" + 
				"  }\n" + 
				"}";
		
		String fromAccountNo = JsonUtils.getJsonPathValueWithDefaultNull(json, PATH_FROM_ACCOUNT_NO);
		
		assertEquals(null, fromAccountNo);
	}
	
	@Test
	public void testPositiveFromAccountNumberFound() {
		String json = "{\n" + 
				"  \"request\": {\n" + 
				"    \"txnToken\": \"7a0b790d-8021-4276-bb95-c139010c8b6b\",\n" + 
				"    \"refId\": \"1544068534010118\",\n" + 	
				"    \"fromAccountNo\": \"21412900306249\",\n" + 				
				"    \"secureplus\": {\n" + 
				"      \"responseCode\": \"676389\"\n" + 
				"    }\n" + 
				"  },\n" + 
				"  \"response\": {\n" + 
				"    \"refId\": \"1544068534010118\",\n" + 
				"    \"status\": {\n" + 
				"      \"code\": \"PENDING\"\n" + 
				"    },\n" + 
				"    \"fromAccountNo\": \"21412900306249\",\n" + 
				"    \"nickname\": \"sumi\",\n" + 
				"    \"amount\": 10,\n" + 
				"    \"toBiller\": {\n" + 
				"      \"id\": 13,\n" + 
				"      \"name\": \"Digi Prepaid\",\n" + 
				"      \"iconUrl\": \"/content/images/topups/016_Digi_Prepaid.png\"\n" + 
				"    },\n" + 
				"    \"ref1\": \"0172514013\",\n" + 
				"    \"serviceCharge\": 0,\n" + 
				"    \"gstAmount\": 0,\n" + 
				"    \"transactionTime\": \"2018-12-06T11:55:34+08:00\",\n" + 
				"    \"transactionId\": \"4bd80836-b314-4394-8ca3-625522972003\",\n" + 
				"    \"multiFactorAuth\": \"MANUAL_SIGN\"\n" + 
				"  },\n" + 
				"  \"additionalData\": {\n" + 
				"    \"billerName\": \"Digi Prepaid\",\n" + 
				"    \"billerCode\": \"016\"\n" + 
				"  }\n" + 
				"}";
		
		String fromAccountNo = JsonUtils.getJsonPathValueWithDefaultNull(json, PATH_FROM_ACCOUNT_NO);
		
		assertEquals("21412900306249", fromAccountNo);
	}
	
	@Test
	public void testPositiveFromAccountNumberEmptyFound() {
		String json = "{\n" + 
				"  \"request\": {\n" + 
				"    \"txnToken\": \"7a0b790d-8021-4276-bb95-c139010c8b6b\",\n" + 
				"    \"refId\": \"1544068534010118\",\n" + 	
				"    \"fromAccountNo\":,\n" + 				
				"    \"secureplus\": {\n" + 
				"      \"responseCode\": \"676389\"\n" + 
				"    }\n" + 
				"  },\n" + 
				"  \"response\": {\n" + 
				"    \"refId\": \"1544068534010118\",\n" + 
				"    \"status\": {\n" + 
				"      \"code\": \"PENDING\"\n" + 
				"    },\n" + 
				"    \"fromAccountNo\": \"21412900306249\",\n" + 
				"    \"nickname\": \"sumi\",\n" + 
				"    \"amount\": 10,\n" + 
				"    \"toBiller\": {\n" + 
				"      \"id\": 13,\n" + 
				"      \"name\": \"Digi Prepaid\",\n" + 
				"      \"iconUrl\": \"/content/images/topups/016_Digi_Prepaid.png\"\n" + 
				"    },\n" + 
				"    \"ref1\": \"0172514013\",\n" + 
				"    \"serviceCharge\": 0,\n" + 
				"    \"gstAmount\": 0,\n" + 
				"    \"transactionTime\": \"2018-12-06T11:55:34+08:00\",\n" + 
				"    \"transactionId\": \"4bd80836-b314-4394-8ca3-625522972003\",\n" + 
				"    \"multiFactorAuth\": \"MANUAL_SIGN\"\n" + 
				"  },\n" + 
				"  \"additionalData\": {\n" + 
				"    \"billerName\": \"Digi Prepaid\",\n" + 
				"    \"billerCode\": \"016\"\n" + 
				"  }\n" + 
				"}";
		
		String fromAccountNo = JsonUtils.getJsonPathValueWithDefaultNull(json, PATH_FROM_ACCOUNT_NO);
		
		assertEquals(null, fromAccountNo);
	}
	
	@Test
	public void testPositiveFromAccountNumberNullFound() {
		String json = "{\n" + 
				"  \"request\": {\n" + 
				"    \"txnToken\": \"7a0b790d-8021-4276-bb95-c139010c8b6b\",\n" + 
				"    \"refId\": \"1544068534010118\",\n" + 	
				"    \"fromAccountNo\": null,\n" + 				
				"    \"secureplus\": {\n" + 
				"      \"responseCode\": \"676389\"\n" + 
				"    }\n" + 
				"  },\n" + 
				"  \"response\": {\n" + 
				"    \"refId\": \"1544068534010118\",\n" + 
				"    \"status\": {\n" + 
				"      \"code\": \"PENDING\"\n" + 
				"    },\n" + 
				"    \"fromAccountNo\": \"21412900306249\",\n" + 
				"    \"nickname\": \"sumi\",\n" + 
				"    \"amount\": 10,\n" + 
				"    \"toBiller\": {\n" + 
				"      \"id\": 13,\n" + 
				"      \"name\": \"Digi Prepaid\",\n" + 
				"      \"iconUrl\": \"/content/images/topups/016_Digi_Prepaid.png\"\n" + 
				"    },\n" + 
				"    \"ref1\": \"0172514013\",\n" + 
				"    \"serviceCharge\": 0,\n" + 
				"    \"gstAmount\": 0,\n" + 
				"    \"transactionTime\": \"2018-12-06T11:55:34+08:00\",\n" + 
				"    \"transactionId\": \"4bd80836-b314-4394-8ca3-625522972003\",\n" + 
				"    \"multiFactorAuth\": \"MANUAL_SIGN\"\n" + 
				"  },\n" + 
				"  \"additionalData\": {\n" + 
				"    \"billerName\": \"Digi Prepaid\",\n" + 
				"    \"billerCode\": \"016\"\n" + 
				"  }\n" + 
				"}";
		
		String fromAccountNo = JsonUtils.getJsonPathValueWithDefaultNull(json, PATH_FROM_ACCOUNT_NO);
		
		assertEquals(null, fromAccountNo);
	}
	
	@Test
	public void testPositiveJsonNotValidFound() {
		String json = "{ \"request\": { \"refId\": \"ABCDEFG\", \"fromAccountNo\": null, \"fromCardNo\": } }";
		
		String fromAccountNo = JsonUtils.getJsonPathValueWithDefaultNull(json, PATH_FROM_ACCOUNT_NO);
		
		assertEquals(null, fromAccountNo);
	}

}
