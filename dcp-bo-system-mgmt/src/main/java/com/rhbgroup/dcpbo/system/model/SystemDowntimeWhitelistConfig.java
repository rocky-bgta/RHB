package com.rhbgroup.dcpbo.system.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TBL_SYSTEM_DOWNTIME_WHITELIST.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "TBL_SYSTEM_DOWNTIME_WHITELIST")
public class SystemDowntimeWhitelistConfig implements Serializable {
	
	private static final long serialVersionUID = 6891908065661402237L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Integer id;

        @Column(name = "user_id", nullable = false)
	private Integer userId;

	@Column(name = "type", nullable = true)
	private String type;

	@Column(name = "created_time", nullable = false)
	private Timestamp createdTime;

	@Column(name = "created_by", nullable = false)
	private String createdBy;
	
	@Column(name = "updated_time", nullable = false)
	private Timestamp updatedTime;

	@Column(name = "updated_by", nullable = false)
	private String updatedBy;
	
}