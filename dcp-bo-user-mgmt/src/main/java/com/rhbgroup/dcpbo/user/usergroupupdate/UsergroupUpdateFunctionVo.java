package com.rhbgroup.dcpbo.user.usergroupupdate;

import com.rhbgroup.dcpbo.user.common.BoData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsergroupUpdateFunctionVo implements BoData {
    private Integer functionId;
    private String functionName;
}
