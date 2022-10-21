package com.rhbgroup.dcpbo.user.common.model.bo;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_BO_CONFIG_DEPARTMENT")
public class BoConfigDepartment implements Serializable {

	private static final long serialVersionUID = 9048521968893221050L;

	@Id
	@Column(name = "id", nullable = false, unique = true)
	private Integer id;
	
	@Column(name = "DEPARTMENT_NAME", nullable = false)
	private String departmentName;
	
	@Column(name = "DEPARTMENT_CODE", nullable = false)
	private String departmentCode;
	
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
