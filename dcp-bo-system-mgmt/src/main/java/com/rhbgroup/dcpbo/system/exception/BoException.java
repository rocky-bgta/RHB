package com.rhbgroup.dcpbo.system.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BoException extends RuntimeException {
	private static final long serialVersionUID = -8614876429598724832L;

	protected String errorCode;
    protected String errorDesc;
    protected HttpStatus httpStatus;

    public Map toMap() {
        Map<String, String> map = new HashMap<>();
        map.put(errorCode, errorDesc);
        return map;
    }
}
