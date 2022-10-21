package com.rhbgroup.dcpbo.customer.dto;

import java.util.List;

import com.rhbgroup.dcp.deposits.casa.model.DcpDepositTransactionPagination;
import com.rhbgroup.dcp.eai.adaptors.uber.reaipn05.AsnbTransaction.model.DcpAsnbTransactionDetail;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class AsnbTransactions implements BoData {
	
	
	private String unitHoldings;
	private String totalUnits;
	
	List<DcpAsnbTransactionDetail> transactionDetail;
}