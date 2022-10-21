package com.rhbgroup.dcpbo.customer.model;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class McaTermStatus implements BoData {
	private String code;
	private String statusType;
	private String title;
	private String description;
}
