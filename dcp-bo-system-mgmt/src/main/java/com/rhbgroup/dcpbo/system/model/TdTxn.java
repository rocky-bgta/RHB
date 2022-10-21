package com.rhbgroup.dcpbo.system.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name="TBL_TD_TXN")
public class TdTxn implements Serializable{

	@Id
    @Column(name="ID") @NotNull
    private Integer id;
    
    @Column(name="TXN_TOKEN_ID") @NotNull
    private Integer txnTokenId;
    
    @Column(name="TENURE") @NotNull
    private Integer tenure;
    
    @Column(name="AUTO_RENEWAL")
    private Boolean autoRenewal;   
    
    @Column(name="IS_JOINT_OWNERSHIP")
    private Boolean isJointOwnership;   
    
    @Column(name="IS_CREDIT_TO_PRINCIPLE")
    private Boolean isCreditToPrinciple; 
    
    @Column(name="TD_CATEGORY_NAME") @NotNull
    private String tdCategoryName; 
    
    @Column(name="IS_STAFF") @NotNull
    private Boolean isStaff;   
    
    @Column(name="IS_ISLAMIC") @NotNull
    private Boolean isIslamic;    
    
    @Column(name="INTEREST_RATE") @NotNull
    private BigDecimal interestRate;   
    
    @Column(name="INTEREST_AMOUNT") @NotNull
    private BigDecimal interestAmount;  
    
    @Column(name="TD_PRODUCT_CODE") @NotNull
    private String tdProductCode;  
    
    @Column(name="TD_PRODUCT_NAME") @NotNull
    private String tdProductName;  
    
    @Column(name="MATURITY_DATE")
    private Timestamp maturityDate; 
    
    @Column(name="EFFECTIVE_DATE") @NotNull
    private Timestamp effectiveDate; 
    
    @Column(name="TOTAL_AMOUNT")
    private BigDecimal totalAmount;
    
    @Column(name="UPDATED_BY") @Size(max=50) @NotNull
    private String updatedBy;
    
    @Column(name="CREATED_BY") @Size(max=50) @NotNull
    private String createdBy;
    
    @Column(name="CREATED_TIME") @NotNull
    private Timestamp createdTime;

    @Column(name="UPDATED_TIME") @NotNull
    private Timestamp updatedTime;
}
