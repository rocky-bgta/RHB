package com.rhbgroup.dcp.bo.batch.test.repository;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.repository.BatchParameterRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchLookup;
import com.rhbgroup.dcp.bo.batch.job.repository.BillPaymentConfigOutboundRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

import lombok.Getter;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class BatchParameterRepositoryImplTest extends BaseJobTest {

    @Autowired
    private BatchParameterRepositoryImpl batchParameterRepository;
    
    @MockBean
    private BillPaymentConfigOutboundRepositoryImpl billPaymentConfigOutboundRepositoryImpl;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    @MockBean @Getter
    private JdbcTemplate mockJdbcTemplate;

    @Test (expected = BatchException.class)
    public void testNegativeUpdateBatchSystemDate() throws BatchException{

        String sqlString = "UPDATE TBL_BATCH_CONFIG SET PARAMETER_VALUE=?, UPDATED_TIME=? WHERE PARAMETER_KEY=?";
        Date date = new GregorianCalendar(2018, Calendar.FEBRUARY, 18).getTime();

        when(getMockJdbcTemplate().update(sqlString, DateUtils.formatDateString(date,BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_FORMAT)
                , new Date(), BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY)).thenReturn(0);
        batchParameterRepository.updateBatchSystemDate(date);

        BatchParameterRepositoryImpl mockBatchParameterRepository = mock(BatchParameterRepositoryImpl.class);
        verify(mockBatchParameterRepository , times(1)).updateBatchSystemDate(date);
    }

    // New record to be inserted to lookup
    private BatchLookup createBatchLookup() {
        BatchLookup batchLookup = new BatchLookup();

        batchLookup.setGroup("TestGroup");
        batchLookup.setValue("TestValue");

        return batchLookup;
    }
}
