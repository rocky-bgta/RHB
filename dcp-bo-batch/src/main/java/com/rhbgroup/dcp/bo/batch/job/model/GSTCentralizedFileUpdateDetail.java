package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

import com.rhbgroup.dcp.bo.batch.framework.model.BaseModel;

@Setter
@Getter
public class GSTCentralizedFileUpdateDetail extends BaseModel {

    // Data for GST Table
    private String entityCode;
    private String entityIndicator;
    private String uniqueId;
    private String sourceSystemId;
    private String transactionIdentifier;
    private String transactionDescription;
    private String gstRate;
    private String treatmentType;
    private String taxCode;
    private String calculationMethod;
    private String glAccountCodeCharges;
    private String startDate;
    private String endDate;
    private String lastUpdateDate;
    private String lastUpdateTime;
    private String lastUpdateBy;
    private String hostTranOrGSTGLCode;
    private String filler;

    // Partial data for Staging rest will be the same with GST table
    private String fileName;
    private String hdDate;
    private String hdTime;
    private String recordIndicator;

    // Data from DCP Table for update and insert
    private String mainFunction;
    private String subFunction;
    private String paymentMethod;
    private String beginDate;
    private String createdTime;
    private String createdBy;
    private String updateTime;
    private String updateBy;

    // Use for DCP table mapper
    private String txnIdentifier;
    private String txnDescription;
    private String sourceSystem;

    // User to compare unique id and comparing data to insert
    private String oldGstMaxUniqueId;
    private String oldGstTxnIdentifier;
    private String oldGstSourceSystem;
    private String newGstMaxUniqueId;
    private String txnType;
}