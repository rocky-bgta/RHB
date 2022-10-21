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
@Table(name = "TBL_LOAN_PROFILE")
public class MortgageProfile {
	@Id
	private int id;
	
	@Column(name = "account_no")
	private String accountNo;
}
