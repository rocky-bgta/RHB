package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.exception.CommonException;
import com.rhbgroup.dcpbo.customer.model.UserProfile;
import com.rhbgroup.dcpbo.customer.model.UserTxnMainFuncLimit;
import com.rhbgroup.dcpbo.customer.model.UserTxnMainLimit;
import com.rhbgroup.dcpbo.customer.repository.UserPerTransactionRepo;
import com.rhbgroup.dcpbo.customer.repository.UserProfileRepo;
import com.rhbgroup.dcpbo.customer.repository.UserTxnMainFuncLimitRepo;
import com.rhbgroup.dcpbo.customer.repository.UserTxnMainLimitRepo;
import com.rhbgroup.dcpbo.customer.service.ConfigService;
import com.rhbgroup.dcpbo.customer.service.GetTransactionTransferService;
import com.rhbgroup.dcpbo.customer.vo.CustomerTrxLimitVo;
import com.rhbgroup.dcpbo.customer.vo.MainFunctionLimitsVo;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.netflix.feign.FeignContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ConfigService.class, ConfigServiceImpl.class, ConfigServiceImplTest.Config.class})
public class ConfigServiceImplTest {

    @MockBean
    FeignContext feignContext;

    @MockBean
    GetTransactionTransferService getTransactionTransferService;

    @Autowired
    ConfigService configServiceMock;

    @MockBean
    UserTxnMainFuncLimitRepo userTxnMainFuncLimitRepoMock;

    @MockBean
    UserTxnMainLimitRepo userTxnMainLimitRepoMock;

    @MockBean
    UserProfileRepo userProfileRepoMock;

    @MockBean
    UserPerTransactionRepo userPerTransactionRepoMock;

    List<CustomerTrxLimitVo> mockCustomerTrxLimitVoList;
    List<MainFunctionLimitsVo> mainFunctionLimitsVoList;
    List<UserTxnMainFuncLimit> userTxnMainFuncLimitListMock;

    @TestConfiguration
    static class Config {

        @Bean
        @Primary
        public ConfigService getConfigService() {
            return new ConfigServiceImpl();
        }

    }

    List<CustomerTrxLimitVo> expectedResult;
    UserProfile userProfile = new UserProfile();
    UserTxnMainLimit userTxnMainLimit = new UserTxnMainLimit();

    @Before
    public void setup() {
        this.expectedResult = givenResult();

        userProfile.setId(1);
        userProfile.setCisNo("1234567");
        userProfile.setUsername("Test User");

        UserTxnMainFuncLimit result1 = new UserTxnMainFuncLimit();
        result1.setUserId(1);
        result1.setMainFunction("INSTANT");
        result1.setTxnType("TRANSFER");
        result1.setAmount(BigDecimal.valueOf(12345.6789));
        result1.setId(1);

        UserTxnMainFuncLimit result2 = new UserTxnMainFuncLimit();
        result2.setUserId(1);
        result2.setMainFunction("TOPUP");
        result2.setTxnType("TOPUP");
        result2.setAmount(BigDecimal.valueOf(12345.6789));
        result2.setId(2);

        UserTxnMainFuncLimit result3 = new UserTxnMainFuncLimit();
        result3.setUserId(1);
        result3.setMainFunction("OTHER_BILLER");
        result3.setTxnType("PAYMENT");
        result3.setAmount(BigDecimal.valueOf(12345.6789));
        result3.setId(3);

        userTxnMainFuncLimitListMock = new ArrayList<>();
        userTxnMainFuncLimitListMock.add(result2);
        userTxnMainFuncLimitListMock.add(result1);
        userTxnMainFuncLimitListMock.add(result3);

        userTxnMainLimit.setId(1);
        userTxnMainLimit.setIsAdvanceEnabled(Boolean.TRUE);
        userTxnMainLimit.setTxnType("TOPUP");
        userTxnMainLimit.setUserId(1);
        userTxnMainLimit.setAmount(BigDecimal.valueOf(12345.6789));

    }

    private List<CustomerTrxLimitVo> givenResult() {
        mockCustomerTrxLimitVoList = new ArrayList<>();
        CustomerTrxLimitVo mockCustomerTrxLimitVo = new CustomerTrxLimitVo();
        mockCustomerTrxLimitVo.setTxnType("TRANSFER");
        mockCustomerTrxLimitVo.setMainFunctionLimits(getMainFuncListResult1());
        mockCustomerTrxLimitVoList.add(mockCustomerTrxLimitVo);

        mockCustomerTrxLimitVo = new CustomerTrxLimitVo();
        mockCustomerTrxLimitVo.setTxnType("PAYMENT");
        mockCustomerTrxLimitVo.setMainFunctionLimits(getMainFuncListResult2());
        mockCustomerTrxLimitVoList.add(mockCustomerTrxLimitVo);

        return this.mockCustomerTrxLimitVoList;
    }

