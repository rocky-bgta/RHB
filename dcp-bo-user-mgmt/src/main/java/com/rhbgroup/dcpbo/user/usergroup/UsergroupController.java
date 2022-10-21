package com.rhbgroup.dcpbo.user.usergroup;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.usergroup.dto.UsergroupVo;
import com.rhbgroup.dcpbo.user.usergroup.list.dto.UsergroupListVo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.usergroup.list.UsergroupListService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping(path = "/bo/usergroup")
public class UsergroupController {

	private UsergroupAddService usergroupAddService;

	private static Logger logger = LogManager.getLogger(UsergroupController.class);

	private UsergroupListService usergroupListService;

	public UsergroupController(UsergroupAddService usergroupAddService,
							   UsergroupListService usergroupListService) {
		this.usergroupAddService = usergroupAddService;
		this.usergroupListService = usergroupListService;
	}

	@BoControllerAudit(eventCode = "22001")
    @PostMapping(value = "/",
    		produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
	public BoData postUsergroupController(@RequestBody UsergroupRequestBody usergroupRequestBody,
										  @RequestHeader(value = "userid",defaultValue = "0") String userid) {

		return usergroupAddService.postUsergroupService(usergroupRequestBody, userid);
	}

	@BoControllerAudit(eventCode = "20024")
	@GetMapping(value = "/list", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
	@ResponseBody
	public BoData getUsergroupList(@RequestParam String keyword, HttpServletResponse response) {
		logger.info(String.format("Entering getUsergroupList() with parameter: %s", keyword));

		UsergroupListVo usergroupListVo = (UsergroupListVo) usergroupListService.getUsergroupList(keyword);
		List<UsergroupVo> usergroupVoList= usergroupListVo.getUsergroup();
		if(usergroupVoList.isEmpty()){
			response.setStatus(HttpStatus.NOT_FOUND.value());
		}

		return usergroupListService.getUsergroupList(keyword);
	}
}
