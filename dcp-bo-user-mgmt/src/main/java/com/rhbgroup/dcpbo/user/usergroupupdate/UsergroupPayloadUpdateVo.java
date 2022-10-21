package com.rhbgroup.dcpbo.user.usergroupupdate;

import com.rhbgroup.dcpbo.user.create.UserFunctionUserGroupVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UsergroupPayloadUpdateVo {

    private Integer groupId;
    private String groupName;
    private String accessType;
    private List<Integer> functionId;

}
