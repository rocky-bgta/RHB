package com.rhbgroup.dcpbo.user.search;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.rhbgroup.dcpbo.user.common.model.bo.ConfigDepartment;
import com.rhbgroup.dcpbo.user.common.model.bo.UserUsergroup;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.info.model.bo.User;
import org.springframework.http.HttpStatus;

public class SearchServiceImpl implements SearchService {
	
	private static final int RESULT_PER_PAGE = 10;

	private UserRepository userRepository;
	private UserGroupRepository userGroupRepository;
	private UserUsergroupRepository userUsergroupRepository;
	private ConfigDepartmentRepository departmentRepository;
	private BoRepositoryHelper boRepositoryHelper;

	private static Logger logger = LogManager.getLogger(SearchServiceImpl.class);
	
	public SearchServiceImpl(
			UserRepository userRepository,
			UserGroupRepository userGroupRepository,
			UserUsergroupRepository userUsergroupRepository,
			ConfigDepartmentRepository departmentRepository,
			BoRepositoryHelper boRepositoryHelper) {
		this.userRepository = userRepository;
		this.userGroupRepository = userGroupRepository;
		this.userUsergroupRepository = userUsergroupRepository;
		this.departmentRepository = departmentRepository;
		this.boRepositoryHelper = boRepositoryHelper;
	}

	private static final String DEFAULT_VALUE = "";
	private static final Integer EXIST_VALUE = 1;
	private static final Integer NOT_EXIST_VALUE = 0;
	private static final Timestamp DEFAULT_TIME = new Timestamp(new Date().getTime());
	private static final String CREATED_TIME = "created_time";
	private static final String UPDATED_TIME = "updated_time";
	private static final String STATUS_DELETE = "D";

