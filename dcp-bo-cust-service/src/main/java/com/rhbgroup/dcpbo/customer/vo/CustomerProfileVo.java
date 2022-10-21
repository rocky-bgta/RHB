package com.rhbgroup.dcpbo.customer.vo;

import java.util.List;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.CustomerProfile;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerProfileVo implements BoData{
	
	List<CustomerProfile> actions;
}
