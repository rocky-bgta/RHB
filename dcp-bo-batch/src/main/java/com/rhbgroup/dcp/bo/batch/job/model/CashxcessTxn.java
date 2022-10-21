package com.rhbgroup.dcp.bo.batch.job.model;

import java.math.BigDecimal;
import java.sql.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CashxcessTxn{
	
    private Integer id;
    private Integer txnTokenId;
    private String channelCode;
    private String subChannel;
    private String deliveryChannel;
    private String functionCode;
    private String cardNumber;
    private String beneficiaryName;
    private String requestDate;
    private String requestTime;
    private String planNumber;
    private String tenure;
    private BigDecimal totalAmount;
    private String trxDescription;
    private String merchantId;
    private String cardExpiryDate;
    private String entryStaffId;
    private String regionCode;
    private String branchCode;
    private String referralId;
    private String salesStaffId;
    private String obd1DisbursementMethod;
    private String obd1BankId;
    private String obd1CasaAccount;
    private BigDecimal obd1Amount;
    private String obd1Ctrl3;
    private String obd2DisbursementMethod;
    private String obd2BankId;
    private String obd2CasaAccount;
    private BigDecimal obd2Amount;
    private String obd2Ctrl3;
    private String obd3DisbursementMethod;
    private String obd3BankId;
    private String obd3CasaAccount;
    private BigDecimal obd3Amount;
    private String obd3Ctrl3;
    private BigDecimal effectiveRate;
    private BigDecimal interestRate;
    private BigDecimal monthlyInstallmentAmount;
    private String toBankName;
}