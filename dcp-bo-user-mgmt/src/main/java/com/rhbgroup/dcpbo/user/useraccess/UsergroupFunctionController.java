package com.rhbgroup.dcpbo.user.useraccess;


import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rhbgroup.dcpbo.user.common.BoData;

@RestController
@RequestMapping(path = "/bo/usergroup")
public class UsergroupFunctionController {

    private UsergroupFunctionService usergroupFunctionService;

    private static Logger logger = LogManager.getLogger(UsergroupFunctionController.class);

    public UsergroupFunctionController(UsergroupFunctionService usergroupFunctionService) {
        this.usergroupFunctionService = usergroupFunctionService;
    }

    @BoControllerAudit(eventCode = "20025", value = "boAuditAdditionalDataRetriever")
    @GetMapping(value = "/search", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public BoData getUserGroupFunction(@RequestParam(value = "keyword", defaultValue = "") String keyword,
                                       @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                       @RequestParam(value = "accessType", defaultValue = "") String accessType,
                                       @RequestParam(value = "functionId", defaultValue = "") String functionId){

        return usergroupFunctionService.getUserGroupFunctionService(keyword, pageNum, accessType, functionId);
    }

}
