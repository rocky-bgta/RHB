package com.rhbgroup.dcpbo.customer.dto;

import java.util.List;

import com.rhbgroup.dcp.deposits.casa.model.DcpDepositTransactionPagination;
import com.rhbgroup.dcp.eai.adaptors.transactionhistory.model.DcpTransaction;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class CasaTransactions implements BoData {
	DcpDepositTransactionPagination pagination;
	List<DcpTransaction> transactionHistory;
}
