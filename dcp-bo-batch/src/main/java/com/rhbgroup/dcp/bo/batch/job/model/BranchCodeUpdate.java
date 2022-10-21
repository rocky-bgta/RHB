package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchCodeUpdate {

    // Data from staging table TBL_BATCH_STAGED_BNM_BRANCHCODE
    int jobExecutionId;
    String fileName;
    String hdDate;
    String hdTime;
    String recordType;
    String bnmBranchCode;
    String rhbBranchCode;
    String rhbBranchName;
    String rhbBranchAdd1;
    String rhbBranchAdd2;
    String rhbBranchAdd3;
    String extra1;
    String extra2;
    String extra3;
    boolean isProcessed;
    String createdTime;
    String updatedTime;

    // Data from TBL_BNM_CTRL3
    String bnm;
    String ctrl3;

    // Data needed for processing
    boolean headerIsExist = false;
    boolean footerIsExist = false;
    int footerRecordCount=0;
}
