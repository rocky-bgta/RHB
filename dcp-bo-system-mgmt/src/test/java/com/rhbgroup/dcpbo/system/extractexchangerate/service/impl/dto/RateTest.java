package com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.dto;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = { Rate.class })
public class RateTest {
	private static Logger logger = LogManager.getLogger(Rate.class);
	private static final double DELTA = 1e-15;


	@Test
	public void testRateTest() {
		logger.debug("testRateTest()");
		
		Rate rate = new Rate();
		rate.setCode("AUD");
		rate.setBuyTT(0.01);
		rate.setSellTT(1.01);
		rate.setUnit(1);

		
		assertEquals("AUD", rate.getCode());
		assertEquals(0.01, rate.getBuyTT(),DELTA);
		assertEquals(1.01, rate.getSellTT(),DELTA);
		assertEquals(1, rate.getUnit());

	}
}
