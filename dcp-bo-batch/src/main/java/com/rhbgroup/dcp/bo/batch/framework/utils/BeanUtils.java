package com.rhbgroup.dcp.bo.batch.framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class BeanUtils {

	private BeanUtils() {
		throw new IllegalStateException("Utility Class");
	}

	private static final ObjectMapper OBJECT_MAPPER_SINGLETON = new ObjectMapper();
	
	public static String toStringUsingJackson(final Object object) {
	    try {
	        return OBJECT_MAPPER_SINGLETON.writeValueAsString(object);
	    } catch (final JsonProcessingException e) {
	        return String.valueOf(object);
	    }
	}
}