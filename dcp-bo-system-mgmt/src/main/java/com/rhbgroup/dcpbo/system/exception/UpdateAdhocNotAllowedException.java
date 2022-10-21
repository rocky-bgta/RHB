package com.rhbgroup.dcpbo.system.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class UpdateAdhocNotAllowedException extends BoException{

	private static final long serialVersionUID = -7018773123287987043L;

	public UpdateAdhocNotAllowedException() {
		errorCode = "41000";
		errorDesc = "Downtime Activated and cannot be updated.";
		httpStatus = HttpStatus.FORBIDDEN;
	}

}



