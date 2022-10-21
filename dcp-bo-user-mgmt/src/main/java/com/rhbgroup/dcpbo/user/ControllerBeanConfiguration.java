package com.rhbgroup.dcpbo.user;

import com.rhbgroup.dcpbo.user.usergroupupdate.UsergroupUpdateFunctionController;
import com.rhbgroup.dcpbo.user.usergroupupdate.UsergroupUpdateFunctionService;
import com.rhbgroup.dcpbo.user.useraccess.UsergroupFunctionService;
import com.rhbgroup.dcpbo.user.usergroup.UsergroupAddService;
import com.rhbgroup.dcpbo.user.usergroup.UsergroupController;
import com.rhbgroup.dcpbo.user.useraccess.UsergroupFunctionController;
import com.rhbgroup.dcpbo.user.usergroupdelete.UsergroupDeleteFunctionController;
import com.rhbgroup.dcpbo.user.usergroupdelete.UsergroupDeleteFunctionService;
import com.rhbgroup.dcpbo.user.workflow.device.WorkflowDeviceController;
import com.rhbgroup.dcpbo.user.workflow.device.WorkflowDeviceService;
import com.rhbgroup.dcpbo.user.workflow.function.WorkflowFunctionController;
import com.rhbgroup.dcpbo.user.workflow.function.WorkflowFunctionService;
import com.rhbgroup.dcpbo.user.workflow.function.device.WorkflowFunctionDeviceApprovalService;
import com.rhbgroup.dcpbo.user.workflow.function.device.WorkflowFunctionDeviceController;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rhbgroup.dcpbo.user.create.UserFunctionController;
import com.rhbgroup.dcpbo.user.create.UserFunctionService;
import com.rhbgroup.dcpbo.user.function.FunctionController;
import com.rhbgroup.dcpbo.user.function.list.FunctionListService;
import com.rhbgroup.dcpbo.user.info.InfoController;
import com.rhbgroup.dcpbo.user.info.InfoService;
import com.rhbgroup.dcpbo.user.search.SearchController;
import com.rhbgroup.dcpbo.user.search.SearchService;
import com.rhbgroup.dcpbo.user.usergroup.list.UsergroupListService;
import com.rhbgroup.dcpbo.user.workflow.overview.WorkflowOverviewController;
import com.rhbgroup.dcpbo.user.workflow.overview.WorkflowOverviewService;
import com.rhbgroup.dcpbo.user.workflow.rejection.WorkflowRejectionController;
import com.rhbgroup.dcpbo.user.workflow.rejection.WorkflowRejectionService;
import com.rhbgroup.dcpbo.user.workflow.user.WFUserApprovalController;
import com.rhbgroup.dcpbo.user.workflow.user.WFUserApprovalService;
import com.rhbgroup.dcpbo.user.workflow.user.WorkflowUserController;
import com.rhbgroup.dcpbo.user.workflow.user.delete.WorkflowUserDeleteService;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupApprovalController;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupController;
import com.rhbgroup.dcpbo.user.workflow.usergroup.WFUserGroupService;


@Configuration
public class ControllerBeanConfiguration {
	@Bean(name = "infoController")
	public InfoController getInfoController(@Qualifier("infoService") InfoService infoService) {
		return new InfoController(infoService);
	}

	@Bean(name = "searchController")
	public SearchController getSearchController(@Qualifier("searchService") SearchService searchService) {
		return new SearchController(searchService);
	}

	@Bean(name = "workflowFunctionDeviceController")
	public WorkflowFunctionDeviceController getWorkflowFunctionDeviceController(
			@Qualifier("workflowFunctionDeviceApprovalService") WorkflowFunctionDeviceApprovalService workflowFunctionDeviceApprovalService) {
		return new WorkflowFunctionDeviceController(workflowFunctionDeviceApprovalService);
	}

