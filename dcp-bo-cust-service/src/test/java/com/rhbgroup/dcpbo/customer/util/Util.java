package com.rhbgroup.dcpbo.customer.util;

import com.rhbgroup.dcpbo.customer.exception.CommonException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Util {

    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    public static String loadJsonResourceFile(Class<?> klass, String filename) throws IOException {
        InputStream is = klass.getClassLoader().getResourceAsStream(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sbld = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sbld.append(line);
        br.close();
        is.close();

        String jsonStr = sbld.toString();
        return jsonStr;
    }

    public static Timestamp toTimestamp(String value) {
        Timestamp timestamp;

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
            timestamp = new Timestamp(simpleDateFormat.parse(value).getTime());
        } catch (ParseException e) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Failed to parse Timestamp for value: " + value);
        }

        return timestamp;
    }
}
