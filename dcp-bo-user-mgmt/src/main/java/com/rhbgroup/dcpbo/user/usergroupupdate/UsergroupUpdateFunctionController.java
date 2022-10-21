package com.rhbgroup.dcpbo.user.usergroupupdate;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.user.common.BoData;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/bo")
public class UsergroupUpdateFunctionController {

    private UsergroupUpdateFunctionService usergroupUpdateFunctionService;

    public UsergroupUpdateFunctionController(UsergroupUpdateFunctionService usergroupUpdateFunctionService) {
        this.usergroupUpdateFunctionService = usergroupUpdateFunctionService;
    }

    @BoControllerAudit(eventCode = "22004", value = "boAuditAdditionalDataRetriever")
    @PutMapping(value = "/usergroup/{usergroupId}/update", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    public BoData updateBoUsergroup(@RequestHeader("userid") Integer userId, @RequestBody UsergroupUpdateRequestVo request,
                               @PathVariable(value = "usergroupId") Integer usergroupId) {
        return usergroupUpdateFunctionService.updateBoUsergroup(userId, request, usergroupId);
    }

}