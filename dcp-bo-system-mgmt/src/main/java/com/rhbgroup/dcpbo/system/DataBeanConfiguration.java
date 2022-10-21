package com.rhbgroup.dcpbo.system;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.connector.JmsConnector;
import com.rhbgroup.dcp.data.repository.CommonRepository;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaInterestRateLogic;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaRateLogic;
import com.rhbgroup.dcp.estatement.bizlogic.EPullAutoEnrollmentLogic;
import com.rhbgroup.dcp.invest.bizlogic.FPXEnquiryAEMessageLogic;
import com.rhbgroup.dcp.loans.bizlogic.GetHirePurchaseTransactionsLogic;
import com.rhbgroup.dcpbo.system.config.DbConfigProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataBeanConfiguration {

	private static Logger logger = LogManager.getLogger(DataBeanConfiguration.class);

	@Bean(name = "dbConfigProperties")
    public DbConfigProperties getDbConfigProperties() {
	    return new DbConfigProperties();
    }

	@Bean("commonRepository")
	public CommonRepository commonRepository() { return new CommonRepository();}

    @Bean("getMcaRateLogic")
    public GetMcaRateLogic getMcaRateLogic() { return new GetMcaRateLogic();} 
    
    @Bean(name = "profileRepository")
    public ProfileRepository getProfileRepository() {
        return new ProfileRepository();
    }
    
    @Bean("getMcaInterestRateLogic")
    public GetMcaInterestRateLogic getMcaInterestRateLogic() { return new GetMcaInterestRateLogic();} 
    
    @Bean("fPXEnquiryAEMessageLogic")
    public FPXEnquiryAEMessageLogic fPXEnquiryAEMessageLogic() { return new FPXEnquiryAEMessageLogic();}

    @Bean("epullAutoEnrollmentLogic")
    public EPullAutoEnrollmentLogic getEPullAutoEnrollmentLogic() { return new EPullAutoEnrollmentLogic();}
}
