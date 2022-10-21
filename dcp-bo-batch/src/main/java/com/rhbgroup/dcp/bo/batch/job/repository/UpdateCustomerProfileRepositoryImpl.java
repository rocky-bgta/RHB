package com.rhbgroup.dcp.bo.batch.job.repository;

import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.NewOldCustomerProfile;
import com.rhbgroup.dcp.bo.batch.job.model.UpdateCustomerProfile;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.*;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;

@Component
public class UpdateCustomerProfileRepositoryImpl extends BaseRepositoryImpl {
    static final Logger logger = Logger.getLogger(UpdateCustomerProfileRepositoryImpl.class);
    @Qualifier("dataSourceDCP")
    @Autowired
    DataSource dataSourceDCP;
    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;

    public Boolean addUpdateCustomerProfileStaging(UpdateCustomerProfile updateCustomerProfile) {
        try {

            getJdbcTemplate().update("INSERT INTO TBL_BATCH_STAGED_UPDATE_CUST_PROFILE " +
                            "(job_execution_id,is_processed,file_name,cis_no,race,birth_date," +
                            "gender,staff_indicator,host_customer_type," +
                            "maddress1,maddress2,maddress3,maddress4,postcode,city,state,country) " +
                            "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    updateCustomerProfile.getJobExecutionId()
                    ,updateCustomerProfile.isProcessed()
                    ,updateCustomerProfile.getFileName()
                    ,updateCustomerProfile.getCisNo()
                    ,updateCustomerProfile.getRace()
                    ,updateCustomerProfile.getBirthDate()
                    ,updateCustomerProfile.getGender()
                    ,updateCustomerProfile.getStaffIndicator()
                    ,updateCustomerProfile.getHostCustomerType()
                    ,updateCustomerProfile.getMaddress1()
                    ,updateCustomerProfile.getMaddress2()
                    ,updateCustomerProfile.getMaddress3()
                    ,updateCustomerProfile.getMaddress4()
                    ,updateCustomerProfile.getPostcode()
                    ,updateCustomerProfile.getCity()
                    ,updateCustomerProfile.getState()
                    ,updateCustomerProfile.getCountry()
            );
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public Boolean updateIsProcessed(String jobExecutionId,String cisNo, boolean isProcessed) {
        try {

            Map<String,String> initialJobArgs = dcpBatchApplicationContext.getInitialJobArguments();
            String jobexecutionid="";

            if(null !=initialJobArgs) {
                jobexecutionid = initialJobArgs.get(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY);
            }

            if(StringUtils.isEmpty(jobexecutionid)) {
                jobexecutionid = jobExecutionId;
            }
            String sql = "UPDATE TBL_BATCH_STAGED_UPDATE_CUST_PROFILE SET is_processed=? WHERE cis_no=? AND job_execution_id=?";

            getJdbcTemplate().update(sql
                    , new Object[]{
                            isProcessed,
                            cisNo,
                            jobexecutionid
                    });
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    // Method to update tbl_user_profile in DCP database
    public Boolean updateCustomerProfile(StepExecution stepExecution,NewOldCustomerProfile newOldCustomerProfile) {

        try {

            if(newOldCustomerProfile.getNew_is_staff().equalsIgnoreCase("N"))
                newOldCustomerProfile.setNew_is_staff("0");
            if(newOldCustomerProfile.getNew_is_staff().equalsIgnoreCase("Y"))
                newOldCustomerProfile.setNew_is_staff("1");

            JdbcTemplate jdbcTemplateDCP = new JdbcTemplate();
            jdbcTemplateDCP.setDataSource(dataSourceDCP);
            jdbcTemplateDCP.update("UPDATE dcp.dbo.TBL_USER_PROFILE SET IS_STAFF=?,DATE_OF_BIRTH=?," +
                            "RESIDENTIAL_ADDRESS1=?, RESIDENTIAL_ADDRESS2=?, " +
                            "RESIDENTIAL_ADDRESS3=?, RESIDENTIAL_ADDRESS4=?," +
                            "RESIDENTIAL_CITY=?, RESIDENTIAL_STATE=?, RESIDENTIAL_COUNTRY=?, " +
                            "RESIDENTIAL_POSTCODE=? " +
                            "WHERE CIS_NO=? AND USER_STATUS <> 'I'",
                            newOldCustomerProfile.getNew_is_staff(),
                            newOldCustomerProfile.getNew_birth_date(),
                            newOldCustomerProfile.getNewMAddress1(),
                            newOldCustomerProfile.getNewMAddress2(),
                            newOldCustomerProfile.getNewMAddress3(),
                            newOldCustomerProfile.getNewMAddress4(),
                            newOldCustomerProfile.getNewCity(),
                            newOldCustomerProfile.getNewState(),
                            newOldCustomerProfile.getNewCountry(),
                            newOldCustomerProfile.getNewPostcode(),
                            newOldCustomerProfile.getNew_cis_no()
                    );

            return true;
        } catch (Exception e) {
            logger.error(e);
            BatchSuspense batchSuspense = new BatchSuspense();
            batchSuspense.setSuspenseColumn("N/A");
            batchSuspense.setSuspenseType("EXCEPTION");
            batchSuspense.setSuspenseMessage(e.getMessage());
            addSuspense(stepExecution,batchSuspense);
            stepExecution.getJobExecution().getExecutionContext().putString("jobstatus", "failed");

            return false;
        }
    }

    public Boolean addSuspense(StepExecution stepExecution, BatchSuspense batchSuspense) {

        String jobName=stepExecution.getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
        String batchSystemDateStr=(String)stepExecution.getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
        long jobExecutionId= stepExecution.getJobExecution().getId().longValue();

        try {
            Date batchSystemDate = DateUtils.getDateFromString(batchSystemDateStr, DEFAULT_DATE_FORMAT);
            batchSuspense.setCreatedTime(batchSystemDate);
        }catch (ParseException e){
            logger.error(e);
        }

        batchSuspense.setJobExecutionId(jobExecutionId);
        batchSuspense.setBatchJobName(jobName);

        try {
            getJdbcTemplate().update("INSERT INTO TBL_BATCH_SUSPENSE (JOB_EXECUTION_ID,BATCH_JOB_NAME,CREATED_TIME,SUSPENSE_COLUMN,SUSPENSE_TYPE,SUSPENSE_MESSAGE,SUSPENSE_RECORD) values (?,?,?,?,?,?,?)"
                , new Object[]{
                        batchSuspense.getJobExecutionId()
                        ,batchSuspense.getBatchJobName()
                        ,batchSuspense.getCreatedTime()
                        ,batchSuspense.getSuspenseColumn()
                        ,batchSuspense.getSuspenseType()
                        ,batchSuspense.getSuspenseMessage()
                        ,batchSuspense.getSuspenseRecord()
                });
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public int getCustomerAddressCount(NewOldCustomerProfile newOldCustomerProfile) {

        int row = 0;
        try {
            String sql = "select count(user_id) from dcp.dbo.tbl_user_address where user_id = ?;";
            row = getJdbcTemplate().queryForObject(sql, new Object[] { newOldCustomerProfile.getUser_id() }, Integer.class);
        } catch (Exception ex) {
            logger.error(ex);
        }
        return row;
    }

    public void cleanCustomerProfileStagingTable(){
        int affectedRowCount = jdbcTemplate.update("TRUNCATE TABLE dcpbo.dbo.TBL_BATCH_STAGED_UPDATE_CUST_PROFILE");
        logger.info(String.format("cleaned batch staged update customer profile table (%s rows removed)", affectedRowCount));
    }

}