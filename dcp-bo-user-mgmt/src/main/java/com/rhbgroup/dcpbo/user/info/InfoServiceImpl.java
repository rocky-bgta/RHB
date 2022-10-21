package com.rhbgroup.dcpbo.user.info;

import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.info.model.bo.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rhbgroup.dcpbo.user.common.BoData;
import com.rhbgroup.dcpbo.user.common.DirectoryServerRepository;
import com.rhbgroup.dcpbo.user.common.UserRepository;
import org.springframework.http.HttpStatus;

import java.util.List;

public class InfoServiceImpl implements InfoService {
	
	private UserRepository userRepository;
	private DirectoryServerRepository directoryServerRepository;

	private static Logger logger = LogManager.getLogger(InfoServiceImpl.class);

	public InfoServiceImpl(UserRepository userRepository, DirectoryServerRepository directoryServerRepository) {
		this.userRepository = userRepository;
		this.directoryServerRepository = directoryServerRepository;
	}

	@Override
	public BoData getStaffId(String staffId) {
		logger.debug("getStaffId()");
		logger.debug("    staffId: " + staffId);
		logger.debug("    userRepository: " + userRepository);
		logger.debug("    directoryServerRepository: " + directoryServerRepository);
		
		List<User> userList = userRepository.findByUsername(staffId);
		logger.debug("    user: " + userList);
		if (userList != null && !userList.isEmpty())
			throw new CommonException("40003", "Cannot find User for staffId: " + staffId, HttpStatus.FORBIDDEN);

		UserInfo userInfo = directoryServerRepository.search(staffId);
		logger.debug("    userInfo: " + userInfo);
		if (userInfo == null)
			throw new CommonException("50007", "Cannot find UserInfo in directory server for staffId: " + staffId, HttpStatus.NOT_FOUND);
		
		return userInfo;
	}

}
