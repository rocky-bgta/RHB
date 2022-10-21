package com.rhbgroup.dcpbo.customer.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

import com.rhbgroup.dcpbo.customer.exception.CommonException;

public class DateUtils {
	private static final String FORMAT_DATE = "yyyy-MM-dd'T'HH:mm:ssXXX";
	private static final String FORMAT_DATE_AUDIT_DATE_TIME = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final long MINUS_MONTHS = 6;
	
    /**
     * Parse the fromDate and toDate. Initialize fromDate to system date if it's null. Initialize fromDate to 6 months ago if's it null.
     * @param frDateStr
     * @param toDateStr
     * @return hashmap
     */
    public static HashMap<String, Timestamp> parseInputDate(String frDateStr, String toDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE);
        
        LocalDateTime localDateTime = null;
        
        // If from date empty, pre-select to 6 months before toDate
        if (frDateStr != null && !frDateStr.isEmpty()) {
        	try {
				localDateTime = LocalDateTime.parse(frDateStr, formatter);
			} catch (DateTimeParseException e) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE,"Invalid date format for fromDate: " + frDateStr);
			}
        } else {
        	localDateTime = LocalDateTime.now().minusMonths(MINUS_MONTHS);
        }
        
        Timestamp frDate = Timestamp.valueOf(localDateTime);
        
        // If to date empty, pre-select to Today
        if (toDateStr != null && !toDateStr.isEmpty()) {
        	try {
				localDateTime = LocalDateTime.parse(toDateStr, formatter);
			} catch (DateTimeParseException e) {
				throw new CommonException(CommonException.GENERIC_ERROR_CODE,"Invalid date format for toDate: " + toDateStr);
			}
        } else {
        	localDateTime = LocalDateTime.now();
        }
        
        Timestamp toDate = Timestamp.valueOf(localDateTime);

        HashMap<String, Timestamp> formattedDate = new HashMap<>();

        formattedDate.put("frDate", frDate);
        formattedDate.put("toDate", toDate);

        return formattedDate;
    }
    
    public static Timestamp parseInputDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATE_AUDIT_DATE_TIME);
        LocalDateTime localDateTime = null;

        try {
        	localDateTime = LocalDateTime.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
        	throw new CommonException(CommonException.GENERIC_ERROR_CODE,"Invalid date format: " + dateStr);
        }
        Timestamp dateTimestamp = Timestamp.valueOf(localDateTime);
        return dateTimestamp;
    }

}
