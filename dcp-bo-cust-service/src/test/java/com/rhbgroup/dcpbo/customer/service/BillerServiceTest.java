package com.rhbgroup.dcpbo.customer.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.BillerResponse;
import com.rhbgroup.dcpbo.customer.repository.BillerRepo;
import com.rhbgroup.dcpbo.customer.repository.TopupBillerRepo;
import com.rhbgroup.dcpbo.customer.service.impl.BillerServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BillerService.class, BillerServiceTest.Config.class })
public class BillerServiceTest {

	@Autowired
	BillerService billerService;

	@MockBean
	private BillerRepo billerRepoMock;

	@MockBean
	private TopupBillerRepo topupBillerRepoMock;
	
	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public BillerService getBillerService() {
			return new BillerServiceImpl();
		}
	}
	
	@Test
	public void getBillerCountSuccessTest() {
		
        when(billerRepoMock.getCount()).thenReturn(35);
        when(topupBillerRepoMock.getCount()).thenReturn(35);
		
        BoData response = (BoData) billerService.getBillerCount();
        BillerResponse res = (BillerResponse) response;
		assertEquals(70, res.getBiller().getTotal());
		
	}
}
