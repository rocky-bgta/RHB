package com.rhbgroup.dcpbo.customer.dto;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetUserNameResponse implements BoData {

	private String statusTitle;
	private String statusDesc;
}
