package com.rhbgroup.dcpbo.customer.dcpbo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Setter
@Getter
@Entity
@Table(name = "TBL_BO_CONFIG_MODULE")
public class ConfigModule implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID", nullable = false)
	private Integer id;

	@Column(name = "MODULE_NAME", nullable = false)
	private String moduleName;

}
