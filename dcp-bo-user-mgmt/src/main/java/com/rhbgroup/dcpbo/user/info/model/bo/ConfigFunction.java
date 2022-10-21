package com.rhbgroup.dcpbo.user.info.model.bo;
import com.rhbgroup.dcpbo.user.function.model.bo.Module;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * A user.
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_BO_CONFIG_FUNCTION")
public class ConfigFunction implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Column(name = "function_name", nullable = false, unique = true)
	private String functionName;

	@Column(name = "checker_scope", nullable = false)
	private String checkerScope;

	@Column(name = "maker_scope", nullable = false)
	private String makerScope;

	@Column(name = "inquirer_scope", nullable = false)
	private String inquirerScope;

	@Column(name = "approval_required", nullable = false)
	private boolean approvalRequired;

	@Column(name = "created_time", nullable = false)
	private Timestamp created_time;

	@Column(name = "created_by", nullable = false)
	private String created_by;

	@Column(name = "updated_time", nullable = false)
	private Timestamp updated_time;

	@Column(name = "updated_by", nullable = false)
	private String updated_by;

	@ManyToOne
	@JoinColumn(name = "module_id", nullable = false)
	private Module module;
}