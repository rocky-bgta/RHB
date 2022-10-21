package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "TBL_LOAN_PROFILE")
public class LoanProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "USER_ID", nullable = false)
    private Integer userId;

    @Column(name = "ACCOUNT_NO", nullable = false)
    private String accountNo;

    @Column(name = "NICKNAME", nullable = false)
    private String nickname;

    @Column(name = "LOAN_PRODUCT_ID", nullable = false)
    private Integer loanProductId;

    @Column(name = "IS_HIDDEN", nullable = false)
    private Boolean isHidden;
}
