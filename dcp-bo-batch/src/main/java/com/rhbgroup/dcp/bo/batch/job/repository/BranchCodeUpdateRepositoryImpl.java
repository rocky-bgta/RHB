package com.rhbgroup.dcp.bo.batch.job.repository;

import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import com.rhbgroup.dcp.bo.batch.job.model.BatchSuspense;
import com.rhbgroup.dcp.bo.batch.job.model.BranchCodeUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.batch.core.StepExecution;
import org.apache.log4j.Logger;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_NAME_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_TIME_FORMAT;

@Component
public class BranchCodeUpdateRepositoryImpl extends BaseRepositoryImpl {
    static final Logger logger = Logger.getLogger(BranchCodeUpdateRepositoryImpl.class);
    @Qualifier("dataSourceDCP")
    @Autowired
    DataSource dataSourceDCP;
    @Autowired
    private DcpBatchApplicationContext dcpBatchApplicationContext;

    public Boolean addToStaging(BranchCodeUpdate branchCodeUpdate) {
        try {

            getJdbcTemplate().update("INSERT INTO TBL_BATCH_STAGED_BNM_BRANCH_CODE (job_execution_id,file_name,hd_date,hd_time,record_type,bnm_branch_code,rhb_branch_code,rhb_branch_name,rhb_branch_add1,rhb_branch_add2,rhb_branch_add3,extra1,extra2,extra3,created_time,updated_time) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                    , new Object[]{
                            branchCodeUpdate.getJobExecutionId()
                            , branchCodeUpdate.getFileName()
                            , branchCodeUpdate.getHdDate()
                            , branchCodeUpdate.getHdTime()
                            , branchCodeUpdate.getRecordType()
                            , branchCodeUpdate.getBnmBranchCode()
                            , branchCodeUpdate.getRhbBranchCode()
                            , branchCodeUpdate.getRhbBranchName()
                            , branchCodeUpdate.getRhbBranchAdd1()
                            , branchCodeUpdate.getRhbBranchAdd2()
                            , branchCodeUpdate.getRhbBranchAdd3()
                            , branchCodeUpdate.getExtra1()
                            , branchCodeUpdate.getExtra2()
                            , branchCodeUpdate.getExtra3()
                            , branchCodeUpdate.getCreatedTime()
                            , branchCodeUpdate.getUpdatedTime()
                    });
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public Boolean insertBranchCode(BranchCodeUpdate branchCodeUpdate) {
        try {

            JdbcTemplate jdbcTemplateDCP = new JdbcTemplate();
            jdbcTemplateDCP.setDataSource(dataSourceDCP);

            String createdTime = new SimpleDateFormat(DEFAULT_DATE_FORMAT+" "+DEFAULT_TIME_FORMAT).format(new Date());
            String createdBy = "Branch Code Update (LDCPW9003T)";

            jdbcTemplateDCP.update("INSERT INTO TBL_BNM_CTRL3 (bnm,ctrl3,created_time,created_by,updated_time,updated_by) values (?,?,?,?,?,?)"
                    , new Object[]{
                            branchCodeUpdate.getBnmBranchCode()
                            , branchCodeUpdate.getRhbBranchCode()
                            , createdTime
                            , createdBy
                            , createdTime
                            , createdBy
                    });
            logger.info("Added new pair of bnm code and ctrl3 code "+branchCodeUpdate.getBnmBranchCode() +", " + branchCodeUpdate.getRhbBranchCode());
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public Boolean updateBranchCode(BranchCodeUpdate branchCodeUpdate) {
        try {

            JdbcTemplate jdbcTemplateDCP = new JdbcTemplate();
            jdbcTemplateDCP.setDataSource(dataSourceDCP);

            String updatedTime = new SimpleDateFormat(DEFAULT_DATE_FORMAT+" "+DEFAULT_TIME_FORMAT).format(new Date());
            String updatedBy = "Branch Code Update (LDCPW9003T)";

            jdbcTemplateDCP.update("UPDATE TBL_BNM_CTRL3 SET ctrl3=?,updated_time=?,updated_by=? WHERE bnm=?"
                    , new Object[]{
                            branchCodeUpdate.getRhbBranchCode()
                            , updatedTime
                            , updatedBy
                            , branchCodeUpdate.getBnmBranchCode()
                    });
            logger.info("Changed ctrl3 branch code from "+ branchCodeUpdate.getCtrl3()+ " to " + branchCodeUpdate.getRhbBranchCode());
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public Boolean addSuspense(StepExecution stepExecution, BatchSuspense batchSuspense) {

        String jobName=stepExecution.getJobExecution().getJobParameters().getString(BATCH_JOB_PARAMETER_JOB_NAME_KEY);
        long jobExecutionId= stepExecution.getJobExecution().getId().longValue();

        batchSuspense.setCreatedTime(new Date());
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

    public Boolean updateIsProcessed(String jobExecutionId, String bnmBranchCode, boolean isProcessed) {
        try {
            Map<String,String> initialJobArgs = dcpBatchApplicationContext.getInitialJobArguments();
            String jobexecutionid="";

            if(null !=initialJobArgs) {
                jobexecutionid = initialJobArgs.get(BATCH_JOB_PARAMETER_JOB_EXECUTION_ID_KEY);
            }

            if(StringUtils.isEmpty(jobexecutionid)) {
                jobexecutionid = jobExecutionId;
            }
            String updatedTime = new SimpleDateFormat(DEFAULT_DATE_FORMAT+" "+DEFAULT_TIME_FORMAT).format(new Date());
            String sql = "UPDATE TBL_BATCH_STAGED_BNM_BRANCH_CODE SET is_processed=?, updated_time=? WHERE bnm_branch_code=? AND job_execution_id=?";

            getJdbcTemplate().update(sql
                    , new Object[]{
                            isProcessed,
                            updatedTime,
                            bnmBranchCode,
                            jobexecutionid
                    });
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }
}