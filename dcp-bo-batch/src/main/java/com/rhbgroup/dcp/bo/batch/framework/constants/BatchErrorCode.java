package com.rhbgroup.dcp.bo.batch.framework.constants;

public final class BatchErrorCode {
	
	private BatchErrorCode() {
		throw new IllegalStateException("Constant class");
	}
	
    public static final String SUCCESS = "10000";
    public static final String FILE_NOT_FOUND = "20001";
    public static final String FIELD_VALIDATION_ERROR = "20002";
    public static final String FILE_VALIDATION_ERROR = "20003";
    public static final String BATCH_DEPENDENCY_MISSING_ERROR = "30002";
    public static final String GENERIC_SYSTEM_ERROR = "80000";
    public static final String FTP_SYSTEM_ERROR = "80001";
    public static final String DB_SYSTEM_ERROR = "80002";
    public static final String CONFIG_SYSTEM_ERROR = "80003";
    public static final String JASPER_CLIENT_ERROR = "80004";

    public static final String SUCCESS_MESSAGE = "No Exception";
    public static final String FILE_NOT_FOUND_MESSAGE = "File Not Found";
    public static final String FILE_VALIDATION_ERROR_MESSAGE = "File Validation Exception";
    public static final String FIELD_VALIDATION_ERROR_MESSAGE = "Format Validation Exception";
    public static final String BATCH_DEPENDENCY_MISSING_ERROR_MESSAGE = "Batch Dependencies missing Exception";
    public static final String GENERIC_SYSTEM_ERROR_MESSAGE = "General System Exception";
    public static final String FTP_SYSTEM_ERROR_MESSAGE = "General FTP Exception";
    public static final String DB_SYSTEM_ERROR_MESSAGE = "General DB Exception";
    public static final String CONFIG_SYSTEM_ERROR_MESSAGE = "General Batch Config Exception";
    public static final String JASPER_CLIENT_ERROR_MESSAGE = "General Jasper Server Client Exception";

}