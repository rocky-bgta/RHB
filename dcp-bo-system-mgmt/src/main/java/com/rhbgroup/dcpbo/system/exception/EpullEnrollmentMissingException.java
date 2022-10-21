package com.rhbgroup.dcpbo.system.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EpullEnrollmentMissingException extends BoException {

	private static final long serialVersionUID = -4557520736122008991L;

	public EpullEnrollmentMissingException() {
		this.errorCode = "41002";
		this.errorDesc = "Missing User Id";
		this.httpStatus = HttpStatus.BAD_REQUEST;
	}

	public EpullEnrollmentMissingException(String errorDesc) {
		this.errorCode = "41003";
		this.errorDesc = errorDesc;
		this.httpStatus = HttpStatus.BAD_REQUEST;
	}

}