	@Override
	public BoData search(String keyword, Integer pageNum, String departmentId, String status, String userGroupId,
						 Timestamp fromTimestamp, Timestamp toTimestamp, String sortOrder) {
		logger.debug("search()");
		logger.debug("    userRepository: " + userRepository);
		logger.debug("    userGroupRepository: " + userGroupRepository);
		logger.debug("    userUserGroupRepository: " + userUsergroupRepository);
		logger.debug("    keyword: " + keyword);
		logger.debug("    pageNum: " + pageNum);
		logger.debug("    departmentId: " + departmentId);
		logger.debug("    status: " + status);
		logger.debug("    userGroupId: " + userGroupId);
		logger.debug("    fromTimestamp: " + fromTimestamp);
		logger.debug("    toTimestamp: " + toTimestamp);

		List<User> userList = new ArrayList<>();
		Integer offset = 0;
		offset = (pageNum - 1) * RESULT_PER_PAGE;
		keyword = keyword.toLowerCase();

		//Check if status is not specified
		Integer statusExist = EXIST_VALUE;
		List<String> statusList = new ArrayList<>();

		try {
			if (status != null)
				statusList = BoRepositoryHelper.constructStringsByDelimitedString(status);
		}catch (Exception e){
			logger.info("Empty status passed in.");
		}
		if (statusList.size() == 0 || status.equalsIgnoreCase(DEFAULT_VALUE)){
			statusExist = NOT_EXIST_VALUE;
			statusList.add(DEFAULT_VALUE);
		}

		//Check if department is not specified
		Integer departmentExist = EXIST_VALUE;
		List<Integer> departmentList = new ArrayList<>();
		try{
			departmentList = BoRepositoryHelper.constructIdsByDelimitedString(departmentId);
		}catch (Exception e){
			logger.info("Empty department passed in.");
		}
		if (departmentList.size() == 0){
			departmentExist = NOT_EXIST_VALUE;
			departmentList.add(NOT_EXIST_VALUE);
		}

		//Check if from date exist
		Integer fromDateExist = EXIST_VALUE;
		if (fromTimestamp == null){
			fromDateExist = NOT_EXIST_VALUE;
			fromTimestamp = DEFAULT_TIME;
		}

		//Check if to date exist
		Integer toDateExist = EXIST_VALUE;
		if (toTimestamp == null){
			toDateExist = NOT_EXIST_VALUE;
			toTimestamp = DEFAULT_TIME;
		}else {
			Date toDateFormatted = new Date(toTimestamp.getTime());
			Calendar cal = Calendar.getInstance();
			cal.setTime(toTimestamp);
			cal.add(Calendar.DATE, 1);
			toTimestamp.setTime(cal.getTime().getTime());
		}

		//Check for sort order and retrieve user list
		if (sortOrder.equalsIgnoreCase(DEFAULT_VALUE) ){
			userList = userRepository.findByCustomParamSortDefault(keyword,
					statusExist,statusList,
					departmentExist, departmentList,
					fromDateExist, fromTimestamp,
					toDateExist, toTimestamp);
		}else if (sortOrder.equalsIgnoreCase(CREATED_TIME)) {
			userList = userRepository.findByCustomParamSortCreated(keyword,
					statusExist,statusList,
					departmentExist, departmentList,
					fromDateExist, fromTimestamp,
					toDateExist, toTimestamp);
		}else if (sortOrder.equalsIgnoreCase(UPDATED_TIME)) {
			userList = userRepository.findByCustomParamSortUpdated(keyword,
					statusExist,statusList,
					departmentExist, departmentList,
					fromDateExist, fromTimestamp,
					toDateExist, toTimestamp);
		}
		if(userList.isEmpty())
			throw new CommonException("50005",
					"No matching user", HttpStatus.NOT_FOUND);


		//Pre-populate Lists to reduce multiple db calls
		List<UserUsergroup> userUsergroupList = userUsergroupRepository.findAllByExcludeStatus(STATUS_DELETE);
		List<Usergroup> usergroupList = userGroupRepository.findAllByExcludeStatus(STATUS_DELETE);
		List<ConfigDepartment> configDepartmentList = departmentRepository.findAll();

		//Extract usergroup ids to match in user_usergroup and filter userlist
		List<Integer> usergroupFilter = new ArrayList<>();
		try {
			List<Integer> userIdFiltered = new ArrayList<>();
			usergroupFilter = BoRepositoryHelper.constructIdsByDelimitedString(userGroupId);
			List<Integer> finalUsergroupFilter1 = usergroupFilter;
			userIdFiltered = userUsergroupList.stream().filter(userUsergroup -> finalUsergroupFilter1.contains(userUsergroup.getUserGroupId())).map(userUsergroup ->
			{
				return userUsergroup.getUserId();
			}
			).collect(Collectors.toList());
			List<Integer> finalUserIdFiltered = userIdFiltered;
			userList = userList.stream().filter(user -> finalUserIdFiltered.contains(user.getId())).collect(Collectors.toList());
		}catch (Exception e){
			logger.info("Empty usergroup id passed");
			usergroupFilter = usergroupList.stream().map(usergroup -> usergroup.getId()).collect(Collectors.toList());
		}


		/*
		 * Calculate total results count and total pages.
		 */
		int activityCount = userList.size();
		int totalPageNum = activityCount / RESULT_PER_PAGE;
		if (activityCount % RESULT_PER_PAGE > 0)
			++totalPageNum;
		logger.debug("    activityCount: " + activityCount);
		logger.debug("    totalPageNum: " + totalPageNum);
		logger.debug("    pagenum: " + pageNum);
		logger.debug("    boolean: " + String.valueOf(totalPageNum < pageNum));

		if (totalPageNum < pageNum)
			throw new CommonException(CommonException.GENERIC_ERROR_CODE,"Page num is invalid for " + pageNum, HttpStatus.BAD_REQUEST);

		//Limit computation to page size result limit
		userList = userList.stream().skip(offset).limit(RESULT_PER_PAGE).collect(Collectors.toList());

		//Populate payload
		for (User user:userList) {
			List<Integer> usergroupIdList = new ArrayList<>();
			List<Integer> finalUsergroupFilter = usergroupFilter;
			usergroupIdList = userUsergroupList.stream().filter(userUsergroup -> userUsergroup.getUserId().equals(user.getId()))
					.map(userUsergroup -> {
						return userUsergroup.getUserGroupId();
					})
					.collect(Collectors.toList());
			List<Integer> finalUsergroupIdList = usergroupIdList;
			List<Usergroup> finalUsergroup = new ArrayList<>();
			finalUsergroup = usergroupList.stream().filter(usergroup -> finalUsergroupIdList.contains(usergroup.getId())).collect(Collectors.toList());
			Optional<ConfigDepartment> department = configDepartmentList.stream().filter(configDepartment -> configDepartment.getDepartmentId() == user.getUserDepartmentId()).findFirst();
			if (department.isPresent()) {
				user.setDepartmentName(department.get().getDepartmentName());
			}
			user.setUsergroup(finalUsergroup);
		};


		/*
		 * Prepare Pagination instance
		 */
		if (pageNum == null)
			pageNum = 1;
		Pagination pagination = new Pagination();
		pagination.setActivityCount(activityCount);
		pagination.setPageNum(pageNum);
		pagination.setTotalPageNum(totalPageNum);
		logger.debug("    pagination: " + pagination);

		/*
		 * Populate SearchResult
		 */
		SearchResult searchResult = new SearchResult();
		searchResult.setUser(userList);
		searchResult.setPagination(pagination);
		configDepartmentList.sort(Comparator.comparing(ConfigDepartment::getDepartmentName));
		searchResult.setDepartmentList(configDepartmentList);
		logger.debug("    searchResult: " + searchResult);
		
		return searchResult;
	}
}
