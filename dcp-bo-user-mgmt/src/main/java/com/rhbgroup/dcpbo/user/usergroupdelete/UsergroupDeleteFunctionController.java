package com.rhbgroup.dcpbo.user.usergroupdelete;

import com.rhbgroup.dcpbo.common.audit.BoControllerAudit;
import com.rhbgroup.dcpbo.user.common.BoData;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path = "/bo")
public class UsergroupDeleteFunctionController {

    private UsergroupDeleteFunctionService usergroupDeleteFunctionService;

    public UsergroupDeleteFunctionController(UsergroupDeleteFunctionService usergroupDeleteFunctionService) {
        this.usergroupDeleteFunctionService = usergroupDeleteFunctionService;
    }

    @BoControllerAudit(eventCode = "22007")
    @PutMapping(value = "/usergroup/{usergroupId}/delete", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    public BoData deleteBoUsergroup(@RequestHeader Integer userId, @RequestBody UsergroupDeleteRequestVo request,
                                    @PathVariable(value = "usergroupId") Integer usergroupId) {


        return usergroupDeleteFunctionService.deleteBoUsergroup(userId, request, usergroupId);
    }
}