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
@Table(name = "TBL_BO_UM_APPROVAL_USER")
public class BoUmApprovalUser implements Serializable {

	private static final long serialVersionUID = -5547971197483357105L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true)
	private Integer id;

	@Column(name = "APPROVAL_ID", nullable = false)
	private Integer approvalId;

	@Column(name = "STATE", nullable = false)
	private String state;

	@Column(name = "LOCKING_ID", nullable = false)
	private String lockingId;

	@Column(name = "PAYLOAD", nullable = false)
	private String payload;

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
