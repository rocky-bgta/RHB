package com.rhbgroup.dcpbo.system;

import com.rhbgroup.dcp.estatement.bizlogic.EPullAutoEnrollmentLogic;
import com.rhbgroup.dcpbo.system.epullenrollment.service.EpullEnrollmentService;
import com.rhbgroup.dcpbo.system.epullenrollment.service.EpullEnrollmentServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaInterestRateLogic;
import com.rhbgroup.dcp.deposits.mca.bizlogic.GetMcaRateLogic;
import com.rhbgroup.dcp.invest.bizlogic.FPXEnquiryAEMessageLogic;
import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.audit.AuditAdditionalDataRetriever;
import com.rhbgroup.dcpbo.system.common.BOAuditAdditionalDataRetriever;
import com.rhbgroup.dcpbo.system.downtime.service.BankService;
import com.rhbgroup.dcpbo.system.downtime.service.DowntimeAdhocService;
import com.rhbgroup.dcpbo.system.downtime.service.ServerDateService;
import com.rhbgroup.dcpbo.system.downtime.service.WorkflowDowntimeService;
import com.rhbgroup.dcpbo.system.downtime.service.impl.BankServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.service.impl.DowntimeAdhocServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.service.impl.ServerDateServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.service.impl.WorkflowDowntimeServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.DowntimeAdhocWhitelistService;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.WorkflowDowntimeAdhocWhitelistService;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.impl.DowntimeAdhocWhitelistServiceImpl;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.impl.WorkflowDowntimeAdhocWhitelistServiceImpl;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.ExtractExchangeRateService;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.ExtractInterestExchangeRateService;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.ExtractExchangeRateServiceImpl;
import com.rhbgroup.dcpbo.system.termDeposit.service.TermDepositPlacementService;
import com.rhbgroup.dcpbo.system.termDeposit.service.impl.TermDepositPlacementServiceImpl;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.impl.ExtractInterestExchangeRateServiceImpl;

@Configuration
public class ServiceBeanConfiguration {

	@Bean("boAuditAdditionalDataRetriever")
	public AuditAdditionalDataRetriever getBOAuditAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
		return new BOAuditAdditionalDataRetriever(additionalDataHolder);
	}


	@Bean("extractExchangeRateService")
	public ExtractExchangeRateService getExtractExchangeRateService(
			GetMcaRateLogic getMcaRateLogic) {
		return new ExtractExchangeRateServiceImpl(getMcaRateLogic);
	}
	
	@Bean("downtimeAdhocService")
	public DowntimeAdhocService getDowntimeAdhocService() {
		return new DowntimeAdhocServiceImpl();
	}
	
	@Bean("serverDateService")
	public ServerDateService getServerDateService() {
		return new ServerDateServiceImpl();
	}
	
	@Bean("workflowDowntimeService")
	public WorkflowDowntimeService getWorkflowDowntimeService() {
		return new WorkflowDowntimeServiceImpl();
	}
	
	@Bean("downtimeAdhocWhitelistService")
	public DowntimeAdhocWhitelistService getDowntimeAdhocWhitelistService() {
		return new DowntimeAdhocWhitelistServiceImpl();
	}
	
	@Bean("workflowDowntimeAdhocWhitelistService")
	public WorkflowDowntimeAdhocWhitelistService getWorkflowDowntimeAdhocWhitelistService() {
		return new WorkflowDowntimeAdhocWhitelistServiceImpl();
	}
	
	@Bean("bankService")
	public BankService getBankService() {
		return new BankServiceImpl();
	}
	
	@Bean("termDepositPlacementService")
	public TermDepositPlacementService getTermDepositPlacementService(FPXEnquiryAEMessageLogic fPXEnquiryAEMessageLogic) {
		return new TermDepositPlacementServiceImpl(fPXEnquiryAEMessageLogic);
	}
		
	@Bean("extractInterestExchangeRateService")
	public ExtractInterestExchangeRateService getExtractInterestExchangeRateService(
			GetMcaInterestRateLogic getMcaInterestRateLogic) {
		return new ExtractInterestExchangeRateServiceImpl(getMcaInterestRateLogic);
	}

	@Bean("epullEnrollmentService")
	public EpullEnrollmentService getEpullEnrollmentService(EPullAutoEnrollmentLogic ePullAutoEnrollmentLogic) {
		return new EpullEnrollmentServiceImpl(ePullAutoEnrollmentLogic);
	}
}
