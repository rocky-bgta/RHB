package com.rhbgroup.dcpbo.customer.model;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * A user.
 */
@lombok.Setter
@lombok.Getter
@Entity
@Table(name = "TBL_transfer_txn")
public class TransferTxn implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false,unique = true )
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "txn_id", nullable = false)
    private String txnId;

    @Column(name = "ref_id", nullable = false)
    private String refId;

    @Column(name = "multi_factor_auth", nullable = false)
    private String multiFactorAuth;

    @Column(name = "main_function", nullable = false)
    private String mainFunction;

    @Column(name = "sub_function")
    private String subFunction;

    @Column(name = "from_account_no", nullable = false)
    private String fromAccountNo;

    @Column(name = "from_account_name")
    private String fromAccountName;

    @Column(name = "to_account_no", nullable = false)
    private String toAccountNo;

    @Column(name = "to_account_name")
    private String toAccountName;

    @Column(name = "to_bank_id")
    private Integer toBankId;

    @Column(name = "to_id_type")
    private String toIdType;

    @Column(name = "to_id_no")
    private String toIdNo;

    @Column(name = "to_resident_status")
    private Boolean toResidentStatus;

    @Column(name = "amount", nullable = false, columnDefinition = "DECIMAL(17,2)")
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "recipient_ref")
    private String recipientRef;

    @Column(name = "other_payment_details")
    private String otherPaymentDetails;

    @Column(name = "service_charge", nullable = false, columnDefinition = "DECIMAL(15,2)")
    private BigDecimal serviceCharge;

    @Column(name = "txn_status", nullable = false)
    private String txnStatus;

    @Column(name = "txn_status_code", nullable = false)
    private String txnStatusCode;

    @Column(name = "txn_time", nullable = false)
    private Date txnTime;

    @Column(name = "gst_rate", nullable = false, columnDefinition = "DECIMAL(6,4)")
    private BigDecimal gstRate;

    @Column(name = "gst_amount", nullable = false, columnDefinition = "DECIMAL(15,2)")
    private BigDecimal gstAmount;

    @Column(name = "gst_treatment_type", nullable = false)
    private String gstTreatmentType;

    @Column(name = "gst_calculation_method", nullable = false)
    private String gstCalculationMethod;

    @Column(name = "gst_tax_code", nullable = false)
    private String gstTaxCode;

    @Column(name = "gst_txn_id", nullable = false)
    private String gstTxnId;

    @Column(name = "gst_ref_no")
    private String gstRefNo;

    @Column(name = "is_quick_pay", nullable = false)
    private Boolean isQuickPay;

    @Column(name = "duitnow_country_code")
    private String duitnowCountryCode;

    @Column(name = "from_ip_address")
    private String fromIpAddress;

    @Column(name = "duitnow_to_registration_id")
    private String duitnowToRegistrationId;

    @Column(name = "duitnow_to_bic")
    private String duitnowToBic;

    @Column(name = "updated_time")
    private Date updatedTime;

}
