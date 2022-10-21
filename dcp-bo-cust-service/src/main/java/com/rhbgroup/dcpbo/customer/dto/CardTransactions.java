package com.rhbgroup.dcpbo.customer.dto;

import java.util.List;

import com.rhbgroup.dcp.creditcards.model.CreditCardTransactionHistory;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CardTransactions implements BoData {
	CardPagination pagination;
	List<CreditCardTransactionHistory> transactionHistory;
}
