package com.rhbgroup.dcpbo.system.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class AdhocDurationOverlappedException extends BoException {

	private static final long serialVersionUID = -6405355984938390587L;

	public AdhocDurationOverlappedException(){
		errorCode = "41000";
		errorDesc = "The duration you choose overlap with another downtime.";
		httpStatus = HttpStatus.FORBIDDEN;
		
	}

}
