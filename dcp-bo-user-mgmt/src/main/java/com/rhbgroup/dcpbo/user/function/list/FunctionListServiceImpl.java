package com.rhbgroup.dcpbo.user.function.list;

import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.function.dto.FunctionVo;
import com.rhbgroup.dcpbo.user.function.dto.ModuleListVo;
import com.rhbgroup.dcpbo.user.function.dto.ModuleVo;
import com.rhbgroup.dcpbo.user.function.repository.FunctionRepository;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.*;

public class FunctionListServiceImpl implements FunctionListService {

	private static Logger logger = LogManager.getLogger(FunctionListServiceImpl.class);

	@Autowired
	FunctionRepository functionRepository;

	@Override
	public BoData getFunctionList(String keyword) {
		List<ConfigFunction> functionList = functionRepository.findByFunctionNameContainingIgnoreCaseOrderByModuleId(keyword);

		Map<Integer, ModuleVo> moduleVoMap = new HashMap<>();

		if (functionList != null && functionList.size() > 0) {
			for (ConfigFunction function : functionList) {
				FunctionVo functionVo = new FunctionVo();
				functionVo.setFunctionId(function.getId());
				functionVo.setFunctionName(function.getFunctionName());

				logger.debug(String.format("functionVo: %s", functionVo));

				addModuleIntoMap(moduleVoMap, function, functionVo);
			}
		} else {
            throw new CommonException("50008", "No matching function", HttpStatus.NOT_FOUND);
		}

		List<ModuleVo> moduleVos = convertMapIntoList(moduleVoMap);
		ModuleListVo moduleListVo = new ModuleListVo();
		moduleListVo.setModule(moduleVos);

		return moduleListVo;

	}

	private List<ModuleVo> convertMapIntoList(Map<Integer, ModuleVo> moduleVoMap) {
		List<ModuleVo> moduleVos = new ArrayList<>();
		Iterator iterator = moduleVoMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry pair = (Map.Entry) iterator.next();
			moduleVos.add((ModuleVo) pair.getValue());
		}
		return moduleVos;
	}

	private void addModuleIntoMap(Map<Integer, ModuleVo> moduleVoMap, ConfigFunction function, FunctionVo functionVo) {
		Integer moduleId = function.getModule().getId();

		//Check if Module already exist in Map.
		if (!moduleVoMap.containsKey(moduleId)) {
			logger.info(String.format("Module Id [%s] does not exist yet.", moduleId));

			List<FunctionVo> functionVoList = new ArrayList<>();
			functionVoList.add(functionVo);

			ModuleVo moduleVo = new ModuleVo();
			moduleVo.setModuleId(function.getModule().getId());
			moduleVo.setModuleName(function.getModule().getModuleName());
			moduleVo.setFunction(functionVoList);

			logger.debug(String.format("moduleVo: %s", moduleVo));
			moduleVoMap.put(moduleVo.getModuleId(), moduleVo);

		} else {
			logger.info(String.format("Module Id [%s] exist.", moduleId));
			ModuleVo moduleVo = moduleVoMap.get(moduleId);
			moduleVoMap.get(moduleId).getFunction().add(functionVo);
		}
	}

}
