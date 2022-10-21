package com.rhbgroup.dcpbo.customer.dcpbo;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

@lombok.Setter
@lombok.Getter
@Entity
@Immutable
@Table(name = "VW_BO_INVESTIGATION_AUDIT")
public class AuditDetailsVW implements Serializable {
    @Id
    private Integer id;

    private String event_code;

    private Integer user_id;

    private String status_code;

    private Timestamp timestamp;    

    @Column(columnDefinition = "NVARCHAR")
    private String details;
    
    private String audit_type;

    private String channel;
    
    private String username;
    
    private String status_description;
    
    private String event_name;
}
