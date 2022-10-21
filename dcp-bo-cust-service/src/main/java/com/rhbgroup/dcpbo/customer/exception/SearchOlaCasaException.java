package com.rhbgroup.dcpbo.customer.exception;

import com.rhbgroup.dcpbo.customer.audit.collector.BoException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SearchOlaCasaException extends BoException {

    public SearchOlaCasaException(){
        errorCode = "50005";
        errorDesc = "No record found";
        httpStatus = HttpStatus.NOT_FOUND;
    }

}