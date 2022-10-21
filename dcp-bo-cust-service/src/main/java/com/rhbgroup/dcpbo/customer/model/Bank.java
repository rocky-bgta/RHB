package com.rhbgroup.dcpbo.customer.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * A user.
 */
@lombok.Setter
@lombok.Getter
@Entity
@Table(name = "TBL_bank")
public class Bank implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,unique = true )
    private Integer id;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bank_name_short", nullable = false)
    private String bankNameShort;

    @Column(name = "bank_initials", nullable = false)
    private String bankInitials;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "bank_code_ibg", nullable = false)
    private String bankCodeIbg;

    @Column(name = "bank_code_ibft")
    private String bankCodeIbft;

    @Column(name = "bank_bic")
    private String bankBic;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_time", nullable = false)
    private Date createdTime;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_time", nullable = false)
    private Date updatedTime;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
}
