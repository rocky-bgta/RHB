package com.rhbgroup.dcpbo.system;

import com.rhbgroup.dcpbo.system.epullenrollment.controller.EpullEnrollmentController;
import com.rhbgroup.dcpbo.system.epullenrollment.service.EpullEnrollmentService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rhbgroup.dcpbo.system.downtime.controller.AdhocController;
import com.rhbgroup.dcpbo.system.downtime.controller.BankController;
import com.rhbgroup.dcpbo.system.downtime.controller.ServerDateController;
import com.rhbgroup.dcpbo.system.downtime.controller.WorkflowDowntimeController;
import com.rhbgroup.dcpbo.system.downtime.service.BankService;
import com.rhbgroup.dcpbo.system.downtime.service.DowntimeAdhocService;
import com.rhbgroup.dcpbo.system.downtime.whitelist.controller.DowntimeAdhocWhitelistController;
import com.rhbgroup.dcpbo.system.downtime.whitelist.controller.WorkflowDowntimeAdhocWhitelistController;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.DowntimeAdhocWhitelistService;
import com.rhbgroup.dcpbo.system.downtime.whitelist.service.WorkflowDowntimeAdhocWhitelistService;
import com.rhbgroup.dcpbo.system.extractexchangerate.controller.ExtractExchangeRateController;
import com.rhbgroup.dcpbo.system.extractexchangerate.controller.ExtractInterestExchangeRateController;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.ExtractExchangeRateService;
import com.rhbgroup.dcpbo.system.termDeposit.controller.TermDepositController;
import com.rhbgroup.dcpbo.system.termDeposit.service.TermDepositPlacementService;
import com.rhbgroup.dcpbo.system.extractexchangerate.service.ExtractInterestExchangeRateService;


@Configuration
public class ControllerBeanConfiguration {

	@Bean(name = "extractExchangeRateController")
	public ExtractExchangeRateController getExtractExchangeRateController(@Qualifier("extractExchangeRateService") ExtractExchangeRateService extractExchangeRateService) {
		return new ExtractExchangeRateController(extractExchangeRateService);
	}
	
	@Bean(name = "adhocController")
	public AdhocController getAdhocController(@Qualifier("downtimeAdhocService") DowntimeAdhocService downtimeAdhocService) {
		return new AdhocController(downtimeAdhocService);
	}
	
	@Bean(name = "serverDateController")
	public ServerDateController getServerDateController() {
		return new ServerDateController();
	}
	
	@Bean(name = "workflowDowntimeController")
	public WorkflowDowntimeController getWorkflowDowntimeController() {
		return new WorkflowDowntimeController();
	}
	
	@Bean(name = "downtimeAdhocWhitelistController")
	public DowntimeAdhocWhitelistController getDowntimeAdhocWhitelistController(@Qualifier("downtimeAdhocWhitelistService")DowntimeAdhocWhitelistService downtimeAdhocWhitelistService) {
		return new DowntimeAdhocWhitelistController(downtimeAdhocWhitelistService);
	}
	
	@Bean(name = "workflowDowntimeAdhocWhitelistController")
	public WorkflowDowntimeAdhocWhitelistController getWorkflowDowntimeAdhocWhitelistController(@Qualifier("workflowDowntimeAdhocWhitelistService") 
		WorkflowDowntimeAdhocWhitelistService workflowDowntimeAdhocWhitelistService) {
		return new WorkflowDowntimeAdhocWhitelistController(workflowDowntimeAdhocWhitelistService);
	}
	
	@Bean(name = "bankController")
	public BankController getBankController(@Qualifier("bankService") BankService bankService) {
		return new BankController(bankService);
	}
	
	@Bean(name = "termDepositController")
	public TermDepositController getTermDepositController(@Qualifier("termDepositPlacementService") TermDepositPlacementService termDepositPlacementService) {
		return new TermDepositController(termDepositPlacementService);
	}

	@Bean(name = "extractInterestExchangeRateController")
	public ExtractInterestExchangeRateController getInterestExtractExchangeRateController(@Qualifier("extractInterestExchangeRateService") ExtractInterestExchangeRateService extractInterestExchangeRateService) {
		return new ExtractInterestExchangeRateController(extractInterestExchangeRateService);
	}

	@Bean(name = "epullEnrollmentController")
	public EpullEnrollmentController getEpullEnrollmentController(@Qualifier("epullEnrollmentService") EpullEnrollmentService epullEnrollmentService) {
		return new EpullEnrollmentController(epullEnrollmentService);
	}

}
