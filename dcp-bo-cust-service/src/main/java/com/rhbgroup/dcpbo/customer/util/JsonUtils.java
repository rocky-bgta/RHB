package com.rhbgroup.dcpbo.customer.util;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

public class JsonUtils {
	
	public static String getJsonPathValueWithDefaultNull(String jsonString, String path) {
		if(isValid(jsonString)) {
			Configuration config = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
			if(JsonPath.using(config).parse(jsonString).read(path, String.class) != null) {
				String value = JsonPath.using(config).parse(jsonString).read(path, String.class);
				return value;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public static boolean isValid(String jsonString) {
		try {
			new JSONObject(jsonString);
		} catch(Exception ex) {
			try {
				new JSONArray(jsonString);
			} catch(Exception exex) {
				return false;
			}
		}
		
		return true;
	}

}
