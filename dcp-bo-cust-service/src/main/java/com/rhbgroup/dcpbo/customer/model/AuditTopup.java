package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "DCP_AUDIT_TOPUP")
public class AuditTopup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,unique = true )
    private Integer id;

    @Column(name = "audit_id", nullable = false)
    private Integer auditId;

    @Column(name = "details", columnDefinition = "nvarchar(4000)", nullable = false)
    private String details;

}
