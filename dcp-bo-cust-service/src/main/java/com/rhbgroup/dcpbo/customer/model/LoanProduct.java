package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "TBL_LOAN_PRODUCT")
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "LOAN_TYPE", nullable = false)
    private String loanType;

    @Column(name = "PRODUCT_CODE", nullable = false)
    private String productCode;

    @Column(name = "PRODUCT_NAME", nullable = false)
    private String productName;

    @Column(name = "IS_ISLAMIC", nullable = false)
    private Boolean isIslamic;

    @Column(name = "IS_EQUITY", nullable = false)
    private Boolean isEquity;

    @Column(name = "IS_FLEXI", nullable = false)
    private Boolean isFlexi;

    @Column(name = "CREATED_TIME", nullable = false)
    private Date createdTime;

    @Column(name = "CREATED_BY", nullable = false)
    private String createdBy;

    @Column(name = "UPDATED_TIME", nullable = false)
    private Date updatedTime;

    @Column(name = "UPDATED_BY", nullable = false)
    private String updatedBy;
}
