package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.util.Date;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_TOPUP_BILLER")
public class TopupBiller implements Serializable {

	private static final long serialVersionUID = -3403662780669588621L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false, unique = true)
	private Integer id;

	@Column(name = "BILLER_CODE", nullable = false)
	private String billerCode;

	@Column(name = "BILLER_NAME", nullable = false)
	private String billerName;

	@Column(name = "BILLER_COLLECTION_ACCOUNT_NO", nullable = false)
	private String billerCollectionAccNo;

	@Column(name = "CATEGORY_ID", nullable = false)
	private Integer categoryId;

	@Column(name = "ICON_URL", nullable = false)
	private String iconUrl;

	@Column(name = "IS_CASA_ALLOWED", nullable = false)
	private Boolean isCasaAllowed;

	@Column(name = "IS_CARD_ALLOWED", nullable = false)
	private Boolean isCardAllowed;

	@Column(name = "MERCHANT_ID")
	private String merchantId;

	@Column(name = "ISIS_BILLER", nullable = false)
	private Boolean isisBiller;

	@Column(name = "STATUS", nullable = false)
	private String status;

	@Column(name = "AMOUNT_SELECTION", nullable = false)
	private String amountSelection;

	@Column(name = "CREATED_TIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdTime;

	@Column(name = "CREATED_BY", nullable = false)
	private String createdBy;

	@Column(name = "UPDATED_TIME", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedTime;

	@Column(name = "UPDATED_BY", nullable = false)
	private String updatedBy;

}