	@Bean(name = "workflowRejectionController")
	public WorkflowRejectionController getWorkflowRejectionController(@Qualifier("workflowRejectionService") WorkflowRejectionService workflowRejectionService) {
		return new WorkflowRejectionController(workflowRejectionService);
	}

	@Bean(name = "workflowFunctionController")
	public WorkflowFunctionController getWorkflowFunctionController(
			@Qualifier("workflowFunctionService") WorkflowFunctionService workflowFunctionService) {
		return new WorkflowFunctionController(workflowFunctionService);
	}

	@Bean(name = "functionController")
	public FunctionController getFunctionController(
			@Qualifier("functionListService") FunctionListService functionListService) {
		return new FunctionController(functionListService);
	}

	@Bean(name = "userFunctionController")
	public UserFunctionController createBoUserController(
			@Qualifier("userFunctionService") UserFunctionService userFunctionService) {
		return new UserFunctionController(userFunctionService);
	}

	@Bean(name = "workflowUserController")
	public WorkflowUserController getWorkflowUserController(
			@Qualifier("workflowUserDeleteService") WorkflowUserDeleteService workflowUserDeleteService,
			@Qualifier("wfUserApprovalService") WFUserApprovalService wfUserApprovalService) {
		return new WorkflowUserController(workflowUserDeleteService, wfUserApprovalService);
	}

	@Bean(name = "wfUserApprovalController")
	public WFUserApprovalController getWFUserApprovalController(
			@Qualifier("wfUserApprovalService") WFUserApprovalService wfUserApprovalService) {
		return new WFUserApprovalController(wfUserApprovalService);
	}
	
	@Bean(name = "wfUserGroupApprovalController")
	public WFUserGroupApprovalController getWFUserGroupApprovalController(
			@Qualifier("wfUserGroupService") WFUserGroupService wfUserGroupService) {
		return new WFUserGroupApprovalController(wfUserGroupService);
	}

	@Bean(name = "workflowOverviewController")
	public WorkflowOverviewController getWorkflowOverviewController(@Qualifier("workflowOverviewService") WorkflowOverviewService workflowOverviewService) {
		return new WorkflowOverviewController(workflowOverviewService);
	}

	@Bean(name = "wfUserGroupController")
	public WFUserGroupController getWFUserGroupController(
			@Qualifier("wfUserGroupService") WFUserGroupService wfUserGroupService) {
		return new WFUserGroupController(wfUserGroupService);
	}

	@Bean(name = "usergroupUpdateFunctionController")
	public UsergroupUpdateFunctionController getUsergroupUpdateFunctionController(
			@Qualifier("usergroupUpdateFunctionService") UsergroupUpdateFunctionService usergroupUpdateFunctionService) {
		return new UsergroupUpdateFunctionController(usergroupUpdateFunctionService);
	}

	@Bean(name = "usergroupListController")
	public UsergroupController getUsergroupController(
			@Qualifier("usergroupListService") UsergroupListService usergroupListService,
			@Qualifier("userGroupAddService") UsergroupAddService userGroupAddService) {
		return new UsergroupController(userGroupAddService,
					usergroupListService);
	}

	@Bean(name = "usergroupFunctionController")
	public UsergroupFunctionController getUsergroupFunctionController(
			@Qualifier("usergroupFunctionService") UsergroupFunctionService usergroupFunctionService){
		return new UsergroupFunctionController(usergroupFunctionService);
	}

    @Bean(name = "usergroupDeleteFunctionController")
    public UsergroupDeleteFunctionController getUsergroupDeleteFunctionController(
            @Qualifier("usergroupDeleteFunctionService") UsergroupDeleteFunctionService usergroupDeleteFunctionService) {
        return new UsergroupDeleteFunctionController(usergroupDeleteFunctionService);
    }

    @Bean(name = "workflowDeviceController")
	public WorkflowDeviceController getWorkflowDeviceController(
	        @Qualifier("workflowDeviceService") WorkflowDeviceService workflowDeviceService){
	    return new WorkflowDeviceController(workflowDeviceService);
    }

}
