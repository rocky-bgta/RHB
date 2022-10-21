package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "DCP_AUDIT_SUMMARY_CONFIG")
public class AuditSummaryConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true)
    private Integer id;

    @Column(name = "EVENT_CODE", nullable = false)
    private String eventCode;

    @Column(name = "PATH")
    private String path;

    @Column(name = "TYPE", nullable = false)
    private String type;
    
    @Column(name = "FIELD_NAME", nullable = false)
    private String fieldName;
}
