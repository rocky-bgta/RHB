package com.rhbgroup.dcpbo.customer.dto;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BillerResponse implements BoData{

	private TotalBillersResponse biller;

}
