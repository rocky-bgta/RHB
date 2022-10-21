package com.rhbgroup.dcpbo.user.usergroupupdate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.user.create.UserPayloadUpdateVo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@JsonInclude
public class UsergroupUpdateRequestVo {

    private Integer functionId;
    private Integer groupId;
    private UsergroupUpdateVo cache;
    private UsergroupUpdateVo input;
}

