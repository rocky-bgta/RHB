package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.dcpbo.ConfigModule;
import com.rhbgroup.dcpbo.customer.repository.ConfigModuleRepository;
import com.rhbgroup.dcpbo.customer.service.ConfigModuleService;
import com.rhbgroup.dcpbo.customer.service.impl.ConfigModuleServiceImpl;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = {
        ConfigModuleControllerTests.class,
        ConfigModuleController.class,
        ConfigModuleService.class,
        ConfigModuleServiceImpl.class})
@EnableWebMvc
public class ConfigModuleControllerTests {

    public static final String WORKFLOW = "Workflow";
    public static final String PROVIDE_ASSISTANCE = "Provide Assistance";
    public static final String USER_MANAGEMENT = "User Management";
    private static Logger logger = LogManager.getLogger(ConfigModuleControllerTests.class);

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ConfigModuleService configModuleService;

    @MockBean
    ConfigModuleRepository configModuleRepositoryMock;

    private List<ConfigModule> configModuleList;

    @Before
    public void init() {
        ConfigModule configModule1 = new ConfigModule();
        configModule1.setId(1);
        configModule1.setModuleName(WORKFLOW);
        ConfigModule configModule2 = new ConfigModule();
        configModule2.setId(2);
        configModule2.setModuleName(PROVIDE_ASSISTANCE);
        ConfigModule configModule3 = new ConfigModule();
        configModule3.setId(3);
        configModule3.setModuleName(USER_MANAGEMENT);

        configModuleList = new ArrayList<>();
        configModuleList.add(configModule1);
        configModuleList.add(configModule2);
        configModuleList.add(configModule3);
    }

    @Test
    public void getModuleList() throws Exception {

        logger.debug("getModuleList()");
        logger.debug("    configModuleList: " + configModuleList);

        when(configModuleRepositoryMock.findAll()).thenReturn(configModuleList);

        String url = "/bo/config/module/list";
        logger.debug("    url: " + url);

        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.jsonPath("$.modules[0].moduleId", is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modules[0].moduleName", is(WORKFLOW)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modules[1].moduleId", is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modules[1].moduleName", is(PROVIDE_ASSISTANCE)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modules[2].moduleId", is(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.modules[2].moduleName", is(USER_MANAGEMENT)));

        logger.debug("JSON PAYLOAD: " + result.andReturn().getResponse().getContentAsString());
    }
}