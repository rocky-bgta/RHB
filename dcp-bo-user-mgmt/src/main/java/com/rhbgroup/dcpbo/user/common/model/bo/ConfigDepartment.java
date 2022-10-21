package com.rhbgroup.dcpbo.user.common.model.bo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Entity
@Table(name = "TBL_BO_CONFIG_DEPARTMENT")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ConfigDepartment implements Serializable {
	private static final long serialVersionUID = 5392427383263494507L;

	@Id
    @Column(name = "id", nullable = false)
	private int departmentId;

    @Column(name = "department_name", nullable = false)
	private String departmentName;
}
