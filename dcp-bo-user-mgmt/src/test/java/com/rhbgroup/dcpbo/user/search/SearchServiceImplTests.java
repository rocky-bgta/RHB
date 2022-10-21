package com.rhbgroup.dcpbo.user.search;

import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.ConfigDepartment;
import com.rhbgroup.dcpbo.user.common.model.bo.UserUsergroup;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.info.model.bo.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SearchServiceImpl.class, SearchServiceImplTests.class, BoRepositoryHelper.class})
public class SearchServiceImplTests {

	private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";
    private static final String STATUS_DELETE = "D";

	@Autowired
	SearchService searchService;

	@MockBean
	UserRepository userRepositoryMock;

	@MockBean
	UserGroupRepository userGroupRepositoryMock;

	@MockBean
	UserUsergroupRepository userUsergroupRepositoryMock;

	@MockBean
	ConfigDepartmentRepository departmentRepositoryMock;

	@Autowired
	BoRepositoryHelper boRepositoryHelper = new BoRepositoryHelper();



	private static Logger logger = LogManager.getLogger(SearchServiceImplTests.class);

	String keyword = "%man%";
	int pageNum = 1;
	String departmentId = "1";
	String status = "A";
	String userGroupId = "1";
	Timestamp fromTimestamp = createTimestamp("2018-10-07T00:00:00+08:00");
	Timestamp toTimestamp = createTimestamp("2018-10-08T00:00:00+08:00");
	String sortOrder = "";

	public SearchServiceImplTests() throws Throwable {
	}

	@Before
	public void beforeClass() throws Throwable {
		List<UserUsergroup> userUsergroupList = new LinkedList<UserUsergroup>();
		userUsergroupList.add(createUserUsergroup(1, 1));
		userUsergroupList.add(createUserUsergroup(2, 1));
		userUsergroupList.add(createUserUsergroup(3, 1));
		userUsergroupList.add(createUserUsergroup(4, 1));
		userUsergroupList.add(createUserUsergroup(5, 2));
		when(userUsergroupRepositoryMock.findByUserGroupId(Mockito.anyInt())).thenReturn(userUsergroupList);
        when(userUsergroupRepositoryMock.findAllByExcludeStatus(STATUS_DELETE)).thenReturn(userUsergroupList);

		userUsergroupList = new LinkedList<UserUsergroup>();
		userUsergroupList.add(createUserUsergroup(1, 1));
		userUsergroupList.add(createUserUsergroup(1, 2));
		when(userUsergroupRepositoryMock.findAllByUserId(1)).thenReturn(userUsergroupList);

		userUsergroupList = new LinkedList<UserUsergroup>();
		userUsergroupList.add(createUserUsergroup(4, 1));
		userUsergroupList.add(createUserUsergroup(4, 3));
		when(userUsergroupRepositoryMock.findAllByUserId(4)).thenReturn(userUsergroupList);

		when(userGroupRepositoryMock.getOne(1)).thenReturn(createUsergroup(1, "User Management Admin"));
		when(userGroupRepositoryMock.getOne(2)).thenReturn(createUsergroup(2, "Customer Service Admin"));
		when(userGroupRepositoryMock.getOne(3)).thenReturn(createUsergroup(3, "Marketing Admin"));

		when(departmentRepositoryMock.getOne(1)).thenReturn(createConfigDepartment(1, "Customer Call Centre"));
		when(departmentRepositoryMock.getOne(2)).thenReturn(createConfigDepartment(2, "Marketing"));
		when(departmentRepositoryMock.getOne(3)).thenReturn(createConfigDepartment(3, "Sales"));

		MockitoAnnotations.initMocks(this);
	}

