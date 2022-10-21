package com.rhbgroup.dcpbo.user.workflow.overview;

import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.ConfigModule;
import com.rhbgroup.dcpbo.user.config.WorkflowConfig;
import com.rhbgroup.dcpbo.user.enums.MaintenanceActionType;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import com.rhbgroup.dcpbo.user.common.UsergroupAccessDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WorkflowOverviewService {

	private UserUsergroupRepository userUsergroupRepository;
	private UsergroupAccessRepository usergroupAccessRepository;
	private ApprovalRepository approvalRepository;
	private ConfigFunctionRepository configFunctionRepository;
	private ConfigModuleRepository configModuleRepository;
	private WorkflowConfig workflowConfig;

	private static Logger logger = LogManager.getLogger(WorkflowOverviewService.class);

	public WorkflowOverviewService(UserUsergroupRepository userUsergroupRepository, UsergroupAccessRepository usergroupAccessRepository, ApprovalRepository approvalRepository, ConfigFunctionRepository configFunctionRepository, ConfigModuleRepository configModuleRepository, WorkflowConfig workflowConfig) {
		this.userUsergroupRepository = userUsergroupRepository;
		this.usergroupAccessRepository = usergroupAccessRepository;
		this.approvalRepository = approvalRepository;
		this.configFunctionRepository = configFunctionRepository;
		this.configModuleRepository = configModuleRepository;
		this.workflowConfig = workflowConfig;
	}

	public WorkflowOverview getWorkflowOverviewService(int userId) {
		List<Integer> userGroupIdList = new ArrayList<>();

		//Get functions that are accessible by user
		userGroupIdList = userUsergroupRepository.findUserGroupIdListByUserId(userId);
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		List<UsergroupAccessDTO> usergroupAccessList = new ArrayList<>();
		List<Object[]> usergroupDtoList = new ArrayList<>();
		if (userGroupIdList.size() == 0) {
			logger.warn("No usergroup id found for user id " + userId);
		} else {
				usergroupDtoList = usergroupAccessRepository.findByUserGroupIdList(userGroupIdList);
			if (usergroupAccessList.size() == 0) {
				logger.warn("No usergroup access found for user groud id " + userGroupIdList);
			}
		}

		for (Object[] usergroupDTO: usergroupDtoList){
			UsergroupAccessDTO usergroupAccessDTO = new UsergroupAccessDTO();
			usergroupAccessDTO.setUserGroupId(String.valueOf(usergroupDTO[0]));
			usergroupAccessDTO.setFunctionId(String.valueOf(usergroupDTO[1]));
			usergroupAccessDTO.setModuleId(String.valueOf(usergroupDTO[2]));
			usergroupAccessDTO.setScopeId(String.valueOf(usergroupDTO[3]));
			usergroupAccessDTO.setAccessType(String.valueOf(usergroupDTO[4]));
			usergroupAccessDTO.setStatus(String.valueOf(usergroupDTO[5]));
			usergroupAccessDTO.setCreatedTime(String.valueOf(usergroupDTO[6]));
			usergroupAccessDTO.setCreatedBy(String.valueOf(usergroupDTO[7]));
			usergroupAccessDTO.setUpdatedTime(String.valueOf(usergroupDTO[8]));
			usergroupAccessDTO.setUpdatedBy(String.valueOf(usergroupDTO[9]));
			usergroupAccessList.add(usergroupAccessDTO);
		}


		//List functions and details
		String moduleIdCounter = "0";
		Boolean firstModule = true;
		Integer functionIdCounter =0;
		Boolean firstFunction = true;
		WorkflowOverview workflowOverview = new WorkflowOverview();
		WorkflowOverviewModule workflowOverviewModule = new WorkflowOverviewModule();
		List<WorkflowOverviewModule> workflowOverviewModuleList = new ArrayList<>();
		List<WorkflowOverviewModuleFunction> workflowOverviewModuleFunctionList = new ArrayList<>();
		WorkflowOverviewModuleFunction workflowOverviewModuleFunction = new WorkflowOverviewModuleFunction();

		//Creating response structure
		String moduleId = "0";
		String moduleName = "";
		for (UsergroupAccessDTO usergroupAccess : usergroupAccessList) {
			if (functionIdCounter == 0 || !String.valueOf(functionIdCounter).equals(usergroupAccess.getFunctionId())){

				moduleId = usergroupAccess.getModuleId();
				if (moduleIdCounter.equals("0")) {
					moduleIdCounter = usergroupAccess.getModuleId();

				}
				if (!moduleId.equals(moduleIdCounter)) {
					workflowOverviewModule.setFunction(workflowOverviewModuleFunctionList);
					workflowOverviewModuleList.add(workflowOverviewModule);
					workflowOverviewModule = new WorkflowOverviewModule();
					workflowOverviewModuleFunctionList = new ArrayList<>();
					moduleIdCounter = moduleId;
				}
				try {
					ConfigModule configModule = configModuleRepository.findOne(Integer.valueOf(moduleId));
					if (configModule != null) {
						moduleName = configModule.getModuleName();
					}
				}catch (Exception e){
					logger.warn("Module name not found for invalid module id " + moduleId,e);
				}
				workflowOverviewModule.setModuleId(String.valueOf(moduleId));
				workflowOverviewModule.setModuleName(moduleName);

				String approvedCount = "0", rejectedCount = "0", pendingCount = "0";
				Integer functionId = Integer.valueOf(usergroupAccess.getFunctionId());

				List<Object[]> statusCountList;

				statusCountList = approvalRepository.findCountByFunctionIdAndStatusNew(functionId);

				for (Object[] status : statusCountList) {
					System.out.print(String.valueOf(status));
					String key = String.valueOf(status[0]);
					String value = String.valueOf(status[1]);

					if (key.equals(MaintenanceActionType.STATUS_PENDING_APPROVAL.getValue())) {
						pendingCount = value;
					} else if (key.equals(MaintenanceActionType.STATUS_APPROVED.getValue())) {
						approvedCount = value;
					} else if (key.equals(MaintenanceActionType.STATUS_REJECTED.getValue())) {
						rejectedCount = value;
					}
				}


				ConfigFunction configFunction = new ConfigFunction();
				configFunction = configFunctionRepository.findOne(functionId);

				workflowOverviewModuleFunction = new WorkflowOverviewModuleFunction();
				workflowOverviewModuleFunction = modelMapper.map(configFunction, WorkflowOverviewModuleFunction.class);
				workflowOverviewModuleFunction.setFunctionId(functionId);
				workflowOverviewModuleFunction.setApprovedCount(approvedCount);
				workflowOverviewModuleFunction.setPendingCount(pendingCount);
				workflowOverviewModuleFunction.setRejectedCount(rejectedCount);
				workflowOverviewModuleFunctionList.add(workflowOverviewModuleFunction);
				functionIdCounter = Integer.valueOf(usergroupAccess.getFunctionId());
			}
		}

		if (!moduleId.equals("0")) {
			try {
				ConfigModule configModule = configModuleRepository.findOne(Integer.valueOf(moduleId));
				if (configModule != null) {
					moduleName = configModule.getModuleName();
				}
			}catch (Exception e){
				logger.warn("Module name not found for invalid module id " + moduleId,e);
			}
			workflowOverviewModule.setModuleId(String.valueOf(moduleId));
			workflowOverviewModule.setModuleName(moduleName);
			workflowOverviewModule.setFunction(workflowOverviewModuleFunctionList);
			workflowOverviewModuleList.add(workflowOverviewModule);
			workflowOverview.setModule(workflowOverviewModuleList);
		}

		if(workflowOverview.getModule() != null){
			workflowConfig.getExclusions().forEach(exclusionItem -> {
				String indModuleName = exclusionItem.getModule();
				workflowOverview.getModule().stream().filter(x -> x.getModuleName().equals(indModuleName)).forEach(module -> {
					module.getFunction().removeIf(function -> function.getFunctionName().equals(exclusionItem.getFunction()));
				});
			});

			workflowOverview.getModule().removeIf(module -> module.getFunction().isEmpty());
		}

		return workflowOverview;
	}
}