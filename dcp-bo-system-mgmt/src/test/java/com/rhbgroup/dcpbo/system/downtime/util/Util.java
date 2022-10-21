package com.rhbgroup.dcpbo.system.downtime.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.rhbgroup.dcpbo.common.exception.CommonException;

public class Util {

	public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

	public static String loadJsonResourceFile(Class<?> klass, String filename) throws IOException {
		System.out.println("loadJsonResourceFile()");
		System.out.println("    klass: " + klass);
		System.out.println("    filename: " + filename);
		
        InputStream is = klass.getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null)
                sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        System.out.println("    jsonStr: " + jsonStr);
		
        return jsonStr;
	}

	public static Timestamp toTimestamp(String value) {
		Timestamp timestamp = null;

		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
			timestamp = new Timestamp(simpleDateFormat.parse(value).getTime());
		} catch (ParseException e) {
			System.out.println(e);
			System.out.println("Failed to parse Timestamp for value: " + value);
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Failed to parse Timestamp for value: " + value);
		}
		
		return timestamp;
	}
}
