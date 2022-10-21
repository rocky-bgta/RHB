package com.rhbgroup.dcpbo.user;

import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.config.WorkflowConfig;
import com.rhbgroup.dcpbo.user.common.KonySubscriptionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rhbgroup.dcpbo.common.audit.AdditionalDataHolder;
import com.rhbgroup.dcpbo.common.audit.AuditAdditionalDataRetriever;
import com.rhbgroup.dcpbo.user.create.UserFunctionService;
import com.rhbgroup.dcpbo.user.create.UserFunctionServiceImpl;
import com.rhbgroup.dcpbo.user.function.list.FunctionListService;
import com.rhbgroup.dcpbo.user.function.list.FunctionListServiceImpl;
import com.rhbgroup.dcpbo.user.info.InfoService;
import com.rhbgroup.dcpbo.user.info.InfoServiceImpl;
import com.rhbgroup.dcpbo.user.search.SearchService;
import com.rhbgroup.dcpbo.user.search.SearchServiceImpl;
import com.rhbgroup.dcpbo.user.useraccess.UsergroupFunctionService;
import com.rhbgroup.dcpbo.user.useraccess.UsergroupFunctionServiceImpl;
import com.rhbgroup.dcpbo.user.usergroup.UsergroupAddService;
import com.rhbgroup.dcpbo.user.usergroup.list.UsergroupListService;
import com.rhbgroup.dcpbo.user.usergroup.list.UsergroupListServiceImpl;
import com.rhbgroup.dcpbo.user.usergroupdelete.UsergroupDeleteFunctionService;
import com.rhbgroup.dcpbo.user.usergroupdelete.UsergroupDeleteFunctionServiceImpl;
import com.rhbgroup.dcpbo.user.usergroupupdate.UsergroupUpdateFunctionService;
import com.rhbgroup.dcpbo.user.usergroupupdate.UsergroupUpdateFunctionServiceImpl;
import com.rhbgroup.dcpbo.user.workflow.device.WorkflowDeviceService;
import com.rhbgroup.dcpbo.user.workflow.function.WorkflowFunctionService;
import com.rhbgroup.dcpbo.user.workflow.function.device.WorkflowFunctionDeviceApprovalService;
import com.rhbgroup.dcpbo.user.workflow.function.device.WorkflowFunctionDeviceApprovalServiceImpl;
import com.rhbgroup.dcpbo.user.workflow.overview.WorkflowOverviewService;
import com.rhbgroup.dcpbo.user.workflow.rejection.WorkflowRejectionAdditionalDataRetriever;
import com.rhbgroup.dcpbo.user.workflow.rejection.WorkflowRejectionService;
import com.rhbgroup.dcpbo.user.workflow.user.WFUserApprovalService;
import com.rhbgroup.dcpbo.user.workflow.user.WFUserApprovalServiceImpl;
import com.rhbgroup.dcpbo.user.workflow.user.delete.WorkflowUserDeleteService;
import com.rhbgroup.dcpbo.user.workflow.user.delete.WorkflowUserDeleteServiceImpl;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupService;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupServiceImpl;


@Configuration
public class ServiceBeanConfiguration {

	@Bean(name = "infoService")
	public InfoService getInfoService(UserRepository userRepository, DirectoryServerRepository directoryServerRepository) {
		return new InfoServiceImpl(userRepository, directoryServerRepository);
	}

	@Bean(name = "searchService")
	public SearchService getSearchService(
			UserRepository userRepository,
			UserGroupRepository userGroupRepository,
			UserUsergroupRepository userUsergroupRepository,
			ConfigDepartmentRepository departmentRepository,
			BoRepositoryHelper boRepositoryHelper) {
		return new SearchServiceImpl(userRepository, userGroupRepository, userUsergroupRepository, departmentRepository, boRepositoryHelper);
	}
	
