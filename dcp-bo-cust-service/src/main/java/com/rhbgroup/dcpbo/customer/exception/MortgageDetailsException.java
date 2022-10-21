package com.rhbgroup.dcpbo.customer.exception;

import com.rhbgroup.dcpbo.customer.audit.collector.BoException;
import lombok.Getter;

@SuppressWarnings("serial")
@Getter
public class MortgageDetailsException extends BoException {

    public MortgageDetailsException(){
        errorCode = CommonException.GENERIC_ERROR_CODE;
        errorDesc = "No matching customer account";
    }

}
