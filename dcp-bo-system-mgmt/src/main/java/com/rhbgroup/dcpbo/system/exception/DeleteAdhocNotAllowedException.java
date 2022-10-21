package com.rhbgroup.dcpbo.system.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class DeleteAdhocNotAllowedException extends BoException{

	private static final long serialVersionUID = -9018773367687987043L;

	public DeleteAdhocNotAllowedException() {
		errorCode = "41001";
		errorDesc = "Downtime Activated and cannot be deleted.";
		httpStatus = HttpStatus.FORBIDDEN;
	}

}
