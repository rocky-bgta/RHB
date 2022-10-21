package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Entity
@Table(name = "VW_UT_ACCOUNT")
public class UnitTrustAccount implements Serializable {
	private static final long serialVersionUID = -8681717606593600055L;

	@Id
	private int id;
	
	@Column(name = "account_no")
	private String accountNo;
	
	@Column(name = "account_status_description")
	private String accountStatus;
	
	@Column(name = "signatory_description")
	private String signatoryDescription;
	
	@Column(name = "batch_extraction_time")
	private Timestamp batchExtractionTime;
}
