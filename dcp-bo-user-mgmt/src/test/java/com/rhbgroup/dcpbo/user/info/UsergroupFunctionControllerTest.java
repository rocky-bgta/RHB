package com.rhbgroup.dcpbo.user.info;

import com.rhbgroup.dcpbo.user.useraccess.UsergroupFunction;
import com.rhbgroup.dcpbo.user.useraccess.UsergroupFunctionList;
import com.rhbgroup.dcpbo.user.useraccess.UsergroupFunctionListDetails;
import com.rhbgroup.dcpbo.user.useraccess.UsergroupFunctionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {UsergroupFunctionControllerTest.class})
@EnableWebMvc
public class UsergroupFunctionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean(name = "usergroupFunctionService")
    UsergroupFunctionService usergroupFunctionServiceMock;

    private static Logger logger = LogManager.getLogger(UsergroupFunctionControllerTest.class);

    @Test
    public void getUserGroupFunctionServiceTest() throws Exception{
        logger.debug("getUserGroupFunctionServiceTest()");
        logger.debug("    userGroupServiceMock: " + usergroupFunctionServiceMock);

        UsergroupFunction usergroupFunction = new UsergroupFunction();
        UsergroupFunctionList usergroupFunctionList = new UsergroupFunctionList();
        List<UsergroupFunctionListDetails> userGroupFunctionListDetailsList = new ArrayList<>();
        UsergroupFunctionListDetails userGroupFunctionListDetails = new UsergroupFunctionListDetails();

        userGroupFunctionListDetails.setFunctionId(1);
        userGroupFunctionListDetails.setFunctionName("User");
        userGroupFunctionListDetailsList.add(userGroupFunctionListDetails);

        usergroupFunctionList.setGroupId(1);
        usergroupFunctionList.setGroupName("Management");
        usergroupFunctionList.setAccessType("M");
        usergroupFunctionList.setFunction(userGroupFunctionListDetailsList);
        //usergroupFunction.setUsergroup(usergroupFunctionList);

        when(usergroupFunctionServiceMock.getUserGroupFunctionService(Mockito.anyString(), Mockito.anyInt(),
                //Mockito.anyString(), Mockito.anyInt())).thenReturn(usergroupFunction);
                Mockito.anyString(), Mockito.anyString())).thenReturn(usergroupFunction);

        String keyword = "Management";
        Integer pageNum = 1;
        String accessType = "M";
        //Integer functionId = 2;
        String functionId = "2";

        usergroupFunction = usergroupFunctionServiceMock.getUserGroupFunctionService(keyword, pageNum, accessType, functionId);
        logger.debug("    userGroupFunction: " + usergroupFunction);

        String url = "/bo/usergroup/search" + "?keyword=" +keyword + "&pageNum="+pageNum + "&accessType=" + accessType + "&functionId=" + functionId;
        logger.debug("  url: " + url);
    }
}
