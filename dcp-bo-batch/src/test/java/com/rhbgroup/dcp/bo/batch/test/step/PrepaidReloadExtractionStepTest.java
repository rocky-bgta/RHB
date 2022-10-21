package com.rhbgroup.dcp.bo.batch.test.step;

import java.util.*;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.job.model.*;
import freemarker.template.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        StepScopeTestExecutionListener.class,
        MockitoTestExecutionListener.class })
@ActiveProfiles("test")
public class PrepaidReloadExtractionStepTest {
    private static final String STEP_NAME_CASA = "PrepaidReloadExtractionCASA";
    private static final String STEP_NAME_CC = "PrepaidReloadExtractionCC";
    private static final String DETAIL_LIST = "DetailList";

    @Autowired
    @Qualifier(STEP_NAME_CASA + ".ItemProcessor")
    private ItemProcessor<PrepaidReloadExtraction,PrepaidReloadExtractionOut> itemProcessorCASA;
    @Autowired
    @Qualifier(STEP_NAME_CC + ".ItemProcessor")
    private ItemProcessor<PrepaidReloadExtraction,PrepaidReloadExtractionOut> itemProcessorCC;

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
        PrepaidReloadExtraction detail = new PrepaidReloadExtraction();
        itemProcessorCASA.process(detail);
        itemProcessorCC.process(detail);
        detail.setPrepaidProductCode("1");
        detail.setAmount(1.0);
        detail.setHostRefNo("1");
        detail.setMobileNo("1");
        detail.setRefNo("1");
        detail.setTxnTime(new Date());
        itemProcessorCASA.process(detail);
        itemProcessorCC.process(detail);
        detail.setRefNo(null);
        detail.setHostRefNo(null);
        detail.setPrepaidProductCode(null);
        itemProcessorCASA.process(detail);
        itemProcessorCC.process(detail);
    }
}