package com.rhbgroup.dcpbo.system.downtime.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.BankDetailsInfo;
import com.rhbgroup.dcpbo.system.downtime.repository.BankPaymentTypeConfigRepository;
import com.rhbgroup.dcpbo.system.downtime.repository.BankRepository;
import com.rhbgroup.dcpbo.system.downtime.service.impl.BankServiceImpl;
import com.rhbgroup.dcpbo.system.model.Bank;
import com.rhbgroup.dcpbo.system.model.BankPaymentTypeConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BankService.class, BankServiceTest.Config.class })
public class BankServiceTest {
	
	@Autowired
	BankService bankService;
	
	@MockBean
	private BankRepository bankRepositoryMock;
	
	@MockBean
	private BankPaymentTypeConfigRepository bankPaymentTypeConfigRepositoryMock;
	
	private Timestamp now;
	
    private Bank bank;
    
    private BankPaymentTypeConfig bankPaymentTypeConfig;
	
	@TestConfiguration
	static class Config {
		@Bean
		@Primary
		public BankService getBankService() {
			return new BankServiceImpl();
		}
	}
	
	@Before
    public void setup(){

		now = new Timestamp(System.currentTimeMillis());
		
		bank = new Bank();
		bank.setId(16);
		bank.setBankName("Affin Bank Berhad");
		bank.setBankNameShort("Affin Bank");
		
		bankPaymentTypeConfig = new BankPaymentTypeConfig();
		
		bankPaymentTypeConfig.setBankId(16);
		bankPaymentTypeConfig.setIbgFlag("Y");
		bankPaymentTypeConfig.setInstantFlag("N");

    }
	
	@Test
	public void testGetBankPaymentTypeSuccessTest() {
		List<Bank> expected = new ArrayList<Bank>();
		expected.add(bank);

        when(bankRepositoryMock.getBankList(Mockito.anyInt(),Mockito.anyInt())).thenReturn(expected);
        when(bankRepositoryMock.getBankListCount()).thenReturn(1);
       
        List<BankPaymentTypeConfig> expected1 = new ArrayList<BankPaymentTypeConfig>();
		expected1.add(bankPaymentTypeConfig);

        when(bankPaymentTypeConfigRepositoryMock.getBankPaymentTypeConfigDetail(Mockito.anyInt())).thenReturn(expected1);
		
		
		BoData response = bankService.getBankPaymentType(1);
		BankDetailsInfo bankDetailsInfo = (BankDetailsInfo) response;
		
		
		assertEquals(1, bankDetailsInfo.getBank().size());
		assertEquals((int)bank.getId(), bankDetailsInfo.getBank().get(0).getId());
		assertEquals(bank.getBankName(), bankDetailsInfo.getBank().get(0).getName());
		assertEquals(bank.getBankNameShort(), bankDetailsInfo.getBank().get(0).getShortName());
		assertEquals("ON", bankDetailsInfo.getBank().get(0).getIsIbg());
		assertEquals("OFF", bankDetailsInfo.getBank().get(0).getIsInstant());
		
		assertEquals(1, bankDetailsInfo.getPagination().getActivityCount());
		assertEquals(1, bankDetailsInfo.getPagination().getTotalPageNum());
		assertEquals(1, bankDetailsInfo.getPagination().getPageNum());
		assertEquals("L", bankDetailsInfo.getPagination().getPageIndicator());

	}
	
}
