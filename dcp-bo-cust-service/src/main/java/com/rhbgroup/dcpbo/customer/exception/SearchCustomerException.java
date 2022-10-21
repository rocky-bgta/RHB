package com.rhbgroup.dcpbo.customer.exception;

import com.rhbgroup.dcpbo.customer.audit.collector.BoException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SearchCustomerException extends BoException {

    public SearchCustomerException(){
        errorCode = "50001";
        errorDesc = "No matching customer";
        httpStatus = HttpStatus.NOT_FOUND;
    }

}
