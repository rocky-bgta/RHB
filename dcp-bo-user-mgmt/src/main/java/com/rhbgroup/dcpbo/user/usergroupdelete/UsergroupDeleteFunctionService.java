package com.rhbgroup.dcpbo.user.usergroupdelete;

import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.create.UserDeleteRequestVo;

public interface UsergroupDeleteFunctionService {

    public BoData deleteBoUsergroup(Integer creatorId, UsergroupDeleteRequestVo request, Integer deleteUsergroupId);
}
