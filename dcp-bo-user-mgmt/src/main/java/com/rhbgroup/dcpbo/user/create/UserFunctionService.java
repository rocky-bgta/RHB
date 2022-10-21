package com.rhbgroup.dcpbo.user.create;

import com.rhbgroup.dcpbo.user.common.BoData;

public interface UserFunctionService {

	public BoData createBoUser(Integer userId, UserCreateRequestVo request);
	
	public BoData updateBoUser(Integer creatorId, UserUpdateRequestVo request, String updateUserId);
	
	public BoData deleteBoUser(Integer creatorId, UserDeleteRequestVo request, Integer deleteUserId);
}
