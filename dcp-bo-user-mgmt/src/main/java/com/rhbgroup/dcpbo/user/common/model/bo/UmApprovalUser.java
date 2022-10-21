package com.rhbgroup.dcpbo.user.common.model.bo;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@lombok.Getter
@lombok.Setter
@Entity
@Table(name = "TBL_BO_UM_APPROVAL_USER")
public class UmApprovalUser implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID", nullable = false)
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
	private Timestamp createdTime;

	@Column(name = "CREATED_BY", nullable = false)
	private String createdBy;

	@Column(name = "UPDATED_TIME", nullable = false)
	private Timestamp updatedTime;

	@Column(name = "UPDATED_BY", nullable = false)
	private String updatedBy;
}
