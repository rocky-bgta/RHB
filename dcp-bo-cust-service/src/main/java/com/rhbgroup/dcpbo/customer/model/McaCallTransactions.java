package com.rhbgroup.dcpbo.customer.model;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class McaCallTransactions implements BoData {
	private String firstField;
	private String secondField;
}
