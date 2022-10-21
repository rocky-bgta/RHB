package com.rhbgroup.dcp.bo.batch.job.model;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

//dcp.[dbo].[TBL_TRANSFER_TXN]
public class IBGRejectStatusTblTransferTxn {
	private int userId;
	private String tellerId;
	private String traceId;	
}
