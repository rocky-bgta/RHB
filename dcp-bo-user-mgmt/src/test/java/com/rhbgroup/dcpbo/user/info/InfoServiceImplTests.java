package com.rhbgroup.dcpbo.user.info;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.info.model.bo.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.user.common.DirectoryServerRepository;
import com.rhbgroup.dcpbo.user.common.UserRepository;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InfoServiceImplTests.class, InfoServiceImpl.class})
public class InfoServiceImplTests {
	
	@Autowired
	InfoService infoService;
	
	@MockBean
	UserRepository userRepositoryMock;
	
	@MockBean
	DirectoryServerRepository directoryServerRepositoryMock;

	private static Logger logger = LogManager.getLogger(InfoServiceImplTests.class);
	
	@Test
	public void getStaffIdTest() throws Exception {
		logger.debug("getStaffIdTest()");
		logger.debug("    infoService: " + infoService);
		logger.debug("    directoryServerRepositoryMock: " + directoryServerRepositoryMock);
		
		String staffId = "123456";
		
		User user = new User();
		user.setId(1);
		user.setUsername(staffId);
		when(userRepositoryMock.findByUsername(staffId)).thenReturn(null);
		
		UserInfo userInfo = new UserInfo();
		userInfo.setName("Mohd Ikhwan Haris");
		userInfo.setEmail("ikhwan@gmail.com");
		when(directoryServerRepositoryMock.search(Mockito.anyString())).thenReturn(userInfo);
		
		userInfo = (UserInfo) infoService.getStaffId(staffId);
		logger.debug("    userInfo: " + userInfo);
		
		assertNotNull(userInfo);
	}
	
	@Test(expected = CommonException.class)
	public void getStaffIdTest_found() throws Exception {
		logger.debug("getStaffIdTest_notFound()");
		logger.debug("    infoService: " + infoService);

		String staffId = "123456";

		User user = new User();
		user.setId(1);
		user.setUsername(staffId);

		List<User> userList = new ArrayList<>();
		userList.add(user);

		when(userRepositoryMock.findByUsername(Mockito.anyString())).thenReturn(userList);

		infoService.getStaffId(staffId);
	}
		
	@Test(expected = CommonException.class)
	public void getStaffIdTest_notFoundInDirectoryServer() throws Exception {
		logger.debug("getStaffIdTest_notFoundInDirectoryServer()");
		logger.debug("    infoService: " + infoService);

		String staffId = "123456";

		User user = new User();
		user.setId(1);
		user.setUsername(staffId);
		when(userRepositoryMock.findByUsername(staffId)).thenReturn(null);
		
		when(directoryServerRepositoryMock.search(Mockito.anyString())).thenReturn(null);

		infoService.getStaffId(staffId);
	}
}
