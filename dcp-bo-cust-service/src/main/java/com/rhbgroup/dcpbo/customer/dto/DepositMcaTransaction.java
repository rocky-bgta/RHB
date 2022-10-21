package com.rhbgroup.dcpbo.customer.dto;

import java.util.List;

import com.rhbgroup.dcp.deposits.mca.model.DcpMcaTransactionHistoryResponse;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DepositMcaTransaction implements BoData {
	DepositMcaTransactionPagination pagination;
	List<DcpMcaTransactionHistoryResponse> transactionHistory;
}
