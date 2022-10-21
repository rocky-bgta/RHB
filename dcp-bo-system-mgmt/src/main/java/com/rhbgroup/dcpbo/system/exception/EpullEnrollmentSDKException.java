package com.rhbgroup.dcpbo.system.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EpullEnrollmentSDKException extends BoException {

	private static final long serialVersionUID = -4557520736122008991L;

	public EpullEnrollmentSDKException() {
		this.errorCode = "40010";
		this.errorDesc = "Error executing epull auto enrollment logic.";
		this.httpStatus = HttpStatus.BAD_REQUEST;
	}
}