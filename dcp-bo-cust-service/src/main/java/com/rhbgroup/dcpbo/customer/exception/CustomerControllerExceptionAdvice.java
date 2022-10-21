package com.rhbgroup.dcpbo.customer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.controller.CustomerController;
import com.rhbgroup.dcpbo.customer.vo.ResponseError;

@ControllerAdvice(assignableTypes = { CustomerController.class })
public class CustomerControllerExceptionAdvice {

	@ExceptionHandler(CustomerControllerException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public BoData customerSearchexceptionResponse(CustomerControllerException ex) {
		ResponseError responseError = new ResponseError();
		responseError.setErrorCode(ex.getErrorCode());
		responseError.setErrorDesc(ex.getErrorDesc());
		return responseError;
	}

}
