package com.rhbgroup.dcpbo.customer.audit.collector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

import java.util.Date;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PutUnblockFacilityAdditionalDataRetriever.class })
public class PutUnblockFacilityAdditionalDataRetrieverTest {
	private static Logger logger = LogManager.getLogger(PutUnblockFacilityAdditionalDataRetriever.class);

	@MockBean
	private AdditionalDataHolder additionalDataHolder;
	
	@Autowired
	PutUnblockFacilityAdditionalDataRetriever putUnblockFacilityAdditionalDataRetriever;
	
	@Test
	public void testUnblockFacilityTest() {
		logger.debug("testCollector()");		
		
		putUnblockFacilityAdditionalDataRetriever.retrieve();

	}
	
}
