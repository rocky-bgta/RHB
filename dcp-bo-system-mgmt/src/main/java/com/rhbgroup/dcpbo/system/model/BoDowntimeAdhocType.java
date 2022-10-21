package com.rhbgroup.dcpbo.system.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A user.
 */
@SuppressWarnings("serial")
@lombok.Getter
@lombok.Setter
@lombok.ToString
@Entity
@Table(name = "TBL_BO_DOWNTIME_ADHOC_TYPE")
public class BoDowntimeAdhocType implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id; 

    @Column(name = "ADHOC_TYPE", nullable = false)
    private String adhocType;
    
    @Column(name = "ADHOC_TYPE_CATEGORY", nullable = false)
    private String adhocTypeCategory;
    
    @Column(name = "ADHOC_TYPE_NAME", nullable = false)
    private String adhocTypeName;
    
    @Column(name = "CREATED_TIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "CREATED_BY", nullable = false)
	private String createdBy;

	@Column(name = "UPDATED_TIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

	@Column(name = "UPDATED_BY", nullable = false)
	private String updatedBy;

}