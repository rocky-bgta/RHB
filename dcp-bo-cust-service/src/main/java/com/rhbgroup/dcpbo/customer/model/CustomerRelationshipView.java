package com.rhbgroup.dcpbo.customer.model;

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
import lombok.ToString;

@Setter
@Getter
@ToString
@Entity
@Table(name = "VW_UT_FUND_MASTER")
public class CustomerRelationshipView implements Serializable {

	private static final long serialVersionUID = -3207973015910610772L;

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "CIS_NO")
	private String cisNo;

	@Column(name = "ACCOUNT_NO")
	private String accountNo;

	@Column(name = "JOIN_TYPE")
	private String joinType;

	@Column(name = "BATCH_EXTRACTION_TIME")
	private Date batchExtractionTime;

	@Column(name = "CREATED_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "CREATED_BY")
	private String createdBy;

	@Column(name = "UPDATED_TIME")
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

	@Column(name = "UPDATED_BY")
	private String updatedBy;

}
