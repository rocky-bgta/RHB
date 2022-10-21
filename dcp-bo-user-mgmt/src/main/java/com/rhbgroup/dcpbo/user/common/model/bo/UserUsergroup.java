package com.rhbgroup.dcpbo.user.common.model.bo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@IdClass(UserUsergroupIdClass.class)
@Table(name = "TBL_BO_USER_USERGROUP")
public class UserUsergroup implements Serializable {

	@Id
	@Column(name = "USER_ID", nullable = false)
	private Integer userId;

	@Id
	@Column(name = "USER_GROUP_ID", nullable = false)
	private Integer userGroupId;

	@Column(name = "STATUS", nullable = false)
	private String status;

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
