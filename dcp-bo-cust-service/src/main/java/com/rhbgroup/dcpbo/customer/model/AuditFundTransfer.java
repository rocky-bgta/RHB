package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "DCP_AUDIT_FUND_TRANSFER")
public class AuditFundTransfer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true)
    private Integer id;

    @Column(name = "AUDIT_ID", nullable = false)
    private Integer auditId;

    @Column(name = "DETAILS", columnDefinition = "nvarchar(4000)", nullable = false)
    private String details;

}
