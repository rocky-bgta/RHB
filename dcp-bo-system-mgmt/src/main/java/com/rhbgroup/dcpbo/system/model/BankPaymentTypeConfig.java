package com.rhbgroup.dcpbo.system.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@lombok.Setter
@lombok.Getter
@Entity
@Table(name = "TBL_BANK_PAYMENT_TYPE_CONFIG")
public class BankPaymentTypeConfig implements Serializable{

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false,unique = true )
    private Integer id;

    @Column(name = "BANK_ID", nullable = false)
    private Integer bankId;

    @Column(name = "PAYMENT_TYPE", nullable = false)
    private String paymentType;

    @Column(name = "ACCOUNT_NUMBER_REGEX", nullable = false)
    private String accountNumberRegex;
    
    @Column(name = "IBG_FLAG", nullable = false)
    private String ibgFlag;
    
    @Column(name = "IBFT_FLAG", nullable = false)
    private String ibftFlag;
	
    @Column(name = "CREATED_TIME", nullable = false)
    private Date createdTime;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "UPDATED_TIME", nullable = false)
    private Date updatedTime;

    @Column(name = "UPDATED_BY", nullable = false)
    private String updatedBy;
    
    @Column(name = "INSTANT_FLAG", nullable = false)
    private String instantFlag;
    
}
