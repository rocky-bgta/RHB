package com.rhbgroup.dcp.bo.batch.job.repository;

import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.SnapshotBoUsersGroup;
import org.springframework.stereotype.Component;
import org.apache.log4j.Logger;
import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;

import java.util.Date;
import java.util.List;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_TIME_FORMAT;

@Component
public class SnapshotBoUsersGroupRepositoryImpl extends BaseRepositoryImpl {
    static final Logger logger = Logger.getLogger(SnapshotBoUsersGroupRepositoryImpl.class);

    // Delete duplicate records in TBL_SNAPSHOT_BO_USER_GROUP
    public Boolean deleteUserGroupSameDayRecords(int jobExecutionId, Date createdDate ) {
        try{
            Date startDate = createdDate;
            Date endDate = DateUtils.addDays(createdDate, 1);
            String startDateStr = DateUtils.formatDateString(startDate, DEFAULT_DATE_FORMAT);
            String endDateStr = DateUtils.formatDateString(endDate, DEFAULT_DATE_FORMAT);

            // Delete duplicate records in TBL_SNAPSHOT_BO_USER_GROUP
            List tempList = getJdbcTemplate().queryForList(String.format("SELECT TOP 1 id FROM TBL_SNAPSHOT_BO_USER_GROUP WHERE (created_time >='%s' AND created_time <'%s')", startDateStr, endDateStr));
            if (!tempList.isEmpty()) {
                String deleteStatement = String.format("DELETE FROM TBL_SNAPSHOT_BO_USER_GROUP WHERE NOT job_execution_id=%s AND (created_time >='%s' AND created_time <'%s')", jobExecutionId, startDateStr, endDateStr);
                logger.trace("Deleting duplicate records in table [TBL_SNAPSHOT_BO_USER_GROUP]");
                getJdbcTemplate().execute(deleteStatement);
            }

            // Delete duplicate records in TBL_SNAPSHOT_BO_USER
            tempList = getJdbcTemplate().queryForList(String.format("SELECT TOP 1 id FROM TBL_SNAPSHOT_BO_USER WHERE (created_time >='%s' AND created_time <'%s')", startDateStr, endDateStr));
            if (!tempList.isEmpty()) {
                String deleteStatement = String.format("DELETE FROM TBL_SNAPSHOT_BO_USER WHERE NOT job_execution_id=%s AND (created_time >='%s' AND created_time <'%s')", jobExecutionId, startDateStr, endDateStr);
                logger.trace("Deleting duplicate records in table [TBL_SNAPSHOT_BO_USER]");
                getJdbcTemplate().execute(deleteStatement);
            }
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public Boolean insertBoUserGroup(SnapshotBoUsersGroup snapshotBoUsersGroup) {
        try {
            getJdbcTemplate().update("INSERT INTO TBL_SNAPSHOT_BO_USER_GROUP (job_execution_id,user_group,role,function_name,created_time,created_by) values (?,?,?,?,?,?)"
                    , new Object[]{
                            snapshotBoUsersGroup.getJobExecutionId()
                            , snapshotBoUsersGroup.getUserGroup()
                            , snapshotBoUsersGroup.getRole()
                            , snapshotBoUsersGroup.getFunctionName()
                            , snapshotBoUsersGroup.getCreatedTime()
                            , snapshotBoUsersGroup.getCreatedBy()
                    });
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    public Boolean insertBoUser(SnapshotBoUsersGroup snapshotBoUsersGroup) {
        try {
            getJdbcTemplate().update("INSERT INTO TBL_SNAPSHOT_BO_USER (job_execution_id,dept_name,user_id,user_name,user_group,role,status,user_created_date,user_created_time,user_updated_time,user_updated_by,last_login_date,last_login_time,created_time,created_by) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                    , new Object[]{
                            snapshotBoUsersGroup.getJobExecutionId()
                            , snapshotBoUsersGroup.getDeptName()
                            , snapshotBoUsersGroup.getUserId()
                            , snapshotBoUsersGroup.getUserName()
                            , snapshotBoUsersGroup.getUserGroup()
                            , snapshotBoUsersGroup.getRole()
                            , snapshotBoUsersGroup.getStatus()
                            , snapshotBoUsersGroup.getUserCreatedDate()
                            , snapshotBoUsersGroup.getUserCreatedTime()
                            , snapshotBoUsersGroup.getUserUpdatedTime()
                            , snapshotBoUsersGroup.getUserUpdatedBy()
                            , snapshotBoUsersGroup.getLastLoginDate()
                            , snapshotBoUsersGroup.getLastLoginTime()
                            , snapshotBoUsersGroup.getCreatedTime()
                            , snapshotBoUsersGroup.getCreatedBy()
                    });
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }
}