	@Bean(name = "workflowFunctionDeviceApprovalService")
	public WorkflowFunctionDeviceApprovalService getWorkflowFunctionDeviceApprovalService(
			ApprovalRepository approvalRepository, ApprovalDeviceRepository approvalDeviceRepository,
			UserRepository userRepository, DeviceProfileRepository deviceProfileRepository,
			UserProfileRepository userProfileRepository) {
		return new WorkflowFunctionDeviceApprovalServiceImpl(approvalRepository, approvalDeviceRepository,
				userRepository, deviceProfileRepository, userProfileRepository);
	}

	@Bean(name = "usergroupListService")
	public UsergroupListService getUsergroupListService() {
		return new UsergroupListServiceImpl();
	}

	@Bean(name = "workflowRejectionService")
	public WorkflowRejectionService getWorkflowRejectionService() {
		return new WorkflowRejectionService();
	}

	@Bean(name = "workflowOverviewService")
	public WorkflowOverviewService getWorkflowOverview(UserUsergroupRepository userUsergroupRepository,
                                                       UsergroupAccessRepository usergroupAccessRepository,
                                                       ApprovalRepository approvalRepository,
                                                       ConfigFunctionRepository configFunctionRepository,
                                                       ConfigModuleRepository configModuleRepository,
                                                       WorkflowConfig workflowConfig) {
		return new WorkflowOverviewService(
				userUsergroupRepository,
				usergroupAccessRepository,
				approvalRepository,
				configFunctionRepository,
				configModuleRepository,
                workflowConfig
				);
	}

	@Bean(name = "userFunctionService")
	public UserFunctionService getUserFunctionService() {
		return new UserFunctionServiceImpl();
	}

	@Bean(name = "functionListService")
	public FunctionListService getFunctionListService() {
		return new FunctionListServiceImpl();
	}

	@Bean(name = "wfUserApprovalService")
	public WFUserApprovalService getWFUserApprovalService() {
		return new WFUserApprovalServiceImpl();
	}

	@Bean(name = "workflowUserDeleteService")
	public WorkflowUserDeleteService getWorkflowUserDeleteService() {
		return new WorkflowUserDeleteServiceImpl();
	}

	@Bean(name = "workflowFunctionService")
	public WorkflowFunctionService getWorkflowFunction() {
		return new WorkflowFunctionService();
	}

	@Bean(name = "wfUserGroupService")
	public WFUserGroupService getWFUserGroupService() {
		return new WFUserGroupServiceImpl();
	}

	@Bean("workflowRejectionAdditionalDataRetriever")
	public AuditAdditionalDataRetriever getWorkflowRejectionAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
		return new WorkflowRejectionAdditionalDataRetriever(additionalDataHolder);
	}

	@Bean("boAuditAdditionalDataRetriever")
	public AuditAdditionalDataRetriever getBOAuditAdditionalDataRetriever(AdditionalDataHolder additionalDataHolder) {
		return new BOAuditAdditionalDataRetriever(additionalDataHolder);
	}

	@Bean(name = "usergroupUpdateFunctionService")
	public UsergroupUpdateFunctionService getUsergroupUpdateFunctionService() {
		return new UsergroupUpdateFunctionServiceImpl();
	}

	@Bean(name = "userGroupAddService")
	public UsergroupAddService postUsergroupService() {
		return new UsergroupAddService();
	}

	@Bean(name = "usergroupFunctionService")
	public UsergroupFunctionService getUserGroupFunctionService() {
		return new UsergroupFunctionServiceImpl();
	}

	@Bean(name = "usergroupDeleteFunctionService")
	public UsergroupDeleteFunctionService getUsergroupDeleteFunctionService() {
		return new UsergroupDeleteFunctionServiceImpl();
	}

    @Bean(name = "workflowDeviceService")
    public WorkflowDeviceService getWorkflowDeviceService() { return new WorkflowDeviceService(); }

	@Bean(name = "boRepositoryHelper")
	public BoRepositoryHelper getBoRepositoryHelper() { return new BoRepositoryHelper(); }

	@Bean
	public KonySubscriptionService getKonySub(){ return new KonySubscriptionService(); }
}
