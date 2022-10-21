package com.rhbgroup.dcpbo.customer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsnbDetails implements BoData{
	
	
	private String  bankTxnRefNo;
	private String  membershipNum;//uhid
	private String  guardianNo;
	private String  guardianType;
	private String  idNumber;//memeberIdNumber
	private String idType;//memberIdType
	 



}
