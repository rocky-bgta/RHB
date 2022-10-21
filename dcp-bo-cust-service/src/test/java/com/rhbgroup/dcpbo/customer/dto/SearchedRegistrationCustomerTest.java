package com.rhbgroup.dcpbo.customer.dto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

import java.util.Date;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SearchedRegistrationCustomer.class })
public class SearchedRegistrationCustomerTest {
	private static Logger logger = LogManager.getLogger(SearchedRegistrationCustomer.class);

	@Test
	public void testBanksTest() {
		logger.debug("testSearchRegistrationTest()");
		String custid = "1";
		String username = "userName";
		String name = "name";
	    String email = "email";
	    String mobileNo = "mobileNo";
	    String cisNo = "cisNo";
	    String aaoip = "aaoip";
	    String idType = "idType";
	    String idNo = "idNo";
	    String status = "status";
		Boolean isPremier = Boolean.FALSE;
	    Date lastLogin = new Date();
	    Boolean isRegistered = Boolean.FALSE;
	    Date lastRegistrationAttempt = new Date();
	    Boolean isLocked = Boolean.FALSE;
	    String acctNumber = "acctNumber";
		
		SearchedRegistrationCustomer searchedRegistrationCustomer = new SearchedRegistrationCustomer();
		searchedRegistrationCustomer.setCustid(custid);
		searchedRegistrationCustomer.setUsername(username);
		searchedRegistrationCustomer.setName(name);
		searchedRegistrationCustomer.setEmail(email);
		searchedRegistrationCustomer.setMobileNo(mobileNo);
		searchedRegistrationCustomer.setCisNo(cisNo);
		searchedRegistrationCustomer.setAaoip(aaoip);
		searchedRegistrationCustomer.setIdType(idType);
		searchedRegistrationCustomer.setIdNo(idNo);
		searchedRegistrationCustomer.setStatus(status);
		searchedRegistrationCustomer.setIsPremier(isPremier);
		searchedRegistrationCustomer.setLastLogin(lastLogin);
		searchedRegistrationCustomer.setIsRegistered(isRegistered);
		searchedRegistrationCustomer.setLastRegistrationAttempt(lastRegistrationAttempt);
		searchedRegistrationCustomer.setIsLocked(isLocked);
		searchedRegistrationCustomer.setAcctNumber(acctNumber);
		
		assertEquals(custid, searchedRegistrationCustomer.getCustid());
		assertEquals(username, searchedRegistrationCustomer.getUsername());
		assertEquals(name, searchedRegistrationCustomer.getName());
		assertEquals(email, searchedRegistrationCustomer.getEmail());
		assertEquals(mobileNo, searchedRegistrationCustomer.getMobileNo());
		assertEquals(cisNo, searchedRegistrationCustomer.getCisNo());
		assertEquals(aaoip, searchedRegistrationCustomer.getAaoip());
		assertEquals(idType, searchedRegistrationCustomer.getIdType());
		assertEquals(idNo, searchedRegistrationCustomer.getIdNo());
		assertEquals(isPremier, searchedRegistrationCustomer.getIsPremier());
		assertEquals(lastLogin, searchedRegistrationCustomer.getLastLogin());
		assertEquals(isRegistered, searchedRegistrationCustomer.getIsRegistered());
		assertEquals(lastRegistrationAttempt, searchedRegistrationCustomer.getLastRegistrationAttempt());
		assertEquals(isLocked, searchedRegistrationCustomer.getIsLocked());
		assertEquals(acctNumber, searchedRegistrationCustomer.getAcctNumber());



	}
	
}
