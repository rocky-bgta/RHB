package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_USER_TXN_ADVANCE_LIMIT")
public class UserTxnMainFuncLimit implements Serializable {

	private static final long serialVersionUID = -1884860253970705684L;

	@Id
    @Column(name = "id", nullable = false, unique = true )
    private Integer id;

	@Column(name = "USER_ID", nullable = false)
	private Integer userId;

	@Column(name = "TXN_TYPE")
	private String txnType;

	@Column(name = "MAIN_FUNCTION")
	private String mainFunction;

	@Column(name = "AMOUNT", nullable = false, columnDefinition = "DECIMAL(17,2)")
	private BigDecimal amount;
	
	@Column(name = "PRE_LOGIN_ALLOW_FLAG")
	private boolean preLogin;
	
	@Column(name = "FULL_LOGIN_ALLOW_FLAG")
	private boolean fullLogin;
	
}
