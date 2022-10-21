package com.rhbgroup.dcpbo.system.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoExceptionResponse {
    private String errorCode;
    private String errorDesc;

    public BoExceptionResponse(){}

    public BoExceptionResponse(String errorCode){
        this.errorCode = errorCode;
    }

    public BoExceptionResponse(String errorCode, String errorDesc){
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }
}
