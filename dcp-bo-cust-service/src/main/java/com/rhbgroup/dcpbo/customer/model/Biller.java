package com.rhbgroup.dcpbo.customer.model;

import java.io.Serializable;
import java.math.BigDecimal;
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
@Table(name = "TBL_BILLER")
public class Biller implements Serializable {

	private static final long serialVersionUID = -8527064382625753289L;

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

	@ManyToOne(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinColumn(name = "CATEGORY_ID", nullable = false)
	private BillerCategory billerCatogery;

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

	@Column(name = "MIN_AMOUNT", nullable = false, columnDefinition="decimal", precision=17, scale=2)
	private BigDecimal minAmount;

	@Column(name = "MAX_AMOUNT", nullable = false, columnDefinition="decimal", precision=17, scale=2)
	private BigDecimal maxAmount;

	@Column(name = "STATUS", nullable = false)
	private String status;

	@Column(name = "REF_VALIDATION", nullable = false)
	private String redValidation;

	@Column(name = "CONFIG_CHECKSUM", nullable = false)
	private String configChecksum;

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
