package com.rhbgroup.dcpbo.customer.vo;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseError implements BoData {

	private String errorCode;
	private String errorDesc;

}