	private void createUserRecords() throws Throwable {
		List<User> userList = new LinkedList<User>();
		addUser(userList, 1, "othman", "Othman", "othman@rhbgroup.com", 1, "A", "2018-10-07T01:00:00+08:00");
		addUser(userList, 2, "rahman", "Rahman", "rahman@rhbgroup.com", 2, "B", "2018-10-07T02:00:00+08:00");
		when(userRepositoryMock.findByUsernamePattern(Mockito.anyString())).thenReturn(userList);

		List<User> anotherList = new LinkedList<User>();
		addUser(anotherList, 3, "othman", "Othman", "othman@rhbgroup.com", 3, "A", "2018-10-07T03:00:00+08:00");
		addUser(anotherList, 4, "sulaiman", "Sulaiman", "sulaiman@rhbgroup.com", 1, "A", "2018-10-07T04:00:00+08:00");
		addUser(anotherList, 5, "aman", "Aman", "aman@rhbgroup.com", 1, "B", "2018-10-07T05:00:00+08:00");
		when(userRepositoryMock.findByNamePattern(Mockito.anyString())).thenReturn(anotherList);

        when(userRepositoryMock.findByCustomParamSortDefault(any(),any(),any(),any(),any(),any(),any(),any(),any())).thenReturn(anotherList);
	}

	@Test
	public void userSearchTest() throws Throwable{
	    createUserRecords();

		List<ConfigDepartment> configDepartmentList = new ArrayList<>();
		ConfigDepartment configDepartment = new ConfigDepartment();
		configDepartmentList.add(configDepartment);

        SearchResult searchResult = (SearchResult) searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);

		assertEquals(3,searchResult.getPagination().getActivityCount());
	}

//	public void searchTest() throws Throwable {
//		logger.debug("searchTest()");
//
//		createUserRecords();
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//
//		SearchResult searchResult = (SearchResult) searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//		logger.debug("    searchResult: " + JsonUtil.objectToJson(searchResult));
//		assertNotNull(searchResult);
//	}

//	public void searchTest_filterStatus_B() throws Throwable {
//		logger.debug("searchTest()");
//
//		createUserRecords();
//		status = "B";
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//		SearchResult searchResult = (SearchResult) searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//		logger.debug("    searchResult: " + JsonUtil.objectToJson(searchResult));
//		assertNotNull(searchResult);
//
//		List<User> users = searchResult.getUser();
//		users.forEach(user -> {
//			assertEquals(status, user.getUserStatusId());
//		});
//	}

//	public void searchTest_usernameNotFoundNameFound() throws Throwable {
//		logger.debug("searchTest_usernameNotFoundNameFound()");
//
//		createUserRecords();
//		when(userRepositoryMock.findByUsernamePattern(Mockito.anyString())).thenReturn(null);
//
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//		SearchResult searchResult = (SearchResult) searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//		logger.debug("    searchResult: " + JsonUtil.objectToJson(searchResult));
//		assertNotNull(searchResult);
//	}

//	public void searchTest_usernameFoundNameNotFound() throws Throwable {
//		logger.debug("searchTest_usernameFoundNameNotFound()");
//
//		createUserRecords();
//		when(userRepositoryMock.findByNamePattern(Mockito.anyString())).thenReturn(null);
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//		SearchResult searchResult = (SearchResult) searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//		logger.debug("    searchResult: " + JsonUtil.objectToJson(searchResult));
//		assertNotNull(searchResult);
//	}

//	@Test(expected = CommonException.class)
//	public void searchTest_bothUsernameAndNameNotFound() throws Throwable {
//		logger.debug("searchTest_bothUsernameAndNameNotFound()");
//
//		when(userRepositoryMock.findByUsernamePattern(Mockito.anyString())).thenReturn(null);
//		when(userRepositoryMock.findByNamePattern(Mockito.anyString())).thenReturn(null);
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//		searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//	}

//	@Test(expected = CommonException.class	)
//	public void searchTest_emptyValues() throws Throwable {
//		logger.debug("searchTest_emptyValues()");
//
//		createUserRecords();
//
//		String keyword = "";
//		Integer pageNum = 1;
//		String departmentId = null;
//		String status = "";
//		String userGroupId = null;
//		Timestamp fromTimestamp = null;
//		Timestamp toTimestamp = null;
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//		SearchResult searchResult = (SearchResult) searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//		logger.debug("    searchResult: " + JsonUtil.objectToJson(searchResult));
//		assertNotNull(searchResult);
//	}

