package com.rhbgroup.dcp.bo.batch.framework.exception;

import lombok.Getter;

public class BatchException extends Exception {
    @Getter
    private final String dcpStatusCode;
    @Getter
    private final String dcpStatusDescription;

    public BatchException(String dcpStatusCode){
        this.dcpStatusCode = dcpStatusCode;
        this.dcpStatusDescription = null;
    }

    public BatchException(String dcpStatusCode, String dcpStatusDescription){
        this.dcpStatusCode = dcpStatusCode;
        this.dcpStatusDescription = dcpStatusDescription;
    }

    public BatchException(String dcpStatusCode, String dcpStatusDescription, Throwable ex){
        super(ex);
        this.dcpStatusCode = dcpStatusCode;
        this.dcpStatusDescription=dcpStatusDescription;
    }

    @Override
    public String getMessage(){
    	return dcpStatusCode+":"+dcpStatusDescription;
    }
}

