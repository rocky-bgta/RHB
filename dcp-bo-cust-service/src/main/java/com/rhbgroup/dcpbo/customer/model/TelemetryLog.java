package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@SuppressWarnings("serial")
@lombok.Setter
@lombok.Getter
@Entity
@Table(name = "DCP_TELEMETRY_LOG")
public class TelemetryLog implements Serializable {

	@EmbeddedId
	private TelemetryLogPK id;

    @Column(name = "AUDIT_DATE_TIME", nullable = false)
    private Date auditDateTime;
   
    @Column(name = "USERNAME", nullable = true)
    private String username;
    
    @Column(name = "TOTAL_ERROR", nullable = true)
    private String totalError;
    
    public String getAuditDateTimeString() {
    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    	String strDate = dateFormat.format(getAuditDateTime()); 
    	return strDate;
    }

}
