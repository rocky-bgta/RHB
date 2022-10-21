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
@Table(name = "TBL_USER_TXN_PRE_LOGIN_LIMIT")
public class UserPerTxnDailyLimit implements Serializable {
	
	private static final long serialVersionUID = -2121236600186466468L;
	
	@Id
    @Column(name = "id", nullable = false, unique = true )
    private Integer id;

	@Column(name = "USER_ID", nullable = false)
	private Integer userId;

	@Column(name = "TXN_TYPE", nullable = false)
	private String txnType;
	
	@Column(name = "MAIN_FUNCTION", nullable = false)
	private String mainFunction; 
	
	@Column(name = "AMOUNT_PER_TXN", nullable = false, columnDefinition = "DECIMAL(17,2)")
	private BigDecimal amountPerTxn;
	
	@Column(name = "AMOUNT_PER_DAY", nullable = false, columnDefinition = "DECIMAL(17,2)")
	private BigDecimal amountPerDay;

}
