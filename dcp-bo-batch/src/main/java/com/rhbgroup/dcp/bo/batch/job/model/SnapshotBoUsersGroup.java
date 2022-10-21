package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SnapshotBoUsersGroup {

    // Data for table TBL_SNAPSHOT_BO_USER
    int jobExecutionId;
    String deptName;
    String userId;
    String userName;
    String userGroup;
    String role;
    String status;
    String userCreatedDate;
    String userCreatedTime;
    String userUpdatedTime;
    String userUpdatedBy;
    String lastLoginDate;
    String lastLoginTime;
    String updatedTime;
    String updatedBy;
    String createdTime;
    String createdBy;

    // Data for TBL_SNAPSHOT_BO_USER_GROUP
    String functionName;
}
