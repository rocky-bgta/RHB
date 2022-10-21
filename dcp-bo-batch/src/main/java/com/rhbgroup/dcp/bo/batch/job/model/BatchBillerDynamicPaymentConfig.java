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
public class BatchBillerDynamicPaymentConfig {
    private int id;
    private String billerCode;
    private String templateName;
    private String ibkFtpFolder;
    private String ftpFolder;
    private String fileNameFormat;
    private String reportUnitUri;
    private String status;
    private boolean isRequiredToExecute;
    private Date createdTime;
    private String createdBy;
    private Date updatedTime;
    private String updatedBy;
}