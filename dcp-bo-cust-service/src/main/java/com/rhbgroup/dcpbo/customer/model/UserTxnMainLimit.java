package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_USER_TXN_LIMIT")
public class UserTxnMainLimit implements Serializable {

	private static final long serialVersionUID = -2121236600186466468L;

	@Id
    @Column(name = "id", nullable = false, unique = true )
    private Integer id;

	@Column(name = "USER_ID", nullable = false)
	private Integer userId;

	@Column(name = "TXN_TYPE", nullable = false)
	private String txnType;
	
	@Column(name = "IS_ADVANCE_ENABLED", nullable = false)
	private Boolean isAdvanceEnabled = Boolean.FALSE;
	
	@Column(name = "AMOUNT", nullable = false, columnDefinition = "DECIMAL(17,2)")
	private BigDecimal amount;
}
