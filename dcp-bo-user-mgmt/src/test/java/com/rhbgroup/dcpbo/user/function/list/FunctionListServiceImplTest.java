package com.rhbgroup.dcpbo.user.function.list;


import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.function.dto.ModuleListVo;
import com.rhbgroup.dcpbo.user.function.model.bo.Module;
import com.rhbgroup.dcpbo.user.function.repository.FunctionRepository;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { FunctionListServiceImplTest.class, FunctionListServiceImpl.class })
public class FunctionListServiceImplTest {

	private static Logger logger = LogManager.getLogger(FunctionListServiceImplTest.class);

	@Autowired
	FunctionListService functionListService;

	@MockBean
	FunctionRepository functionRepositoryMock;

	@Test
	public void testFunctionListServiceImpl() {
		logger.debug("testFunctionListServiceImpl()");
		final String KEYWORD = "use";

		Module module1 = new Module();
		module1.setId(1);
		module1.setModuleName("Workflow");
		Module module2 = new Module();
		module2.setId(2);
		module2.setModuleName("User Management");


		ConfigFunction configFunction1 = new ConfigFunction();
		configFunction1.setId(1);
		configFunction1.setFunctionName("User Group");
		configFunction1.setModule(module1);

		ConfigFunction configFunction2 = new ConfigFunction();
		configFunction2.setId(2);
		configFunction2.setFunctionName("Activities");
		configFunction2.setModule(module2);

		ConfigFunction configFunction3 = new ConfigFunction();
		configFunction3.setId(3);
		configFunction3.setFunctionName("Favourite");
		configFunction3.setModule(module1);

		ConfigFunction configFunction4 = new ConfigFunction();
		configFunction4.setId(4);
		configFunction4.setFunctionName("Device");
		configFunction4.setModule(module2);

		List<ConfigFunction> configFunctions = new ArrayList<>();
		configFunctions.add(configFunction1);
		configFunctions.add(configFunction2);
		configFunctions.add(configFunction3);
		configFunctions.add(configFunction4);

		when(functionRepositoryMock.findByFunctionNameContainingIgnoreCaseOrderByModuleId(KEYWORD)).thenReturn(configFunctions);

		ModuleListVo moduleListVo = (ModuleListVo) functionListService.getFunctionList(KEYWORD);

		assertNotNull(moduleListVo);
		assertEquals(2, moduleListVo.getModule().size());

	}

	@Test(expected = CommonException.class)
	public void testFunctionListServiceImpl_NoMatching() {
		logger.debug("testFunctionListServiceImpl_NoMatching()");
		final String KEYWORD = "use";

		List<ConfigFunction> configFunctions = new ArrayList<>();

		when(functionRepositoryMock.findByFunctionNameContainingIgnoreCaseOrderByModuleId(KEYWORD)).thenReturn(configFunctions);

		ModuleListVo moduleListVo = (ModuleListVo) functionListService.getFunctionList(KEYWORD);

		assertNotNull(moduleListVo);
	}

	@Test(expected = CommonException.class)
	public void testFunctionListServiceImpl_GenericException() {
		logger.debug("testFunctionListServiceImpl_NoMatching()");
		final String KEYWORD = "use";

		when(functionRepositoryMock.findByFunctionNameContainingIgnoreCaseOrderByModuleId(KEYWORD)).thenReturn(null);

		ModuleListVo moduleListVo = (ModuleListVo) functionListService.getFunctionList(KEYWORD);

		assertNull(moduleListVo);
	}
}
