package com.rhbgroup.dcpbo.customer.dcpbo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A telemetry audit type.
 */
@SuppressWarnings("serial")
@lombok.Setter
@lombok.Getter
@Entity
@Table(name = "TBL_telemetry_audit_type")
public class TelemetryAuditType implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,unique = true )
    private Integer id;

    @Column(name = "audit_type", nullable = false)
    private String auditType;

    @Column(name = "created_time", nullable = false)
    private Date createdTime;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "updated_time", nullable = false)
    private Date updatedTime;

    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
}
