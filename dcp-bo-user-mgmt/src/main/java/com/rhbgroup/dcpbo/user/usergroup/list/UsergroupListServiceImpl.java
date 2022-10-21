package com.rhbgroup.dcpbo.user.usergroup.list;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.common.UserGroupRepository;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.usergroup.dto.UsergroupVo;
import com.rhbgroup.dcpbo.user.usergroup.list.dto.UsergroupListVo;

public class UsergroupListServiceImpl implements UsergroupListService {

	private static Logger logger = LogManager.getLogger(UsergroupListServiceImpl.class);
	@Autowired
	UserGroupRepository userGroupRepository;

	@Override
	public BoData getUsergroupList(String keyword) {

		List<Usergroup> usergroups = userGroupRepository.findDistinctByGroupNameContaining(keyword);

		List<UsergroupVo> usergroupVos = new ArrayList<>();

		if (usergroups != null && usergroups.size() > 0) {
			for (Usergroup usergroup : usergroups) {
				logger.debug(String.format("Retrieved usergroup: %s", usergroup.toString()));
				UsergroupVo usergroupVo = new UsergroupVo();
				usergroupVo.setGroupId(usergroup.getId());
				usergroupVo.setGroupName(usergroup.getGroupName());
				usergroupVos.add(usergroupVo);
				logger.debug(String.format("Extracted usergroupVo: %s", usergroupVo.toString()));
			}
		} else {
		    throw new CommonException("50006", "No matching usergroup", HttpStatus.NOT_FOUND);
		}

		UsergroupListVo usergroupList = new UsergroupListVo();
		usergroupList.setUsergroup(usergroupVos);
		logger.debug(String.format("usergroupListVo Returned: %s", usergroupList.toString()));
		return usergroupList;
	}

}
