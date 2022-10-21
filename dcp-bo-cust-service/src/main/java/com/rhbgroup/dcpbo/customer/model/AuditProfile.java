package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@lombok.Setter
@lombok.Getter
@Entity
@Table(name = "DCP_audit_profile")
public class AuditProfile implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -919233209299835530L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,unique = true )
    private Integer id;

    @Column(name = "audit_id", nullable = false)
    private Integer auditId;

    @Column(name = "details", columnDefinition = "nvarchar(4000)", nullable = false)
    private String details;
}
