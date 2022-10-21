package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class AsnbVarianceDetails {
	
	private String uhBenificiaryAsnbId;  
	private String uhBenificiaryIcNo;  
	private String fund;   
	private String date;      
	private String time;         
	private String bnkRefNum; 
	private String fdsRefNum;  
	private String amount;             
	private String varRemarks;

}
