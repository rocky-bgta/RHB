package com.rhbgroup.dcp.bo.batch.framework.exception;

public class BatchValidationException extends BatchException {
    public BatchValidationException(String dcpStatusCode) {
        super(dcpStatusCode);
    }
    public BatchValidationException(String dcpStatusCode, String dcpStatusDescription){
        super(dcpStatusCode,dcpStatusDescription);
    }

    public BatchValidationException(String dcpStatusCode, String dcpStatusDescription, Throwable ex){
        super(dcpStatusCode,dcpStatusDescription,ex);
    }
}
