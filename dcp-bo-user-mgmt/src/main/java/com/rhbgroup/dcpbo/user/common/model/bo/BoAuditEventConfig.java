package com.rhbgroup.dcpbo.user.common.model.bo;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * A user.
 */
@lombok.Getter
@lombok.Setter
@Entity
@Table(name = "TBL_BO_AUDIT_EVENT_CONFIG")
public class BoAuditEventConfig implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "event_code", nullable = false)
    private String eventCode;

    @Column(name = "function_id")
    private Integer functionId;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "activity_name", nullable = false)
    private String activityName;

    @Column(name = "details_table_name")
    private String detailsTableName;

}