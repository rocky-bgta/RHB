package com.rhbgroup.dcp.bo.batch.email;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcp.bo.batch.job.model.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VarianceDto implements BoData{
	
	private String beneficiaryAhnbId;
	private String fund;
	private String date;
	private String time;
	private String bankRefNo;
	private String asnbRefNo;
	private String amount;

}
