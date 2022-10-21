package com.rhbgroup.dcp.bo.batch.job.model;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsnbSuccessList implements BoData {
	
	private String name;
	private List<String> bnkTxnRefNum;
	private double bankAmount;
	private double bankNoOfTran;
	private double asnbAmount;
	private double asnbNoOfTran;
	private double varAmount;
	private double varNoOfTran;
	private double totalRedBankAmount;
	private double totalRedBankNoOfTnx;
	
	private double totalRedAsnbAmount;
	private double totalRedAsnbNoOfTnx;
	
	private double totalRedVarAmount;
	private double totalRedVarNoOfTnx;
	
	
	//total subscription
	private double alltotalSubBankAmount;
	private double alltotalSubBankNoOfTnx;
	
	private double alltotalSubAsnbAmount;
	private double alltotalSubAsnbNoOfTnx;
	
	private double alltotalSubVarAmount;
	private double alltotalSubVarNoOfTnx;
	
}

