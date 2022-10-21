package com.rhbgroup.dcpbo.customer.exception;

import com.rhbgroup.dcpbo.customer.audit.collector.BoException;
import lombok.Getter;

@Getter
public class CustomerControllerException extends BoException {

	private static final long serialVersionUID = 3816446300639107727L;

	public CustomerControllerException(){
		errorCode = "50001";
		errorDesc = "No Customer Limit found";
	}

}
