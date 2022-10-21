package com.rhbgroup.dcp.bo.batch.test.repository;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.job.model.BatchLookup;
import com.rhbgroup.dcp.bo.batch.job.model.GSTCentralizedFileUpdateDetail;
import com.rhbgroup.dcp.bo.batch.job.repository.GSTCentralizedFileUpdateRepositoryImpl;
import freemarker.template.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.repository.BatchSuspenseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;

import javax.sql.DataSource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class GSTCentralizedFileUpdateRepositoryImplTest extends BaseJobTest {

    @Autowired
    private GSTCentralizedFileUpdateRepositoryImpl gstCentralizedFileUpdateRepository;

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
    public void testAddGSTCentralizedFileUpdateStaging() {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = createGSTDetailA();

        when(mockJdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        assertEquals(false, gstCentralizedFileUpdateRepository.addGSTCentralizedFileUpdateStaging(gstCentralizedFileUpdateDetail));
    }

    @Test
    public void testGetUnprocessedGSTCentralizedStatusFromStaging() {
        List<GSTCentralizedFileUpdateDetail> gstCentralizedFileUpdateDetailList = new ArrayList<GSTCentralizedFileUpdateDetail>();
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = createGSTDetailA();

        gstCentralizedFileUpdateDetailList.add(gstCentralizedFileUpdateDetail);

        when(mockJdbcTemplate.query(anyString(), any(Object[].class), any(BeanPropertyRowMapper.class))).thenReturn(gstCentralizedFileUpdateDetailList);

        assertEquals(gstCentralizedFileUpdateDetailList, gstCentralizedFileUpdateRepository.getUnprocessedGSTCentralizedStatusFromStaging("99999"));
    }

    @Test
    public void testGetBatchLookUpValue() {
        List<BatchLookup> lookUpGSTList = new ArrayList<>();
        BatchLookup batchLookup = createBatchLookup();

        lookUpGSTList.add(batchLookup);

        when(mockJdbcTemplate.query(anyString(), any(Object[].class), any(BeanPropertyRowMapper.class))).thenReturn(lookUpGSTList);

        assertEquals(lookUpGSTList, gstCentralizedFileUpdateRepository.getBatchLookUpValue(""));
    }

    @Test
    public void testGetGSTFromDCP() {
        List<GSTCentralizedFileUpdateDetail> gstCentralizedFileUpdateDetailList = new ArrayList<GSTCentralizedFileUpdateDetail>();
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = createGSTDetailA();

        gstCentralizedFileUpdateDetailList.add(gstCentralizedFileUpdateDetail);

        when(mockJdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class))).thenReturn(gstCentralizedFileUpdateDetailList);

        assertEquals(gstCentralizedFileUpdateDetailList, gstCentralizedFileUpdateRepository.getGSTFromDCP());
    }

    @Test
    public void testPositiveInsertTblBatchSuspense() throws BatchException {
        BatchSuspense batchSuspense = createBatchSuspense();

        when(batchSuspenseRepository.addBatchSuspenseToDB(batchSuspense)).thenReturn(1);

        assertEquals(1, gstCentralizedFileUpdateRepository.insertTblBatchSuspense(batchSuspense));
    }

    @Test
    public void testNegativeInsertTblBatchSuspense() throws BatchException {
        BatchSuspense batchSuspense = new BatchSuspense();

        assertEquals(0, gstCentralizedFileUpdateRepository.insertTblBatchSuspense(batchSuspense));
    }

    @Test
    public void testPositiveUpdateGSTDCP() throws BatchException {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = createGSTDetailA();
        String setSQL = "";
        List<String> parameterToUpdate = new ArrayList<String>();
        parameterToUpdate.add("Test");
        parameterToUpdate.add("Test2");
        parameterToUpdate.add("Test3");
        parameterToUpdate.add("Test4");
        parameterToUpdate.add("Test5");

        when(mockJdbcTemplate.update(anyString(), any(Object.class))).thenReturn(1);

        assertEquals(true, gstCentralizedFileUpdateRepository.updateGSTDCP(setSQL, parameterToUpdate));
    }

    @Test (expected =  NullPointerException.class)
    public void testNegativeUpdateGSTDCP() throws BatchException {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = createGSTDetailA();
        String setSQL = "";
        List<String> parameterToUpdate = null;

        gstCentralizedFileUpdateRepository.updateGSTDCP(setSQL, parameterToUpdate);
    }

    @Test
    public void testPositiveInsertNewGSTToDB() throws BatchException {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = createGSTDetailA();
        String setSQL = "";
        List<String> parameterToUpdate = null;

        when(mockJdbcTemplate.update(anyString(), anyString(),
                anyString(), anyString(),
                anyString(), anyString(),
                anyString(), anyString(),
                anyString(), anyString(),
                anyString(), anyString(),
                anyString(), anyString(),
                anyString(), anyString(),
                anyString(), anyString(),
                anyString(), anyString(),
                anyString(), anyString()
        )).thenReturn(1);

        assertEquals(true, gstCentralizedFileUpdateRepository.insertNewGSTToDB(gstCentralizedFileUpdateDetail));
    }

    @Test (expected = BatchException.class)
    public void testNegativeInsertNewGSTToDB() throws BatchException {
        assertEquals(false, gstCentralizedFileUpdateRepository.insertNewGSTToDB(any()));
    }

    @Test
    public void testPositiveGetGSTConfigEssentialData(){
        List<GSTCentralizedFileUpdateDetail> gstCentralizedFileUpdateDetailList = new ArrayList<GSTCentralizedFileUpdateDetail>();
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = createGSTDetailA();

        gstCentralizedFileUpdateDetailList.add(gstCentralizedFileUpdateDetail);

        when(mockJdbcTemplate.query(anyString(), any(Object[].class),any(BeanPropertyRowMapper.class))).thenReturn(gstCentralizedFileUpdateDetailList);

        assertEquals(gstCentralizedFileUpdateDetailList, gstCentralizedFileUpdateRepository.getGSTConfigEssentialData("Test", "Test", "Test"));
    }

    @Test
    public void testPositiveCheckGSTConfigNewRecord(){
        List<GSTCentralizedFileUpdateDetail> gstCentralizedFileUpdateDetailList = new ArrayList<GSTCentralizedFileUpdateDetail>();
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = createGSTDetailA();

        gstCentralizedFileUpdateDetailList.add(gstCentralizedFileUpdateDetail);

        when(mockJdbcTemplate.query(anyString(), any(Object[].class),any(BeanPropertyRowMapper.class))).thenReturn(gstCentralizedFileUpdateDetailList);

        assertEquals(gstCentralizedFileUpdateDetailList, gstCentralizedFileUpdateRepository.checkGSTConfigNewRecord("Test"));
    }

    @Test
    public void testPositiveGetGSTNewValue(){
        List<GSTCentralizedFileUpdateDetail> gstCentralizedFileUpdateDetailList = new ArrayList<GSTCentralizedFileUpdateDetail>();
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = createGSTDetailA();

        gstCentralizedFileUpdateDetailList.add(gstCentralizedFileUpdateDetail);

        when(mockJdbcTemplate.query(anyString(), any(Object[].class),any(BeanPropertyRowMapper.class))).thenReturn(gstCentralizedFileUpdateDetailList);

        assertEquals(gstCentralizedFileUpdateDetail, gstCentralizedFileUpdateRepository.getGSTNewValue("Test","Test","Test","Test"));
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

        assertEquals(1, gstCentralizedFileUpdateRepository.updateProcessStatus(parameterToUpdate));
    }

    @Test (expected =  NullPointerException.class)
    public void testNegativeUpdateProcessStatus() {
        List<String> parameterToUpdate = null;

        gstCentralizedFileUpdateRepository.updateProcessStatus(parameterToUpdate);
    }

    // New record to be inserted gst
    private GSTCentralizedFileUpdateDetail createGSTDetailA() {
        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail = new GSTCentralizedFileUpdateDetail();
        gstCentralizedFileUpdateDetail.setOldGstMaxUniqueId("1");
        gstCentralizedFileUpdateDetail.setOldGstSourceSystem("IBK");
        gstCentralizedFileUpdateDetail.setOldGstTxnIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setNewGstMaxUniqueId("2");
        gstCentralizedFileUpdateDetail.setJobExecutionId("999999");
        gstCentralizedFileUpdateDetail.setRecordIndicator("1");
        gstCentralizedFileUpdateDetail.setUniqueId("1");
        gstCentralizedFileUpdateDetail.setSourceSystemId("IBK");
        gstCentralizedFileUpdateDetail.setTransactionIdentifier("NIBK2003_A");
        gstCentralizedFileUpdateDetail.setTransactionDescription("FUND TRANSFER TO OWN ACCOUNT BELOW THRESHOLD");
        gstCentralizedFileUpdateDetail.setGstRate("00.0000");
        gstCentralizedFileUpdateDetail.setTreatmentType("04");
        gstCentralizedFileUpdateDetail.setTaxCode("OS");
        gstCentralizedFileUpdateDetail.setCalculationMethod("I");
        gstCentralizedFileUpdateDetail.setGlAccountCodeCharges("");
        gstCentralizedFileUpdateDetail.setStartDate("20150320");
        gstCentralizedFileUpdateDetail.setEndDate("20150331");
        gstCentralizedFileUpdateDetail.setLastUpdateDate("20180515");
        gstCentralizedFileUpdateDetail.setUpdateTime("175359");
        gstCentralizedFileUpdateDetail.setLastUpdateBy("402849");
        gstCentralizedFileUpdateDetail.setEntityCode("");
        gstCentralizedFileUpdateDetail.setEntityIndicator("");

        return gstCentralizedFileUpdateDetail;
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

    // New record to be inserted to lookup
    private BatchLookup createBatchLookup() {
        BatchLookup batchLookup = new BatchLookup();

        batchLookup.setGroup("TestGroup");
        batchLookup.setValue("TestValue");

        return batchLookup;
    }
}
