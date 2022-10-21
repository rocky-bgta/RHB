package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A telemetry operation name.
 */
@SuppressWarnings("serial")
@lombok.Setter
@lombok.Getter
@Entity
@Table(name = "DCP_TELEMETRY_LOG")
public class TelemetryLogPayload implements Serializable {

	@EmbeddedId
	private TelemetryLogPK id;
	
    @Column(name = "AUDIT_DATE_TIME", nullable = false)
    private Timestamp auditDateTime;

    @Column(name = "PAYLOAD", nullable = true)
    private byte[] payload;

    @Column(name = "AUDIT_PARAM", nullable = true)
    private String auditParam;

    @Column(name = "HOSTNAME", nullable = true)
    private String hostname;

    @Column(name = "CIS_NUMBER", nullable = true)
    private String cisNumber;

    @Column(name = "DEVICE_ID", nullable = true)
    private String deviceId;
    
}
