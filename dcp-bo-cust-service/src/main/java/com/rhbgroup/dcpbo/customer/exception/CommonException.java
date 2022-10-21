package com.rhbgroup.dcpbo.customer.exception;

import org.springframework.http.HttpStatus;

import com.rhbgroup.dcpbo.customer.audit.collector.BoException;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonException extends BoException {
	
	private static final long serialVersionUID = -8985346268381947379L;

	public static final String GENERIC_ERROR_CODE = "80000";
	public static final String TRANSACTION_NOT_FOUND = "50002";
	public static final String CUSTOMER_NOT_FOUND = "50001";

	public CommonException() {
		
	}

	public CommonException(String errorCode, String message) {
		this.errorCode = errorCode;
		this.errorDesc = message;
	}

	public CommonException(String errorCode, String errorDesc, HttpStatus httpStatus) {
		this.errorCode = errorCode;
		this.errorDesc = errorDesc;
		this.httpStatus = httpStatus;
	}

	public CommonException(String errorCode) {

		this.errorCode = errorCode;
	}
	

}
