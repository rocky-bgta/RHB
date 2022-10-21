package com.rhbgroup.dcpbo.customer.exception;


import com.rhbgroup.dcpbo.customer.audit.collector.BoException;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import com.rhbgroup.dcpbo.customer.audit.collector.BoException;
import com.rhbgroup.dcpbo.customer.client.ConfigErrorInterface;
import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;

@ControllerAdvice
public class CommonExceptionAdvice {
	@Autowired
	ConfigErrorInterface configErrorInterface;

	private final Logger log = LogManager.getLogger(CommonExceptionAdvice.class);


	@ExceptionHandler(value = BoException.class)
	@ResponseBody
	protected ResponseEntity<Object> handleNotFound(BoException ex, WebRequest request) {


		BoExceptionResponse response = new BoExceptionResponse();

		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

		try {
			String errorCode = ex.getErrorCode();
			response = configErrorInterface.getConfigError(errorCode);
			if (ex.getHttpStatus() != null) {
				httpStatus = ex.getHttpStatus();
			}

			log.debug("response desc:" + response.getErrorDesc());

		} catch (Exception e) {
			response.setErrorCode(CommonException.GENERIC_ERROR_CODE);
			response.setErrorDesc("Internal Server Error");
			log.error(e);
		}

		return new ResponseEntity<>(response, httpStatus);
	}

	@ExceptionHandler(value = RuntimeException.class)
	@ResponseBody
	protected ResponseEntity<Object> handleUnspecifiedError(RuntimeException ex, WebRequest request) {


		BoExceptionResponse response = new BoExceptionResponse();

		HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

		try {
			response = configErrorInterface.getConfigError(CommonException.GENERIC_ERROR_CODE);
			log.error("Caught unhandled Runtime Exception : ", ex);

		} catch (Exception e) {
			response.setErrorCode(CommonException.GENERIC_ERROR_CODE);
			response.setErrorDesc("Internal Server Error");
			log.error(e);
		}

		return new ResponseEntity<>(response, httpStatus);
	}
}
