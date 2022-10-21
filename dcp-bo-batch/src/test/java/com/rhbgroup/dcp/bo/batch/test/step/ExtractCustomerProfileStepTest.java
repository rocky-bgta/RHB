package com.rhbgroup.dcp.bo.batch.test.step;

import java.util.*;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.job.model.*;
import freemarker.template.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import com.rhbgroup.dcp.bo.batch.test.common.BaseFTPJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        StepScopeTestExecutionListener.class,
        MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class ExtractCustomerProfileStepTest {
    private static final String STEP_NAME = "ExtractCustomerProfile";

    @Autowired
    @Qualifier(STEP_NAME+ ".ItemProcessor")
    private ItemProcessor<ExtractCustomerProfile, ExtractCustomerProfile> itemProcessor;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    private StepExecution stepExecution;

    public StepExecution getStepExection() {
        stepExecution = MetaDataInstanceFactory.createStepExecution();
        return stepExecution;
    }
    @Test
    public void testProcessor() throws Exception {

        ExtractCustomerProfile detail = new ExtractCustomerProfile();
        itemProcessor.process(detail);
        detail.setCisNo("1");
        itemProcessor.process(detail);
    }
}