package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "DCP_AUDIT_DETAIL_CONFIG")
public class AuditDetailConfig implements Serializable {

	private static final long serialVersionUID = -8236690051880814239L;

	@Id
	@Column(name = "ID")
	private int id;

	@Column(name = "EVENT_CODE")
	private String eventCode;

	@Column(name = "FIELD_NAME")
	private String fieldName;

	@Column(name = "PATH")
	private String path;

	@Column(name = "TYPE")
	private String type;

}
