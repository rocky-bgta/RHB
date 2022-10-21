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
@Table(name = "TBL_topup_txn")
public class TopupTxn implements Serializable {

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

    @Column(name = "from_account_no")
    private String fromAccountNo;

    @Column(name = "from_card_no")
    private String fromCardNo;

    @Column(name = "to_biller_id", nullable = false)
    private Integer toBillerId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "ref_1", nullable = false)
    private String ref1;

    @Column(name = "amount", nullable = false, columnDefinition = "DECIMAL(17,2)")
    private BigDecimal amount;

    @Column(name = "total_service_charge", nullable = false, columnDefinition = "DECIMAL(15,2)")
    private BigDecimal totalServiceCharge;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

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

    @Column(name = "to_favourite_id")
    private Integer toFavouriteId;

    @Column(name = "is_quick_pay", nullable = false)
    private Boolean isQuickPay;

    @Column(name = "mobility_one_txn_id")
    private String mobilityOneTxnId;

    @Column(name = "updated_time")
    private Date updatedTime;

}
