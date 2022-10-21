package com.rhbgroup.dcpbo.user.common.model.bo;

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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_BO_APPROVAL")
public class BoUserApproval implements Serializable {

	private static final long serialVersionUID = 3374569044053624774L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Integer id;

	@Column(name = "FUNCTION_ID", nullable = false)
	private Integer functionId;

	@Column(name = "CREATOR_ID", nullable = false)
	private Integer creatorId;

	@Column(name = "DESCRIPTION", nullable = false)
	private String description;

	@Column(name = "ACTION_TYPE", nullable = false)
	private String actionType;

	@Column(name = "STATUS", nullable = false)
	private String status;

	@Column(name = "REASON")
	private String reason;

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
