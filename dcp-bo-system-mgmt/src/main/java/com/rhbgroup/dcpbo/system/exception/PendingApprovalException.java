package com.rhbgroup.dcpbo.system.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class PendingApprovalException extends BoException{

	private static final long serialVersionUID = 3931813958741379134L;

	public PendingApprovalException() {
		errorCode = "40002";
		errorDesc = "A similar request is already pending approval.";
		httpStatus = HttpStatus.FORBIDDEN;
	}

}
