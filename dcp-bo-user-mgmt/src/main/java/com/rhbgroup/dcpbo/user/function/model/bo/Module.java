package com.rhbgroup.dcpbo.user.function.model.bo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@lombok.Setter
@lombok.Getter
@Entity
@Table(name = "TBL_BO_CONFIG_MODULE")
public class Module implements Serializable {

	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "MODULE_NAME", nullable = false)
	private String moduleName;

	@Column(name = "CREATED_TIME", nullable = false)
	private Timestamp createdTime;

	@Column(name = "CREATED_BY", nullable = false)
	private String createdBy;

	@Column(name = "UPDATED_TIME", nullable = false)
	private Timestamp updatedTime;

	@Column(name = "UPDATED_BY", nullable = false)
	private String updatedBy;
}
