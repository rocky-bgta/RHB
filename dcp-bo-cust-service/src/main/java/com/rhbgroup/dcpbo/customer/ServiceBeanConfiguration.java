package com.rhbgroup.dcpbo.customer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rhbgroup.dcp.adaptor.BusinessAdaptor;
import com.rhbgroup.dcp.data.repository.ProfileRepository;
import com.rhbgroup.dcp.uber.deposits.casa.bizlogic.GetTermDepositDetailsLogic;
import com.rhbgroup.dcpbo.customer.audit.collector.AdditionalDataHolder;
import com.rhbgroup.dcpbo.customer.audit.collector.AuditAdditionalDataRetriever;
import com.rhbgroup.dcpbo.customer.audit.collector.DeleteDeviceAdditionalDataRetriever;
import com.rhbgroup.dcpbo.customer.audit.collector.DuitnowAdditionalDataRetriever;
import com.rhbgroup.dcpbo.customer.audit.collector.McaAdditionalDataRetriever;
import com.rhbgroup.dcpbo.customer.audit.collector.PutProfileStatusAdditionalDataRetriever;
import com.rhbgroup.dcpbo.customer.audit.collector.PutUnlockFacilityAdditionalDataRetriever;
import com.rhbgroup.dcpbo.customer.audit.collector.PutUnblockFacilityAdditionalDataRetriever;
import com.rhbgroup.dcpbo.customer.audit.collector.TermDepositAdditionalDataRetriever;
import com.rhbgroup.dcpbo.customer.audit.collector.UnitTrustAdditionalDataRetriever;
import com.rhbgroup.dcpbo.customer.contract.ViewTransaction;
import com.rhbgroup.dcpbo.customer.repository.BoDepositRepository;
import com.rhbgroup.dcpbo.customer.repository.BoLoanProfileRepository;
import com.rhbgroup.dcpbo.customer.service.AuditEventsFunctionService;
import com.rhbgroup.dcpbo.customer.service.AuditRegistrationService;
import com.rhbgroup.dcpbo.customer.service.BillerService;
import com.rhbgroup.dcpbo.customer.service.DuitnowEnquiryService;
import com.rhbgroup.dcpbo.customer.service.McaTermService;
import com.rhbgroup.dcpbo.customer.service.ResetUserNameService;
import com.rhbgroup.dcpbo.customer.service.ViewDepositService;
import com.rhbgroup.dcpbo.customer.service.ViewTransactionAsb;
import com.rhbgroup.dcpbo.customer.service.impl.AuditEventsFunctionServiceImpl;
import com.rhbgroup.dcpbo.customer.service.impl.AuditRegistrationServiceImpl;
import com.rhbgroup.dcpbo.customer.service.impl.BillerServiceImpl;
import com.rhbgroup.dcpbo.customer.service.impl.DuitnowEnquiryServiceImpl;
import com.rhbgroup.dcpbo.customer.service.impl.McaTermServiceImpl;
import com.rhbgroup.dcpbo.customer.service.impl.ResetUserNameServiceImpl;
import com.rhbgroup.dcpbo.customer.service.impl.ViewHirePurchaseTransactionsLoanServiceImpl;
import com.rhbgroup.dcpbo.customer.service.impl.ViewTermDepositServiceImpl;
import com.rhbgroup.dcpbo.customer.service.impl.ViewTransactionPersonalLoanServiceImpl;

@Configuration
public class ServiceBeanConfiguration {

    @Bean("viewTransactionAsb")
    public ViewTransaction getViewTransactionAsb() {
        return new ViewTransactionAsb();
    }

    @Bean("viewTermDepositService")
    public ViewDepositService getViewTermDepositServiceImpl(BoDepositRepository boDepositRepository,
                                                            GetTermDepositDetailsLogic getTermDepositDetailsLogic,
                                                            ProfileRepository profileRepository) {
        return new ViewTermDepositServiceImpl(boDepositRepository, getTermDepositDetailsLogic, profileRepository);
    }


    @Bean("viewTransactionPersonalLoan")
    public ViewTransaction getViewTransactionPersonalLoan(BoLoanProfileRepository boLoanProfileRepository,
                                                          @Qualifier("personalFinanceTransactionHistoryLogic") BusinessAdaptor personalFinanceTransactionHistoryLogic) {
        return new ViewTransactionPersonalLoanServiceImpl(boLoanProfileRepository, personalFinanceTransactionHistoryLogic);
    }

