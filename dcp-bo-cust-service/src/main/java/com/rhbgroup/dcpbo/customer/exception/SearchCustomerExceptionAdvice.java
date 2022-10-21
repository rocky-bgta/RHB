package com.rhbgroup.dcpbo.customer.exception;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.controller.CustController;
import com.rhbgroup.dcpbo.customer.dto.ResponseError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(assignableTypes = {CustController.class})
public class SearchCustomerExceptionAdvice {

    @ExceptionHandler(SearchCustomerException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BoData customerSearchexceptionResponse(SearchCustomerException ex){
        ResponseError responseError = new ResponseError();
        responseError.setErrorCode(ex.getErrorCode());
        responseError.setErrorDesc(ex.getErrorDesc());
        responseError.setHttpStatus(ex.getHttpStatus());
        return responseError;
    }
}
