package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.Date;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class BatchStagedDailyDeltaNewProfile {
    private long id;
    private Date processingDate;
    private int userId;
    private boolean isProcessed;
    private Date createdTime;
    private Date updatedTime;
    private long jobExecutionId;
}