package com.rhbgroup.dcpbo.user.useraccess;

import com.rhbgroup.dcpbo.common.exception.CommonException;
import com.rhbgroup.dcpbo.user.common.*;
import com.rhbgroup.dcpbo.user.common.model.bo.Usergroup;
import com.rhbgroup.dcpbo.user.common.model.bo.UsergroupAccess;
import com.rhbgroup.dcpbo.user.info.model.bo.ConfigFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class UsergroupFunctionServiceImpl implements UsergroupFunctionService{

    @Autowired
    UserGroupRepository userGroupRepository;

    @Autowired
    UsergroupAccessRepository usergroupAccessRepository;

    @Autowired
    ConfigFunctionRepository configFunctionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BoRepositoryHelper boRepositoryHelper;

    private static Logger logger = LogManager.getLogger(UsergroupFunctionServiceImpl.class);

    public static final int PAGE_SIZE = 10;

    @Override
    public UsergroupFunction getUserGroupFunctionService(String keyword, Integer pageNum, String accessType, String functionId){

        UsergroupFunction usergroupFunction = new UsergroupFunction();
        UsergroupFunctionListDetails usergroupFunctionListDetails;

        List<Usergroup> usergroupList = new ArrayList<>();
        List<UsergroupFunctionList>  usergroupFunctionListList = new ArrayList<>();

        //Pagination
        Integer totalPageNum = 0;
        Integer activityCount = 0;
        Integer offset = 0;

        UsergroupPagination pagination = new UsergroupPagination();
        pagination.setPageNumber(1);
        offset = (pageNum - 1) * PAGE_SIZE;
        pagination.setPageNumber(pageNum);

        //1- Get Usergroup for keyword(groupName)
        usergroupList = userGroupRepository.getUsergroupByKeyword(keyword);

        if (usergroupList.isEmpty())
            throw new CommonException("50006",
                    "Cannot find Usergroup for keyword: " + keyword, HttpStatus.NOT_FOUND);

        if(functionId != null){
            if(!functionId.equals("")){
                logger.debug("Inside functionId....... " + functionId);


                List<Integer> functionIds = BoRepositoryHelper.constructIdsByDelimitedString(functionId);
                List<Integer> userGroupIdList = usergroupAccessRepository.findUserGroupIdByFunctionId(functionIds);

                List<Usergroup> tmpList = new LinkedList<Usergroup>();
                usergroupList.stream().filter(x -> userGroupIdList.contains(x.getId())).forEach(x -> {
                    tmpList.add(x);
                });
                usergroupList = tmpList;

                logger.debug("    after filter, usergroupList: " + usergroupList);

                if (usergroupList.isEmpty())
                    throw new CommonException("50006",
                            "Cannot find Usergroup for keyword: " + keyword + " and functionId: " + functionId,
                            HttpStatus.NOT_FOUND);
            }
        }

        if(accessType != null){
            if(!accessType.equals("")){
                logger.debug("Inside accessType....... " + accessType);

                List<String> accessTypes = BoRepositoryHelper.constructStringsByDelimitedString(accessType);
                List<Integer> usergroupIdList1 =  usergroupAccessRepository.findUsergroupIdByAccessType(accessTypes);

                List<Usergroup> tmpList = new LinkedList<Usergroup>();
                usergroupList.stream().filter(x -> usergroupIdList1.contains(x.getId())).forEach(x -> {
                    tmpList.add(x);
                });
                usergroupList = tmpList;

                logger.debug("    after  filter, usergroupList: " + usergroupList);
            }
        }

        if (usergroupList.isEmpty())
            throw new CommonException("50006",
                    "Cannot find Usergroup for keyword: " + keyword, HttpStatus.NOT_FOUND);

        //Constructing pagination
        activityCount = usergroupList.size();
        totalPageNum = activityCount / PAGE_SIZE;
        if (activityCount % PAGE_SIZE > 0)
            ++totalPageNum;

        usergroupList = usergroupList.stream().skip(offset).limit(PAGE_SIZE).collect(Collectors.toList());

        usergroupList.forEach(usergroup -> {
            List<UsergroupFunctionListDetails> usergroupFunctionListDetailsList = new ArrayList<>();
            UsergroupFunctionList usergroupFunctionList = new UsergroupFunctionList();

            List<UsergroupAccess> usergroupAccessList = new ArrayList<>();
            List<String> accessTypes1 = new ArrayList<>();
            if(accessType != null && !accessType.equals("")) {
                accessTypes1 = BoRepositoryHelper.constructStringsByDelimitedString(accessType);
            }

            if(!accessTypes1.isEmpty()){
                usergroupAccessList = usergroupAccessRepository.findByUserGroupIdAndStatusAndAccessType(usergroup.getId(), accessTypes1);
            }else {
                usergroupAccessList = usergroupAccessRepository.findByUserGroupIdAndStatus(usergroup.getId());
            }

            if(usergroupAccessList != null){
                usergroupAccessList.forEach(usergroupAccess -> {
                    UsergroupFunctionListDetails usergroupFunctionListDetails1 = new UsergroupFunctionListDetails();
                    ConfigFunction configFunction1 = configFunctionRepository.findOne(usergroupAccess.getFunctionId());

                    usergroupFunctionListDetails1.setFunctionId(usergroupAccess.getFunctionId());
                    usergroupFunctionListDetails1.setFunctionName(configFunction1.getFunctionName());

                    usergroupFunctionListDetailsList.add(usergroupFunctionListDetails1);

                    usergroupFunctionList.setGroupId(usergroupAccess.getUserGroupId());
                    usergroupFunctionList.setGroupName(usergroup.getGroupName());
                    usergroupFunctionList.setAccessType(usergroupAccess.getAccessType());
                    usergroupFunctionList.setFunction(usergroupFunctionListDetailsList);
                });
                usergroupFunctionListList.add(usergroupFunctionList);
            }
            usergroupFunction.setUsergroup(usergroupFunctionListList);
        });

        pagination.setActivityCount(activityCount);
        pagination.setPageNumber(pageNum);
        pagination.setTotalPageNum(totalPageNum);

        usergroupFunction.setPagination(pagination);

        return usergroupFunction;
    }
}

