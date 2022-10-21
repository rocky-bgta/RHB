package com.rhbgroup.dcpbo.customer;


import com.rhbgroup.dcp.investments.bizlogic.GetUnitTrustDetailsLogic;
import com.rhbgroup.dcp.investments.bizlogic.GetUnitTrustLogic;
import com.rhbgroup.dcpbo.customer.service.KonySubscriptionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.rhbgroup.dcp.creditcards.bizlogic.GetCreditCardDetailsLogic;
import com.rhbgroup.dcp.creditcards.bizlogic.GetCreditCardTransactionsLogic;
import com.rhbgroup.dcp.creditcards.bizlogic.GetCreditCardsLogic;
import com.rhbgroup.dcp.data.repository.CommonRepository;
import com.rhbgroup.dcp.deposits.casa.bizlogic.GetAccountDetailsLogic;
import com.rhbgroup.dcp.deposits.casa.bizlogic.GetCasaDepositsSummaryLogic;
import com.rhbgroup.dcp.deposits.casa.bizlogic.GetDepositTransactionsLogic;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetDepositsMcaTransaction;
import com.rhbgroup.dcp.loans.bizlogic.GetAsbLoanAccountDetailsLogic;
import com.rhbgroup.dcp.loans.bizlogic.GetHirePurchaseAccountDetailsLogic;
import com.rhbgroup.dcp.loans.bizlogic.GetLoansLogic;
import com.rhbgroup.dcp.loans.bizlogic.GetMortgageDetailsLogic;
import com.rhbgroup.dcp.loans.bizlogic.GetMortgageTransactionHistoryLogic;
import com.rhbgroup.dcp.loans.bizlogic.GetPersonalFinanceAccountDetailsLogic;
import com.rhbgroup.dcp.profiles.bizlogic.GetAccountProfilesLogic;
import com.rhbgroup.dcp.uber.asnb.bizlogic.GetAsnbAccountInquiryLogic;
import com.rhbgroup.dcp.uber.asnb.bizlogic.GetAsnbTransactionInquiryLogic;
import com.rhbgroup.dcp.uber.deposits.casa.bizlogic.GetTermDepositsLogic;
import com.rhbgroup.dcpbo.customer.aspect.ApiContext;
import com.rhbgroup.dcpbo.customer.contract.SearchCustomer;
import com.rhbgroup.dcpbo.customer.service.DcpSearchCustomer;

@Configuration
public class BeanConfiguration {

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    ApiContext apiState(){
        return new ApiContext();
    }
    
    @Bean
    public GetCreditCardDetailsLogic getGetCreditCardDetailsLogic() {
    	return new GetCreditCardDetailsLogic();
    }

    @Bean
    public GetAccountDetailsLogic getGetAccountDetailsLogic() {
    	
    	return new GetAccountDetailsLogic();
    }
    
    @Bean
    public GetAsbLoanAccountDetailsLogic getGetAsbLoanAccountDetailsLogic() {
    	return new GetAsbLoanAccountDetailsLogic();
    }
    
    @Bean
    public GetAsnbAccountInquiryLogic getGetAsnbAccountInquiryLogic() {
    	return new GetAsnbAccountInquiryLogic();
    }
    
    @Bean
    public GetAsnbTransactionInquiryLogic getGetAsnbTransactionInquiryLogic() {
    	return new GetAsnbTransactionInquiryLogic();
    }
    
    @Bean
    public GetAccountProfilesLogic getGetAccountProfilesLogic() {
    	return new GetAccountProfilesLogic();
    }
    
    @Bean
    public GetCasaDepositsSummaryLogic getGetCasaDepositsSummaryLogic() {
    	return new GetCasaDepositsSummaryLogic();
    }

    @Bean
    public GetCreditCardsLogic getGetCreditCardsLogic() {
    	return new GetCreditCardsLogic();
    }

    @Bean
    public GetTermDepositsLogic getGetTermDepositsLogic() {
    	return new GetTermDepositsLogic();
    }

    @Bean
    public GetLoansLogic getGetLoansLogic() {
    	return new GetLoansLogic();
    }

    @Bean
    public GetMortgageDetailsLogic getGetMortgageDetailsLogic() {
    	return new GetMortgageDetailsLogic();
    }
    
    @Bean
    public GetHirePurchaseAccountDetailsLogic getGetHirePurchaseAccountDetailsLogic() {
    	return new GetHirePurchaseAccountDetailsLogic();
    }
    
    @Bean
	public GetDepositTransactionsLogic getGetDepositTransactionsLogic() {
    	return new GetDepositTransactionsLogic(); 
    }
    
    @Bean
	public GetCreditCardTransactionsLogic getGetCreditCardTransactionsLogic() {
    	return new GetCreditCardTransactionsLogic(); 
    }

    @Bean(name = "searchCustomer")
    public SearchCustomer getSearchCustomer() {
        return new DcpSearchCustomer();
    }
    
    @Bean
    public GetPersonalFinanceAccountDetailsLogic getPersonalFinanceAccountDetailsLogic() {
    	return new GetPersonalFinanceAccountDetailsLogic();
    }

    @Bean
    public GetMortgageTransactionHistoryLogic getMortgageTransactionHistoryLogic(){
        return new GetMortgageTransactionHistoryLogic();
    }

    @Bean
    public CommonRepository commonRepository(){
        return new CommonRepository();
    }
    
    @Bean
    public GetDepositsMcaTransaction getGetDepositsMcaTransaction() {
    	return new GetDepositsMcaTransaction();
    }

    @Bean
    public GetUnitTrustLogic getUnitTrustLogic() {
        return new GetUnitTrustLogic();
    }

    @Bean
    public GetUnitTrustDetailsLogic getUnitTrustDetailsLogic() { return new GetUnitTrustDetailsLogic(); }

    @Bean
    public KonySubscriptionService getKonySub(){ return new KonySubscriptionService(); }

    @Bean
    public RestTemplate getRestTemplate() { return new RestTemplate(); }

}