//	@Test(expected = CommonException.class)
//	public void searchTest_departmentNotMatched() throws Throwable {
//		logger.debug("searchTest_departmentNotMatched()");
//
//		createUserRecords();
//
//		String departmentId = "123";
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//		searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//	}

//	@Test(expected = CommonException.class)
//	public void searchTest_statusNotMatched() throws Throwable {
//		logger.debug("searchTest_statusNotMatched()");
//
//		createUserRecords();
//
//		String status = "ABC";
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//		searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//	}

//	@Test(expected = CommonException.class)
//	public void searchTest_userGroupNotMatched() throws Throwable {
//		logger.debug("searchTest_userGroupNotMatched()");
//
//		createUserRecords();
//
//		String userGroupId = "123";
//
//		List<UserUsergroup> userUsergroupList = new LinkedList<UserUsergroup>();
//		userUsergroupList.add(createUserUsergroup(12, Integer.valueOf(userGroupId)));
//		when(userUsergroupRepositoryMock.findByUserGroupId(Mockito.anyInt())).thenReturn(userUsergroupList);
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//		searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//	}

//	public void searchTest_userGroupNotFound() throws Throwable {
//		logger.debug("searchTest_userGroupNotFound()");
//
//		createUserRecords();
//
//		when(userUsergroupRepositoryMock.findByUserGroupId(Mockito.anyInt())).thenReturn(null);
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//		searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//	}

//	public void searchTest_userGroupNotFoundReturnEmptyList() throws Throwable {
//		logger.debug("searchTest_userGroupNotFoundReturnEmptyList()");
//
//		createUserRecords();
//
//		List<UserUsergroup> list = new LinkedList<UserUsergroup>();
//		when(userUsergroupRepositoryMock.findByUserGroupId(Mockito.anyInt())).thenReturn(list);
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//		searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//	}

//	@Test(expected = CommonException.class)
//	public void searchTest_fromTimestampNotMatched() throws Throwable {
//		logger.debug("searchTest_fromTimestampNotMatched()");
//
//		createUserRecords();
//
//		Timestamp fromTimestamp = createTimestamp("2018-10-17T00:00:00+08:00");
//		Timestamp toTimestamp = createTimestamp("2018-10-27T00:00:00+08:00");
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//		searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//	}

//	@Test(expected = CommonException.class)
//	public void searchTest_toTimestampNotMatched() throws Throwable {
//		logger.debug("searchTest_toTimestampNotMatched()");
//
//		createUserRecords();
//
//		Timestamp fromTimestamp = createTimestamp("2018-10-01T00:00:00+08:00");
//		Timestamp toTimestamp = createTimestamp("2018-10-02T00:00:00+08:00");
//
//		logger.debug("    keyword: " + keyword);
//		logger.debug("    pageNum: " + pageNum);
//		logger.debug("    departmentId: " + departmentId);
//		logger.debug("    status: " + status);
//		logger.debug("    userGroupId: " + userGroupId);
//		logger.debug("    fromTimestamp: " + fromTimestamp);
//		logger.debug("    toTimestamp: " + toTimestamp);
//
//		searchService.search(keyword, pageNum, departmentId, status, userGroupId, fromTimestamp, toTimestamp, sortOrder);
//	}

//	@Test
//	public void userSetTest() {
//		logger.debug("userSetTest()");
//
//		UserSet set = new UserSet();
//		logger.debug("    set: " + set);
//		logger.debug("        isEmpty: " + set.isEmpty());
//
//		User user = new User();
//		user.setId(1);
//		set.add(user);
//
//		logger.debug("    contains: " + set.contains(user));
//	}

