package com.rhbgroup.dcp.bo.batch.framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public final class JsonUtils {
    @Autowired
    static ObjectMapper mapper = new ObjectMapper();

    private JsonUtils() {
    	throw new IllegalStateException("Utility Class");
    }
    
    public static String convertObjectToString(Object obj)  {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            return "Unable to convert object to json"+ ex.getMessage()+ ex.getStackTrace() ;
        }
    }
    
    public static <T> T jsonToObject(String json, Class<T> objectClass) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(json, objectClass);
        } catch (IOException e) {
            return null;
        }
    }
}
