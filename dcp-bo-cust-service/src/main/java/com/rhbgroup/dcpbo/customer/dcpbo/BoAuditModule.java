package com.rhbgroup.dcpbo.customer.dcpbo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Setter
@Getter
@Entity
public class BoAuditModule {

    @Id
    @Column(name="audit_id")
    private Integer id;
    @Column(name="event_id")
    private Integer eventId;
    @Column(name="details_table_name")
    private String detailsTableName;
    @Column(name="function_id")
    private Integer functionId;
    @Column(name="activity_name")
    private String activityName;
    @Column(name="username")
    private String username;
    @Column(name="current_ts")
    private String timestamp;

}