    private List<MainFunctionLimitsVo> getMainFuncListResult1() {
        this.mainFunctionLimitsVoList = new ArrayList<>();

        MainFunctionLimitsVo mainFunctionLimitsVo = new MainFunctionLimitsVo();
        mainFunctionLimitsVo.setMainFunction("IBG");
        mainFunctionLimitsVo.setAmount(new BigDecimal(5000.00));
        mainFunctionLimitsVoList.add(mainFunctionLimitsVo);

        mainFunctionLimitsVo = new MainFunctionLimitsVo();
        mainFunctionLimitsVo.setMainFunction("INSTANT");
        mainFunctionLimitsVo.setAmount(new BigDecimal(5000.00));
        mainFunctionLimitsVoList.add(mainFunctionLimitsVo);

        return this.mainFunctionLimitsVoList;
    }

    private List<MainFunctionLimitsVo> getMainFuncListResult2() {
        this.mainFunctionLimitsVoList = new ArrayList<MainFunctionLimitsVo>();

        MainFunctionLimitsVo mainFunctionLimitsVo = new MainFunctionLimitsVo();
        mainFunctionLimitsVo.setMainFunction("OTHER_BILLER");
        mainFunctionLimitsVo.setAmount(new BigDecimal(15000.00));
        mainFunctionLimitsVoList.add(mainFunctionLimitsVo);

        mainFunctionLimitsVo = new MainFunctionLimitsVo();
        mainFunctionLimitsVo.setMainFunction("JOMPAY_BILLER");
        mainFunctionLimitsVo.setAmount(new BigDecimal(10000.00));
        mainFunctionLimitsVoList.add(mainFunctionLimitsVo);

        return this.mainFunctionLimitsVoList;
    }

    @Test
    public void getCustomerTrxLimitsTest() {
        when(userProfileRepoMock.findOneById(Mockito.anyInt())).thenReturn(this.userProfile);
        when(userTxnMainFuncLimitRepoMock.findByUserId(Mockito.anyInt())).thenReturn(this.userTxnMainFuncLimitListMock);
        when(userTxnMainLimitRepoMock.findByUserIdAndTxnType(Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(this.userTxnMainLimit);

        List<CustomerTrxLimitVo> actualResponse = configServiceMock.getCustomerTrxLimits("1");
        Assert.assertThat(actualResponse.get(0).getTxnType(), Matchers.is("TRANSFER"));
    }

    @Test(expected = CommonException.class)
    public void getCustomerTrxLimitsTest_noneIntegerUserId() {
        when(userProfileRepoMock.findOneById(Mockito.anyInt())).thenReturn(this.userProfile);
        when(userTxnMainFuncLimitRepoMock.findByUserId(Mockito.anyInt())).thenReturn(this.userTxnMainFuncLimitListMock);
        when(userTxnMainLimitRepoMock.findByUserIdAndTxnType(Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(this.userTxnMainLimit);

        configServiceMock.getCustomerTrxLimits("A");
    }

    @Test(expected = CommonException.class)
    public void getCustomerTrxLimitsTest_Fail_userNotFound() {
        when(userProfileRepoMock.findOneById(Mockito.anyInt())).thenReturn(null);
        when(userTxnMainFuncLimitRepoMock.findByUserId(Mockito.anyInt())).thenReturn(this.userTxnMainFuncLimitListMock);
        when(userTxnMainLimitRepoMock.findByUserIdAndTxnType(Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(this.userTxnMainLimit);

        configServiceMock.getCustomerTrxLimits("1");
    }

    @Test
    public void getCustomerTrxLimitsTest_txnMainFuncNotFound() {
        when(userProfileRepoMock.findOneById(Mockito.anyInt())).thenReturn(this.userProfile);
        when(userTxnMainFuncLimitRepoMock.findByUserId(Mockito.anyInt())).thenReturn(new ArrayList<>());
        when(userTxnMainLimitRepoMock.findByUserIdAndTxnType(Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(this.userTxnMainLimit);

        List<CustomerTrxLimitVo> actualResponse = configServiceMock.getCustomerTrxLimits("1");
        Assert.assertThat(actualResponse.get(0).getTxnType(), Matchers.is("TOPUP"));
    }

    @Test
    public void getCustomerTrxLimitsTest_userTxnMainLimitNull() {
        when(userProfileRepoMock.findOneById(Mockito.anyInt())).thenReturn(this.userProfile);
        when(userTxnMainFuncLimitRepoMock.findByUserId(Mockito.anyInt())).thenReturn(this.userTxnMainFuncLimitListMock);
        when(userTxnMainLimitRepoMock.findByUserIdAndTxnType(Mockito.anyInt(), Mockito.anyString()))
                .thenReturn(null);

        configServiceMock.getCustomerTrxLimits("1");
    }
}
