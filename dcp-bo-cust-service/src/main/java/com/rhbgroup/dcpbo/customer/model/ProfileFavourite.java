package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Blob;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_FAVOURITE_PROFILE")
public class ProfileFavourite implements Serializable {

	private static final long serialVersionUID = -1065578354038075233L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false, unique = true)
	private Integer id;

	@Column(name = "USER_ID", nullable = false)
	private Integer userId;

	@Column(name = "TXN_TYPE", nullable = false)
	private String txnType;

	@Column(name = "MAIN_FUNCTION", nullable = false)
	private String mainFunction;

	@Column(name = "SUB_FUNCTION")
	private String subFunction;

	@Column(name = "PAYEE_ID")
	private Integer payeeId;

	@Column(name = "TO_ACCOUNT_NO")
	private String toAccountNo;

	@Column(name = "NICKNAME", nullable = false)
	private String nickname;

	@Column(name = "IMAGE")
	private Blob image;

	@Column(name = "EMAIL")
	private String email;

	@Column(name = "MOBILE_NO")
	private String mobileNo;

	@Column(name = "amount", nullable = false, columnDefinition = "DECIMAL(17,2)")
	private BigDecimal amount;

	@Column(name = "REF_1")
	private String ref1;

	@Column(name = "REF_2")
	private String ref2;

	@Column(name = "REF_3")
	private String ref3;

	@Column(name = "REF_4")
	private String ref4;

	@Column(name = "IS_QUICK_LINK", nullable = false)
	private Boolean isQuickLink = Boolean.FALSE;

	@Column(name = "QUICK_LINK_ORDER")
	private Integer quickLinkOrder;

	@Column(name = "IS_QUICK_PAY", nullable = false)
	private Boolean isQuickPay = Boolean.FALSE;

	@Column(name = "IS_FIRST_TRANSACTION", nullable = false)
	private Boolean isFirstTrx = Boolean.FALSE;

	@Column(name = "TO_ID_TYPE")
	private String toIdType;

	@Column(name = "TO_ID_NO")
	private String toIdNo;

	@Column(name = "TO_RESIDENT_STATUS")
	private Boolean toResidentStatus = Boolean.FALSE;

	@Column(name = "DUITNOW_COUNTRY_CODE")
	private String duitnowCountryCode;

}