//	@Test
//	public void userSetTest_doesntContainUser() {
//		logger.debug("userSetTest_doesntContainUser()");
//
//		UserSet set = new UserSet();
//		logger.debug("    set: " + set);
//		logger.debug("        isEmpty: " + set.isEmpty());
//
//		User user = new User();
//		user.setId(1);
//		set.add(user);
//
//		User otherUser = new User();
//		otherUser.setId(2);
//
//		logger.debug("    contains: " + set.contains(otherUser));
//	}

//	@Test
//	public void userSetTest_empty() {
//		logger.debug("userSetTest_empty()");
//
//		UserSet set = new UserSet();
//		logger.debug("    set: " + set);
//		logger.debug("        isEmpty: " + set.isEmpty());
//
//		User user = new User();
//		user.setId(1);
//
//		logger.debug("    contains: " + set.contains(user));
//	}

//	@Test
//	public void departmentSetTest() {
//		logger.debug("departmentSetTest()");
//
//		DepartmentSet set = new DepartmentSet();
//		logger.debug("    set: " + set);
//		logger.debug("        isEmpty: " + set.isEmpty());
//
//		ConfigDepartment dept = new ConfigDepartment();
//		dept.setDepartmentId(1);
//		set.add(dept);
//
//		logger.debug("    contains: " + set.contains(dept));
//	}

//	@Test
//	public void departmentSetTest_doesntContainDepartment() {
//		logger.debug("departmentSetTest_doesntContainDepartment()");
//
//		DepartmentSet set = new DepartmentSet();
//		logger.debug("    set: " + set);
//		logger.debug("        isEmpty: " + set.isEmpty());
//
//		ConfigDepartment dept = new ConfigDepartment();
//		dept.setDepartmentId(1);
//		set.add(dept);
//
//		ConfigDepartment otherDept = new ConfigDepartment();
//		otherDept.setDepartmentId(2);
//
//		logger.debug("    contains: " + set.contains(otherDept));
//	}

//	@Test
//	public void departmentSetTest_empty() {
//		logger.debug("departmentSetTest_empty()");
//
//		DepartmentSet set = new DepartmentSet();
//		logger.debug("    set: " + set);
//		logger.debug("        isEmpty: " + set.isEmpty());
//
//		ConfigDepartment dept = new ConfigDepartment();
//		dept.setDepartmentId(1);
//
//		logger.debug("    contains: " + set.contains(dept));
//	}

	private void addUser(
			List<User> userList,
			int id, String username,
			String name,
			String email,
			int departmentId,
			String status,
			String createdTimeString) throws ParseException {
		Timestamp createdTime = createTimestamp(createdTimeString);
		Timestamp updatedTime = new Timestamp(createdTime.getTime() + 1800000);

		User user = new User();
		user.setId(id);
		user.setUsername(username);
		user.setName(name);
		user.setEmail(email);
		user.setUserDepartmentId(departmentId);
		user.setUserStatusId(status);
		user.setCreatedTime(createdTime);
		user.setUpdatedTime(updatedTime);
		user.setUpdatedBy("ikhwan");
		userList.add(user);
	}

	private Timestamp createTimestamp(String dateString) throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
		return new Timestamp(simpleDateFormat.parse(dateString).getTime());
	}

	private Usergroup createUsergroup(int id, String groupName) {
		Usergroup usergroup = new Usergroup();
		usergroup.setId(id);
		usergroup.setGroupName(groupName);
		usergroup.setGroupStatus("A");

		return usergroup;
	}

	private ConfigDepartment createConfigDepartment(int departmentId, String departmentName) {
		ConfigDepartment configDepartment = new ConfigDepartment();
		configDepartment.setDepartmentId(departmentId);
		configDepartment.setDepartmentName(departmentName);

		return configDepartment;
	}

	private UserUsergroup createUserUsergroup(int userId, int usergroupId) {
		UserUsergroup userUsergroup = new UserUsergroup();
		userUsergroup.setUserId(userId);
		userUsergroup.setUserGroupId(Integer.valueOf(userGroupId));

		return userUsergroup;
	}
}
