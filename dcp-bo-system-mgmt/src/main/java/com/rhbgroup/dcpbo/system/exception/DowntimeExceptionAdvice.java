package com.rhbgroup.dcpbo.system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.rhbgroup.dcpbo.common.exception.CommonExceptionAdvice;
import com.rhbgroup.dcpbo.system.model.response.BoExceptionResponse;

@ControllerAdvice
public class DowntimeExceptionAdvice extends CommonExceptionAdvice {
	
	@ExceptionHandler(value = AdhocDurationOverlappedException.class)
	@ResponseBody
	public ResponseEntity<Object> handleAdhocDurationOverlappedException(AdhocDurationOverlappedException ex, 
			WebRequest request) {
		BoExceptionResponse boExceptionResponse = new BoExceptionResponse();
		boExceptionResponse.setErrorCode(ex.getErrorCode());
		boExceptionResponse.setErrorDesc(ex.getErrorDesc());
	
		return new ResponseEntity<>(boExceptionResponse, HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(value = PendingApprovalException.class)
	@ResponseBody
	public ResponseEntity<Object> handlePendingApprovalException(PendingApprovalException ex, 
			WebRequest request) {
		BoExceptionResponse boExceptionResponse = new BoExceptionResponse();
		boExceptionResponse.setErrorCode(ex.getErrorCode());
		boExceptionResponse.setErrorDesc(ex.getErrorDesc());
	
		return new ResponseEntity<>(boExceptionResponse, HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(value = DeleteAdhocNotAllowedException.class)
	@ResponseBody
	public ResponseEntity<Object> handleDeleteAdhocNotAllowedException(DeleteAdhocNotAllowedException ex, 
			WebRequest request) {
		BoExceptionResponse boExceptionResponse = new BoExceptionResponse();
		boExceptionResponse.setErrorCode(ex.getErrorCode());
		boExceptionResponse.setErrorDesc(ex.getErrorDesc());
	
		return new ResponseEntity<>(boExceptionResponse, HttpStatus.FORBIDDEN);
	}
}
