package com.rhbgroup.dcpbo.customer;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.data.repository.CommonRepository;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.data.repository.TransferRepository;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaDepositsLogic;
import com.rhbgroup.dcp.loans.bizlogic.GetAsbLoanTransactionHistoryLogic;
import com.rhbgroup.dcp.loans.bizlogic.GetHirePurchaseTransactionsLogic;
import com.rhbgroup.dcp.loans.bizlogic.GetPersonalFinanceTransactionHistoryLogic;
import com.rhbgroup.dcp.profiles.bizlogic.GetDuitNowSendersLogic;
import com.rhbgroup.dcp.profiles.bizlogic.GetDuitnowEnquiryLogic;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaTermDetailsLogic;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaTermLogic;
import com.rhbgroup.dcp.uber.deposits.casa.bizlogic.GetTermDepositDetailsLogic;
import com.rhbgroup.dcpbo.customer.contract.DcpData;
import com.rhbgroup.dcpbo.customer.repository.*;
import com.rhbgroup.dcpbo.customer.service.DcpAccountData;
import com.rhbgroup.dcpbo.customer.service.DcpCustomerAuditService;
import com.rhbgroup.dcpbo.customer.service.DcpCustomerData;
import com.rhbgroup.dcpbo.customer.service.DcpCustomerPaginationData;
import com.rhbgroup.dcpbo.customer.service.impl.DcpCustomerAuditServiceImpl;
import io.ebean.EbeanServer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.rhbgroup.dcpbo.customer.service.DcpRegistrationData;

/**
 * All persistance, data retrieval mechanism bean should be declare here.
 * @author Faisal
 */
@Configuration
public class DataBeanConfiguration {

    @Bean(name = "dcpCustomerData")
    public DcpData getDcpCustomerData() {
        return new DcpCustomerData();
    }

    @Bean(name = "dcpAccountData")
    public DcpData getDcpAccountData() {
        return new DcpAccountData();
    }
    
    @Bean(name = "dcpCustomerPaginationData")
    public DcpData getDcpCustomerPaginationData() {
        return new DcpCustomerPaginationData();
    }
    
    @Bean(name = "dcpRegistrationData")
    public DcpData getDcpRegistrationData() {
        return new DcpRegistrationData();
    }

    @Bean
    public BoSearchRepository getBoSearchRepository() { return new BoSearchRepository(); }

    @Bean(name = "profileRepository")
    public ProfileRepository getProfileRepository() {
        return new ProfileRepository();
    }

    @Bean("boLoanProfileRepository")
    public BoLoanProfileRepository getBoLoanProfileRepository() { return new BoLoanProfileRepository();}

    @Bean("asbLoanTransactionHistoryLogic")
    public BusinessAdaptor asbLoanTransactionHistoryLogic() { return new GetAsbLoanTransactionHistoryLogic();}

    @Bean("boDepositRepository")
    public BoDepositRepository getBoDepositRepository() { return new BoDepositRepository();}

    @Bean("termDepositDetailsLogic")
    public GetTermDepositDetailsLogic getTermDepositDetailsLogic() { return new GetTermDepositDetailsLogic();}

    @Bean("personalFinanceTransactionHistoryLogic")
    public BusinessAdaptor GetPersonalFinanceTransactionHistoryLogic() {return new GetPersonalFinanceTransactionHistoryLogic();}

    @Bean("hirePurchaseTransactionsLogic")
    public BusinessAdaptor getHirePurchaseTransactionsLogic() { return new GetHirePurchaseTransactionsLogic();}

    @Bean("duitnowSendersLogic")
    public BusinessAdaptor getDuitNowSendersLogic(){
        return new GetDuitNowSendersLogic();
    }

    @Bean("duitnowEnquiryLogic")
    public BusinessAdaptor getDuitnowEnquiryLogic(){
        return new GetDuitnowEnquiryLogic();
    }
    
	@Bean("getMcaDepositsLogic")
	public BusinessAdaptor getMcaDepositsLogic() {
		return new GetMcaDepositsLogic();
	}

	@Bean("mcaTermDetailsLogic")
	public BusinessAdaptor getMcaTermDetailsLogic() {
		return new GetMcaTermDetailsLogic();
	}
	
	@Bean("getMcaTermLogic")
	public BusinessAdaptor getMcaTermLogic() {
		return new GetMcaTermLogic();
	}
	
	@Bean("transferRepository")
	public TransferRepository getTransferRepository() {
		return new TransferRepository();
	}
	
	@Bean("commonRepository")
	public CommonRepository getCommonRepository() {
		return new CommonRepository();
	}


//    @Bean("dcpAuditCategoryConfigRepository")
//    public DcpAuditCategoryConfigRepository getDcpAuditCategoryConfigRepository(
//            @Qualifier("eBeanServer") EbeanServer ebeanServer) {
//        return new DcpAuditCategoryConfigRepository(ebeanServer);
//    }
//
//    @Bean("dcpAuditRepository")
//    public DcpAuditRepository getDcpAuditRepository(
//            @Qualifier("eBeanServer") EbeanServer ebeanServer) {
//        return new DcpAuditRepository(ebeanServer);
//    }
//
//    @Bean("dcpAuditEventConfigRepository")
//    public DcpAuditEventConfigRepository getDcpAuditEventConfigRepository (
//            @Qualifier("eBeanServer") EbeanServer ebeanServer, BoRepositoryHelper boRepositoryHelper) {
//        return new DcpAuditEventConfigRepository(ebeanServer, boRepositoryHelper);
//    }
//
//    @Bean("boRepositoryHelper")
//    public BoRepositoryHelper getBoRepositoryHelper() {
//        return new BoRepositoryHelper();
//    }
//
//    @Bean("dcpAuditFundTransferRepository")
//    public DcpAuditFundTransferRepository getDcpAuditFundTransferRepository(
//            @Qualifier("eBeanServer") EbeanServer ebeanServer) {
//        return new DcpAuditFundTransferRepository(ebeanServer);
//    }
//
//    @Bean("dcpAuditBillPaymentRepository")
//    public DcpAuditBillPaymentRepository getDcpAuditBillPaymentRepository(
//            @Qualifier("eBeanServer") EbeanServer ebeanServer) {
//        return new DcpAuditBillPaymentRepository(ebeanServer);
//    }
//
//    @Bean("dcpAuditMiscRepository")
//    public DcpAuditMiscRepository getDcpAuditMiscRepository(
//            @Qualifier("eBeanServer") EbeanServer ebeanServer) {
//        return new DcpAuditMiscRepository(ebeanServer);
//    }
//
//    @Bean("dcpAuditProfileRepository")
//    public DcpAuditProfileRepository getDcpAuditProfileRepository(
//            @Qualifier("eBeanServer") EbeanServer ebeanServer) {
//        return new DcpAuditProfileRepository(ebeanServer);
//    }
//
//    @Bean("dcpAuditTopupRepository")
//    public DcpAuditTopupRepository getDcpAuditTopupRepository(
//            @Qualifier("eBeanServer") EbeanServer ebeanServer) {
//        return new DcpAuditTopupRepository(ebeanServer);
//    }
//
//    @Bean("boAuditSummaryConfigRepository")
//    public BoAuditSummaryConfigRepository getBoAuditSummaryConfigRepository(
//            @Qualifier("eBeanServer") EbeanServer ebeanServer, BoRepositoryHelper boRepositoryHelper) {
//        return new BoAuditSummaryConfigRepository(ebeanServer, boRepositoryHelper);
//    }
}
