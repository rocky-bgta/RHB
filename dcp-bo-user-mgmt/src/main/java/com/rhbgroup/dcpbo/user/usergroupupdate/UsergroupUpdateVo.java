package com.rhbgroup.dcpbo.user.usergroupupdate;

import com.rhbgroup.dcpbo.user.create.UserFunctionUserGroupVo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UsergroupUpdateVo {

    private String groupName;
    private List<Integer> functionId;
    private String accessType;
}


