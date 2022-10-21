package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import java.io.Serializable;
import java.sql.Blob;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Getter
@Setter
@Embeddable
public class TelemetryLogPK implements Serializable {
	
    @Column(name = "MESSAGE_ID", nullable = true)
    private String messageId;
    
    @Column(name = "AUDIT_TYPE", nullable = true)
    private String auditType;
    
    @Column(name = "OPERATION_NAME", nullable = true)
    private String operationName;
    
}
