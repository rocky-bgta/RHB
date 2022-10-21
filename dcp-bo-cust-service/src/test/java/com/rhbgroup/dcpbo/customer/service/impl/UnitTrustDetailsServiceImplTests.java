package com.rhbgroup.dcpbo.customer.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import com.rhbgroup.dcp.data.entity.profiles.UserProfile;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.investments.bizlogic.GetUnitTrustDetailsLogic;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.dto.CustomerAccounts;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rhbgroup.dcpbo.customer.model.InvestmentProfile;
import com.rhbgroup.dcpbo.customer.model.UnitTrustAccount;
import com.rhbgroup.dcpbo.customer.model.UnitTrustAccountHolding;
import com.rhbgroup.dcpbo.customer.model.UnitTrustDetails;
import com.rhbgroup.dcpbo.customer.model.UnitTrustFundMaster;
import com.rhbgroup.dcpbo.customer.repository.InvestmentProfileRepository;
import com.rhbgroup.dcpbo.customer.repository.UnitTrustAccountHoldingRepository;
import com.rhbgroup.dcpbo.customer.repository.UnitTrustAccountRepository;
import com.rhbgroup.dcpbo.customer.repository.UnitTrustFundMasterRepository;
import com.rhbgroup.dcpbo.customer.service.UnitTrustDetailsService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {UnitTrustDetailsServiceImpl.class, UnitTrustDetailsServiceImpl.class, AdditionalDataHolder.class})
public class UnitTrustDetailsServiceImplTests {

    @MockBean
    private GetUnitTrustDetailsLogic getUnitTrustDetailsLogic;

    @Autowired
    private UnitTrustDetailsServiceImpl unitTrustDetailsServiceImpl;

    @MockBean
    private ProfileRepository profileRepository;

	int customerId = 1;
	String accountNo = "1234-567890";

	private static Logger logger = LogManager.getLogger(UnitTrustDetailsServiceImplTests.class);

	@Test
	public void getUnitTrustDetailsTest() throws Throwable {
        Capsule capsule = new Capsule();
        capsule = createCapsule("GetUnitTrustLogic.json");

        when(getUnitTrustDetailsLogic.executeBusinessLogic(Mockito.any())).thenReturn(capsule);

        UserProfile userProfile = new UserProfile();
        userProfile.setUsername("Test");
        userProfile.setIdNo("123");

        when(profileRepository.getUserProfileByUserId(any())).thenReturn(userProfile);

        UnitTrustDetails unitTrustDetails = (UnitTrustDetails) unitTrustDetailsServiceImpl.getUnitTrustDetails(accountNo, customerId);
        assertNotNull(unitTrustDetails);
	}

    private Capsule createCapsule(String filename) throws IOException {
        logger.debug("Loading file: " + filename + " ...");

        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
            sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        logger.debug("    jsonStr: " + jsonStr);

        Capsule capsule = new Capsule();
        capsule.updateCurrentMessage(jsonStr);
        capsule.setOperationSuccess(true);
        return capsule;
    }
}
