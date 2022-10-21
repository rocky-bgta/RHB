package com.rhbgroup.dcpbo.user.usergroupupdate;

import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.create.UserDeleteRequestVo;
import com.rhbgroup.dcpbo.user.create.UserUpdateRequestVo;

public interface UsergroupUpdateFunctionService {

    public BoData updateBoUsergroup(Integer creatorId, UsergroupUpdateRequestVo request, Integer updateUsergroupId);

}