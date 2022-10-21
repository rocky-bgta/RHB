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
public class EmailDto implements BoData{
	
	private String fund;
	private String tran;
	private String totalAmount;
	private String asnbTran;
	private String asnbTotalAmount;

}
