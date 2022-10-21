package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class AsnbHelperDTO{
	
	List<AsnbVarianceDetails> ansbVarDetails;//variance
	Map<String, AsnbSuccessList> asnbMap;//summary Details
	private boolean checkForPnbRecords;
	List<String> pnbFundList;
	private String channelTypeWithOutFile;

}
