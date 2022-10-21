package com.rhbgroup.dcp.bo.batch.job.model;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class DcpPendingDto {
	
	private String fundName;
	private String amount;
	private String UhBenificiaryId;
	private String UhBenificiaryIcNo;
	private String date;
	private String time;
	private String bnkRefNo;
	private String fdsRefNo;

}
