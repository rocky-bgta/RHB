package com.rhbgroup.dcpbo.customer.model;

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
@Table(name = "TBL_DEPOSIT_PROFILE")
public class CasaProfile {
	@Id
	private int id;
	
	@Column(name = "account_no")
	private String accountNo;
}
