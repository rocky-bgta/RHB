package com.rhbgroup.dcpbo.user.common.model.dcp;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.UpdatedTimestamp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_FUND_DETAILS")
public class FundDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false,unique = true )
    private Integer id;
    
    @Column(name = "FUND_ID", nullable = false, unique = true, length = 30)
	private String fundId;
    
    @Column(name = "FUND_LONG_NAME", nullable = false, length = 100)
    private String fundLongName;

    @Column(name = "FUND_SHORT_NAME", nullable = false, length = 50)
	private String fundShortName;
	
    @Column(name = "INVESTMENT_TYPE", nullable = false, length = 50)
	private String fundType;
	
    @Column(name = "COLLECTION_ACCT_NUMBER", nullable = false, length = 50)
    private String collectionAccountNumber;
	
    @Column(name = "SUSPENSION_FROM_DATETIME", nullable = false)
    Timestamp suspensionFromDateTime;

    @Column(name = "SUSPENSION_TO_DATETIME", nullable = false)
    Timestamp suspensionToDateTime;	
    
    @Column(name = "IS_ACTIVE", nullable = false,columnDefinition = "TINYINT", length = 1)
    private boolean isActive;

    @Column(name = "CREATED_BY", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "CREATED_TIME", nullable = false)
    @CreatedTimestamp
    Timestamp createdTime;

    @Column(name = "UPDATED_BY", nullable = false, length = 50)
    private String updatedBy;

    @Column(name = "UPDATED_TIME", nullable = false)
    @UpdatedTimestamp
    Timestamp updatedTime;
    
    @Column(name = "BANK_CHARGE_AMOUNT",nullable = false, columnDefinition="Decimal(17,2)" )
    private BigDecimal bankChargeAmount;
    
    @Column(name = "BANK_CHARGE_PERCENTAGE",nullable = false, columnDefinition="Decimal(17,2)" )
    private BigDecimal bankChargePercentage;
    
    @Column(name = "IMAGE_URL",nullable = true, length = 250 )
    private String imageUrl;
}
