package com.rhbgroup.dcpbo.customer.dcpbo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Setter
@Getter
@Entity
public class BoAuditDetails {

    @Id
    @Column(name="id")
    private Integer id;
    @Column(name="audit_id")
    private Integer auditId;
    @Column(name="details")
    private String details;
}
