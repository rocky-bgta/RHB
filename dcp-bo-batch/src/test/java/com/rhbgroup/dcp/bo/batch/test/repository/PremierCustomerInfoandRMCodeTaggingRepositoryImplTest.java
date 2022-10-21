package com.rhbgroup.dcp.bo.batch.test.repository;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.PremierCustomerInfoandRMCodeTaggingDetail;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchSuspenseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.repository.PremierCustomerInfoandRMCodeTaggingRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import freemarker.template.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class PremierCustomerInfoandRMCodeTaggingRepositoryImplTest extends BaseJobTest {

    @Autowired
    private PremierCustomerInfoandRMCodeTaggingRepositoryImpl premierCustomerInfoandRMCodeTaggingRepository;

    @MockBean
    private JdbcTemplate mockJdbcTemplate;

    @MockBean
    private BatchSuspenseRepositoryImpl batchSuspenseRepository;

    @Mock
    private DataSource dataSource;

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

    @Test
    public void testPositiveInsertPremierCustomerInfoandRMCodeTaggingDetailToStaging() throws Exception {
        List<PremierCustomerInfoandRMCodeTaggingDetail> premierCustomerInfoandRMCodeTaggingDetail = createDetailList();

        int[] returnValue = {1};
        when(mockJdbcTemplate.batchUpdate(anyString(), (BatchPreparedStatementSetter) any(BatchPreparedStatementSetter.class))).thenReturn(returnValue);
        assertEquals(1, premierCustomerInfoandRMCodeTaggingRepository.addRecordBatch(premierCustomerInfoandRMCodeTaggingDetail));
    }

    @Test
    public void testPositiveTruncateTable() {
        String sql = "TRUNCATE TABLE %s";
        String tableName = "table";
        when(mockJdbcTemplate.update(sql, new Object[] {tableName})).thenReturn(1);

        assertEquals(1, premierCustomerInfoandRMCodeTaggingRepository.truncateTable(tableName));
    }

    @Test
    public void testPositiveGetBatchPremierNeworUpdatedValue() {
        List<PremierCustomerInfoandRMCodeTaggingDetail> premierCustomerInfoandRMCodeTaggingDetailList = new ArrayList<>();
        PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = createDetail();

        premierCustomerInfoandRMCodeTaggingDetailList.add(premierCustomerInfoandRMCodeTaggingDetail);

        when(mockJdbcTemplate.query(anyString(), any(Object[].class), any(BeanPropertyRowMapper.class))).thenReturn(premierCustomerInfoandRMCodeTaggingDetailList);

        assertEquals(premierCustomerInfoandRMCodeTaggingDetailList, premierCustomerInfoandRMCodeTaggingRepository.getBatchPremierNeworUpdatedValue("99999"));
    }

    @Test
    public void testPositiveGetCIFNotFoundRecord(){
        List<PremierCustomerInfoandRMCodeTaggingDetail> premierCustomerInfoandRMCodeTaggingDetailList = new ArrayList<>();
        PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = createDetail();

        premierCustomerInfoandRMCodeTaggingDetailList.add(premierCustomerInfoandRMCodeTaggingDetail);

        when(mockJdbcTemplate.query(anyString(), any(Object[].class), any(BeanPropertyRowMapper.class))).thenReturn(premierCustomerInfoandRMCodeTaggingDetailList);

        assertEquals(premierCustomerInfoandRMCodeTaggingDetailList, premierCustomerInfoandRMCodeTaggingRepository.getCIFNotFoundRecord("99999"));
    }

    @Test
    public void testPositiveUpdateProcessStatus() {
        List<String> parameterToUpdate = new ArrayList<String>();
        parameterToUpdate.add("Test");
        parameterToUpdate.add("Test2");
        parameterToUpdate.add("Test3");
        parameterToUpdate.add("Test4");
        parameterToUpdate.add("Test5");

        when(mockJdbcTemplate.update(anyString(), any(Object.class))).thenReturn(1);

        assertEquals(1, premierCustomerInfoandRMCodeTaggingRepository.updateProcessStatus(parameterToUpdate));
    }

    @Test (expected =  NullPointerException.class)
    public void testNegativeUpdateProcessStatus() {
        List<String> parameterToUpdate = null;

        premierCustomerInfoandRMCodeTaggingRepository.updateProcessStatus(parameterToUpdate);
    }

    @Test
    public void testPositiveInsertTblBatchSuspense() throws BatchException {
        BatchSuspense batchSuspense = createBatchSuspense();

        when(batchSuspenseRepository.addBatchSuspenseToDB(batchSuspense)).thenReturn(1);

        assertEquals(true, premierCustomerInfoandRMCodeTaggingRepository.insertTblBatchSuspense(batchSuspense));
    }

    @Test
    public void testNegativeInsertTblBatchSuspense(){
        BatchSuspense batchSuspense = new BatchSuspense();

        assertEquals(false, premierCustomerInfoandRMCodeTaggingRepository.insertTblBatchSuspense(batchSuspense));
    }

    @Test
    public void testPositiveUpdateTBLUserProfile(){
        PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = createDetail();
        String setSQL = "";
        List<String> parameterToUpdate = new ArrayList<String>();
        parameterToUpdate.add("Test");
        parameterToUpdate.add("Test2");
        parameterToUpdate.add("Test3");
        parameterToUpdate.add("Test4");
        parameterToUpdate.add("Test5");

        when(mockJdbcTemplate.update(anyString(), any(Object.class))).thenReturn(1);

        assertEquals(true, premierCustomerInfoandRMCodeTaggingRepository.updateTableUserProfile(setSQL, parameterToUpdate));
    }

    @Test (expected =  NullPointerException.class)
    public void testNegativeUpdateGSTDCP() throws BatchException {
        PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = createDetail();
        String setSQL = "";
        List<String> parameterToUpdate = null;

        premierCustomerInfoandRMCodeTaggingRepository.updateTableUserProfile(setSQL, parameterToUpdate);
    }

    // New record to be inserted premier
    private PremierCustomerInfoandRMCodeTaggingDetail createDetail() {
        PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = new PremierCustomerInfoandRMCodeTaggingDetail();
        premierCustomerInfoandRMCodeTaggingDetail.setCifNo("1");
        premierCustomerInfoandRMCodeTaggingDetail.setOldIsPremier("1");
        return premierCustomerInfoandRMCodeTaggingDetail;
    }

    private List<PremierCustomerInfoandRMCodeTaggingDetail> createDetailList() {
    	List<PremierCustomerInfoandRMCodeTaggingDetail> pcList = new ArrayList<PremierCustomerInfoandRMCodeTaggingDetail>();
        PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = new PremierCustomerInfoandRMCodeTaggingDetail();
        premierCustomerInfoandRMCodeTaggingDetail.setCifNo("1");
        premierCustomerInfoandRMCodeTaggingDetail.setOldIsPremier("1");
        pcList.add(premierCustomerInfoandRMCodeTaggingDetail);
        return pcList;
    }

    // New record to be inserted to suspense
    private BatchSuspense createBatchSuspense() {
        BatchSuspense batchSuspense = new BatchSuspense();

        batchSuspense.setId(1);
        batchSuspense.setJobExecutionId(1);
        batchSuspense.setSuspenseColumn("TestColumn");
        batchSuspense.setSuspenseMessage("TestMessage");
        batchSuspense.setSuspenseRecord("TestRecord");
        batchSuspense.setSuspenseType("TestType");
        batchSuspense.setBatchJobName("TestBatch");

        return batchSuspense;
    }
}
