package com.rhbgroup.dcpbo.user.function;

import com.rhbgroup.dcpbo.user.function.dto.ModuleListVo;
import com.rhbgroup.dcpbo.user.function.list.FunctionListServiceImpl;
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

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
		FunctionControllerTest.class,
		FunctionController.class,
		FunctionListServiceImpl.class })
public class FunctionControllerTest {
	private static Logger logger = LogManager.getLogger(FunctionControllerTest.class);

	@Autowired
	FunctionListServiceImpl functionListServiceimpl;

	@MockBean
	FunctionRepository functionRepositoryMock;

	@Test
	public void testFunctionController() {
		logger.debug("testFunctionController()");

		final String KEYWORD = "user";

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

		FunctionController functionController = new FunctionController(functionListServiceimpl);

		HttpServletResponse response = mock(HttpServletResponse.class);
		response.setStatus(200);
		ModuleListVo moduleListVo = (ModuleListVo) functionController.getFunctionList(KEYWORD, response);

		assertNotNull(moduleListVo);
		assertEquals(2, moduleListVo.getModule().size());
	}
}
