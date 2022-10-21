package com.rhbgroup.dcpbo.customer.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcp.creditcards.model.DcpSuppCard;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreditCardDetails implements BoData {
	private String cardHolderName;
    private BigDecimal outstandingBalance;
    private BigDecimal statementBalance;
    private BigDecimal availableLimit;
    private BigDecimal availableCredit;
    private BigDecimal creditLimit;
    private String paymentDueDate;
    private BigDecimal minPaymentDue;
    private String embossedName;
    private BigDecimal rewardPointBalance;
    private List<DcpSuppCard> suppCards;
    private Boolean isRewardPoint;
}
