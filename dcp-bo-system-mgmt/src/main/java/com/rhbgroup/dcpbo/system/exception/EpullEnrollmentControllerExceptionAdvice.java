package com.rhbgroup.dcpbo.system.exception;

import com.rhbgroup.dcpbo.system.model.response.BoExceptionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class EpullEnrollmentControllerExceptionAdvice {

	@ExceptionHandler({EpullEnrollmentMissingException.class, EpullEnrollmentSDKException.class})
	@ResponseBody
	public ResponseEntity<Object> handleEpullBadRequestException(BoException ex, WebRequest request) {
		BoExceptionResponse boExceptionResponse = new BoExceptionResponse();
		boExceptionResponse.setErrorCode(ex.getErrorCode());
		boExceptionResponse.setErrorDesc(ex.getErrorDesc());

		return new ResponseEntity<>(boExceptionResponse, ex.getHttpStatus());
	}
}
