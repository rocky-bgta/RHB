package com.rhbgroup.dcpbo.customer.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Getter
@Setter
@Entity
@Table(name = "DCP_AUDIT_EVENT_CONFIG")
public class AuditEventConfig implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true)
    private Integer id;

    @Column(name = "EVENT_CODE", nullable = false)
    private String eventCode;

    @Column(name = "EVENT_NAME", nullable = false)
    private String eventName;

    @Column(name = "EVENT_CATEGORY_ID")
    private Integer eventCategoryId;

    @Column(name = "DETAILS_TABLE_NAME", nullable = false)
    private String detailsTableName;

}