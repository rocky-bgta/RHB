package com.rhbgroup.dcpbo.system.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "TBL_INVEST_TXN")
public class InvestTxn implements Serializable {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true )
    private Integer id;
	
	@Column(name = "USER_ID") @NotNull
    private Integer userId;
	
	@Column(name = "TXN_ID") @Size(max = 36)
    private String txnId;
	
	@Column(name = "REF_ID") @Size(max = 17)
    private String refId;
	
	@Column(name = "MAIN_FUNCTION") @Size(max = 50) @NotNull
    private String mainFunction;
	
	@Column(name = "SUB_FUNCTION") @Size(max = 50)
    private String subFunction;
	
	@Column(name = "FROM_ACCOUNT_NO") @Size(max = 20)
    private String fromAccountNo;
	
	@Column(name = "FROM_ACCOUNT_NAME") @Size(max = 140)
    private String fromAccountName;
	
	@Column(name = "TO_ACCOUNT_NO") @Size(max = 35)
    private String toAccountNo;

	@Column(name = "TO_ACCOUNT_NAME") @Size(max = 140)
    private String toAccountName;
	
	@Column(name = "AMOUNT") @Digits(integer=17, fraction=2) @NotNull
    private BigDecimal amount;
	
	@Column(name = "RECIPIENT_REF") @Size(max = 140)
    private String recipientRef;
	
	@Column(name = "MULTI_FACTOR_AUTH") @Size(max = 20) @NotNull
    private String multiFactorAuth;
	
	@Column(name = "TXN_STATUS") @Size(max = 50) @NotNull  
    private String txnStatus;
	
	@Column(name = "TXN_TIME") @NotNull  
    private Timestamp txnTime;
	
	@Column(name = "SERVICE_CHARGE") @Digits(integer=15, fraction=2) @NotNull
    private BigDecimal serviceCharge;
	
	@Column(name = "GST_RATE") @Digits(integer=6, fraction=4) @NotNull
    private BigDecimal gstRate;
	
	@Column(name = "GST_AMOUNT") @Digits(integer=15, fraction=2) @NotNull
    private BigDecimal gstAmount;
	
	@Column(name = "GST_TREATMENT_TYPE") @Size(max = 2) @NotNull
    private String gstTreatmentType;
	
	@Column(name = "GST_CALCULATION_METHOD") @Size(max = 1) @NotNull
    private String gstCalculationMethod;
	
	@Column(name = "GST_TAX_CODE") @Size(max = 10) @NotNull
    private String gstTaxCode;
	
	@Column(name = "GST_TXN_ID", nullable = false)
    private Integer gstTxnId;
	
	@Column(name = "GST_REF_NO") @Size(max = 40)
    private String gstRefNo;
	
	@Column(name = "IS_QUICK_PAY") @NotNull
    private Boolean isQuickPay;
	
	@Column(name = "FROM_IP_ADDRESS") @Size(max = 15)
    private String fromIPAddress;
	
	@Column(name = "TXN_STATUS_CODE")@Size(max = 50)
    private String txnStatusCode;
	
	@Column(name = "FROM_ACCOUNT_CONNECTOR_CODE") @Size(max = 15)
    private String fromAccountConnectorCode;
	
	@Column(name = "TO_FAVOURITE_ID")
    private Integer toFavouriteId;
	
	@Column(name = "CHANNEL") @Size(max = 20) @NotNull  
    private String channel;
    
	@Column(name = "PAYMENT_METHOD")@Size(max = 30)
    private String paymentMethod;
	
	@Column(name = "ACCESS_METHOD") @Size(max = 30)
    private String accessMethod;
	
	@Column(name = "DEVICE_ID")  @Size(max = 40)
    private String deviceId;
	
	@Column(name = "SUB_CHANNEL")  @Size(max = 30)
    private String subChannel;
	
	@Column(name = "TXN_TOKEN_ID")@NotNull
    private Integer txnTokenId;
	
	@Column(name = "CURF_ID")@Size(max = 35)
    private String curfId;
	
	@Column(name = "REJECT_DESCRIPTION") @Size(max = 100)
    private String rejectDescription;
	
	@Column(name = "REJECT_CODE") @Size(max = 15)
    private String rejectCode;
	
	@Column(name = "IS_SETUP_FAVOURITE")  
    private Boolean isSetupFavourite;
    
    @Column(name = "IS_SETUP_QUICK_LINK")  
    private Boolean isSetupQuickLink;
    
    @Column(name = "IS_SETUP_QUICK_PAY")  
    private Boolean isSetupQuickPay;
	
    @Column(name = "TXN_CCY") @Size(max = 3)
    private String txnCcy;
    
    @Column(name = "CREATED_TIME") @NotNull
    private Timestamp createdTime;
    
    @Column(name = "CREATED_BY") @Size(max = 50) @NotNull
    private String createdBy;
    
    @Column(name = "UPDATED_TIME") @NotNull
    private Timestamp updatedTime;
    
    @Column(name = "UPDATED_BY")  @Size(max = 50) @NotNull
    private String updatedBy;
    
}