    @Bean("viewHirePurchaseTransactionLoan")
    public ViewTransaction getHirePurchaseTransactionLoan(BoLoanProfileRepository boLoanProfileRepository,
                                                          @Qualifier("hirePurchaseTransactionsLogic") BusinessAdaptor hirePurchaseTransactionHistoryLogic) {
        return new ViewHirePurchaseTransactionsLoanServiceImpl(boLoanProfileRepository, hirePurchaseTransactionHistoryLogic);
    }

//    @Bean("dcpCustomerAuditService")
//    public DcpCustomerAuditService getDcpCustomerAuditService(DcpAuditCategoryConfigRepository dcpAuditCategoryConfigRepository,
//                                                              DcpAuditRepository dcpAuditRepository,
//                                                              DcpAuditEventConfigRepository dcpAuditEventConfigRepository,
//                                                              BoAuditSummaryConfigRepository boAuditSummaryConfigRepository,
//                                                              DcpAuditFundTransferRepository dcpAuditFundTransferRepository,
//                                                              DcpAuditBillPaymentRepository dcpAuditBillPaymentRepository,
//                                                              DcpAuditMiscRepository dcpAuditMiscRepository,
//                                                              DcpAuditProfileRepository dcpAuditProfileRepository,
//                                                              DcpAuditTopupRepository dcpAuditTopupRepository) {
//        return new DcpCustomerAuditServiceImpl(dcpAuditCategoryConfigRepository, dcpAuditRepository,
//                dcpAuditEventConfigRepository, boAuditSummaryConfigRepository, dcpAuditFundTransferRepository,
//                dcpAuditBillPaymentRepository, dcpAuditMiscRepository, dcpAuditProfileRepository,
//                dcpAuditTopupRepository);
//    }

    @Bean("termDepositAdditionalDataRetriever")
    public AuditAdditionalDataRetriever getTermDepositAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
        return new TermDepositAdditionalDataRetriever(additionalDataHolder);
    }

    @Bean("deleteDeviceAdditionalDataRetriever")
    public AuditAdditionalDataRetriever deleteDeviceAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
        return new DeleteDeviceAdditionalDataRetriever(additionalDataHolder);
    }

    @Bean("putProfileStatusAdditionalDataRetriever")
    public AuditAdditionalDataRetriever putProfileStatusAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
        return new PutProfileStatusAdditionalDataRetriever(additionalDataHolder);
    }

    @Bean("mcaAdditionalDataRetriever")
    public AuditAdditionalDataRetriever getMcaAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
        return new McaAdditionalDataRetriever(additionalDataHolder);
    }

    @Bean("unitTrustAdditionalDataRetriever")
    public AuditAdditionalDataRetriever getUnitTrustAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
        return new UnitTrustAdditionalDataRetriever(additionalDataHolder);
    }

    @Bean(name = "auditEventsFunctionService")
    public AuditEventsFunctionService getAuditEventslisting() {
        return new AuditEventsFunctionServiceImpl();
    }

    @Bean("duitnowEnquiryService")
    public DuitnowEnquiryService getDuitnowEnquiryService( @Qualifier("duitnowSendersLogic") BusinessAdaptor getDuitNowSendersLogic,
                                                          @Qualifier("duitnowEnquiryLogic") BusinessAdaptor getDuitnowEnquiryLogic) {
        return new DuitnowEnquiryServiceImpl(getDuitNowSendersLogic, getDuitnowEnquiryLogic);
    }

    @Bean("mcaTermService")
    public McaTermService getMcaTermService(@Qualifier("mcaTermDetailsLogic") BusinessAdaptor getMcaTermDetailsLogic, 
    		@Qualifier("getMcaTermLogic") BusinessAdaptor getMcaTermLogic) {
        return new McaTermServiceImpl( getMcaTermDetailsLogic, getMcaTermLogic);
    }

    @Bean("duitnowAdditionalDataRetriever")
    public AuditAdditionalDataRetriever getDuitnowAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
        return new DuitnowAdditionalDataRetriever(additionalDataHolder);
    }
    
    @Bean("billerService")
	public BillerService getBillerService() {
		return new BillerServiceImpl();
	}
    
    @Bean("resetUserNameService")
	public ResetUserNameService getResetUserNameService() {
		return new ResetUserNameServiceImpl();
	}
    
    @Bean("auditRegistrationService")
  	public AuditRegistrationService getAuditRegistrationService() {
  		return new AuditRegistrationServiceImpl();
  	}
    
    @Bean("putUnlockFacilityAdditionalDataRetriever")
    public AuditAdditionalDataRetriever putUnlockFacilityAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
    	return new PutUnlockFacilityAdditionalDataRetriever(additionalDataHolder);
    }
    
    @Bean("putUnblockFacilityAdditionalDataRetriever")
    public AuditAdditionalDataRetriever putUnblockFacilityAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
    	return new PutUnblockFacilityAdditionalDataRetriever(additionalDataHolder);
    }
    
}
