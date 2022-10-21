package com.rhbgroup.dcpbo.customer.service.impl;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.feign.FeignContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.repository.BankRepository;
import com.rhbgroup.dcpbo.customer.repository.UserProfileRepo;
import com.rhbgroup.dcpbo.customer.service.PersonalLoanService;
import com.rhbgroup.dcpbo.customer.service.ProfileFavouriteService;
import com.rhbgroup.dcpbo.customer.vo.ProfileFavouriteListVo;
import com.rhbgroup.dcpbo.customer.vo.ProfileFavouriteVo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PersonalLoanService.class, ProfileFavouriteServiceImplTest.class, BankRepository.class,
		UserProfileRepo.class })
public class ProfileFavouriteServiceImplTest {

	@MockBean
	FeignContext feignContext;

	@MockBean
	ConfigErrorInterface configErrorInterface;

	@MockBean
	ProfileFavouriteService serviceImpl;

	@MockBean
	UserProfileRepo mockUserProfileRepo;

	@MockBean
	BankRepository mockBankRepo;

	@Autowired
	private ProfileFavouriteService uut;

	ProfileFavouriteListVo mockProfileFavouriteListVo;
	List<ProfileFavouriteVo> mainProfileFavouriteVoList;

	ProfileFavouriteListVo expectedResult;

	@Before
	public void setup() {

		this.expectedResult = givenResult();
	}

	private ProfileFavouriteListVo givenResult() {
		mockProfileFavouriteListVo = new ProfileFavouriteListVo();
		ProfileFavouriteVo mainProfileFavouriteVo = new ProfileFavouriteVo();
		mainProfileFavouriteVoList = new ArrayList<ProfileFavouriteVo>();

		mainProfileFavouriteVo.setId("1");
		mainProfileFavouriteVo.setTxnType("TRANSFER");
		mainProfileFavouriteVo.setMainFunction("OWN");
		mainProfileFavouriteVo.setPayeeName("RHB Bank");
		mainProfileFavouriteVo.setToAccountNo("12345678901234");
		mainProfileFavouriteVo.setNickname("My House Fund");
		mainProfileFavouriteVo.setRef1("moms allowance");
		mainProfileFavouriteVo.setIsQuickLink(true);
		mainProfileFavouriteVo.setIsQuickPay(true);
		mainProfileFavouriteVo.setMainLabel("My House Fund");
		mainProfileFavouriteVoList.add(mainProfileFavouriteVo);

		mainProfileFavouriteVo = new ProfileFavouriteVo();
		mainProfileFavouriteVo.setId("2");
		mainProfileFavouriteVo.setTxnType("PAYMENT");
		mainProfileFavouriteVo.setMainFunction("OTHER_BILLER");
		mainProfileFavouriteVo.setPayeeName("TNB Sdn Bhd");
		mainProfileFavouriteVo.setNickname("Dad's TNB Bill");
		mainProfileFavouriteVo.setRef1("89073907");
		mainProfileFavouriteVo.setIsQuickLink(true);
		mainProfileFavouriteVo.setIsQuickPay(false);
		mainProfileFavouriteVo.setMainLabel("TNB Sdn Bhd");
		mainProfileFavouriteVoList.add(mainProfileFavouriteVo);

		mockProfileFavouriteListVo.setFavourites(mainProfileFavouriteVoList);

		return this.mockProfileFavouriteListVo;
	}

	@Test
	public void getProfileFavouritesTest() {
		BDDMockito.given(serviceImpl.getProfileFavourites("1")).willReturn(expectedResult);

		ProfileFavouriteListVo actualResponse = (ProfileFavouriteListVo) this.uut.getProfileFavourites("1");

		Assert.assertThat(actualResponse.getFavourites().get(0).getTxnType(), Matchers.is("TRANSFER"));
		Assert.assertThat(actualResponse.getFavourites().get(0).getMainFunction(), Matchers.is("OWN"));
		BDDMockito.verify(serviceImpl, Mockito.atLeastOnce()).getProfileFavourites("1");
	}

	@Test
	public void getPersonalLoanDetailsTest_notFound() throws Exception {

		when(mockUserProfileRepo.getOne(Mockito.anyInt())).thenReturn(null);
		when(mockBankRepo.getOne(Mockito.anyInt())).thenReturn(null);

		String customerId = "1";

		serviceImpl.getProfileFavourites(customerId);
	}
}
