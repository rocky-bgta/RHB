package com.rhbgroup.dcpbo.user.function;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.function.dto.ModuleListVo;
import com.rhbgroup.dcpbo.user.function.dto.ModuleVo;
import com.rhbgroup.dcpbo.user.function.list.FunctionListService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping(path = "/bo/function")
public class FunctionController {

	private static Logger logger = LogManager.getLogger(FunctionController.class);

	private FunctionListService functionListService;

	public FunctionController(FunctionListService functionListService) {
		this.functionListService = functionListService;
	}

	@BoControllerAudit(eventCode = "20020")
	@GetMapping(value = "/list", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    @ResponseBody
	public BoData getFunctionList(@RequestParam String keyword, HttpServletResponse response) {
		logger.info(String.format("Entering1 getFunctionList() with parameter: %s", keyword));

		ModuleListVo moduleListVo = (ModuleListVo) functionListService.getFunctionList(keyword);
		List<ModuleVo> moduleVoList = moduleListVo.getModule();
		if(moduleVoList.isEmpty()){
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }

        return functionListService.getFunctionList(keyword);
	}

}
