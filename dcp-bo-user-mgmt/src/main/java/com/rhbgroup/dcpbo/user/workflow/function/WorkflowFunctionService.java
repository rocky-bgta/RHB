package com.rhbgroup.dcpbo.user.workflow.function;

import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.Approval;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class WorkflowFunctionService {

	@Autowired
	ApprovalRepository approvalRepository;
	@Autowired
	ConfigFunctionRepository configFunctionRepository;
	@Autowired
	UserRepository userRepository;

	private static Logger logger = LogManager.getLogger(WorkflowFunctionService.class);

	//Retrieve transaction of type payment with ref id
	public WorkflowFunction getWorkflowFunctionService(int functionId, String status) {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		//- 1 -get workflow approvals for functionId based on status
		List<Approval> approvalList = new ArrayList<>();
		if (status.equals(""))
			approvalList = approvalRepository.findByFunctionId(functionId);
		else
			approvalList = approvalRepository.findByFunctionIdAndStatus(functionId, status);

		if (approvalList.size() == 0) {
			logger.warn("Approval not found for functionId " + functionId);
		}
		List<WorkflowFunctionFunctionWorkflow> workflowFunctionFunctionWorkflowList = new ArrayList<>();
		WorkflowFunctionFunction workflowFunctionFunction = new WorkflowFunctionFunction();

		//- 2 -get name of creator for the workflow usergroup
		for (Approval approval : approvalList) {
			WorkflowFunctionFunctionWorkflow workflowFunctionFunctionWorkflow = new WorkflowFunctionFunctionWorkflow();
			workflowFunctionFunctionWorkflow = modelMapper.map(approval, WorkflowFunctionFunctionWorkflow.class);
			workflowFunctionFunctionWorkflow.setApprovalId(String.valueOf(approval.getId()));
			Integer approvalId = approval.getCreatorId();
			String name = "";
			name = userRepository.findNameById(approvalId);
			if (name.equals(""))
				logger.warn("Name not found for creator " + approvalId);
			workflowFunctionFunctionWorkflow.setName(name);
			workflowFunctionFunctionWorkflowList.add(workflowFunctionFunctionWorkflow);
		}
		workflowFunctionFunction.setWorkflow(workflowFunctionFunctionWorkflowList);
		workflowFunctionFunction.setFunctionId(String.valueOf(functionId));

		//- 3 -get name of the workflow usergroup
		ConfigFunction configFunction = configFunctionRepository.findOne(Integer.valueOf(functionId));
		if (configFunction == null)
			logger.warn("Config Function not found for the usergroup " + functionId);
		workflowFunctionFunction.setFunctionName(configFunction.getFunctionName());

		WorkflowFunction workflowFunction = new WorkflowFunction();
		workflowFunction.setFunction(workflowFunctionFunction);

		return workflowFunction;
	}
}