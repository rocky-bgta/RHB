/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rhbgroup.dcp.bo.batch.framework.utils;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import org.apache.log4j.Logger;

/**
 * @author mhailmizam
 */
public abstract class BaseFTPUtils {

    private static final Logger logger = Logger.getLogger(BaseFTPUtils.class);
    private static final String ASNBPOSTINGFILE = "ASNB_DCP_Settlement";
    private static final String ERROR_LITERAL = "Failed to wait. Retrying without waiting: %s";
    protected static final String ERROR_LITERAL_UPLOAD = "Error happened while upload local file [%s] to FTP [%s] using FTP [%s:%s]";
    protected static final String ERROR_LITERAL_CLOSE = "Failed to close FTP client connection [%s:%d]";
    protected static final String ERROR_LITERAL_MAX_RETRY = "Failed to login to FTP Server [%s:%d], ReplyString[%s], exceeded MAX retry";
    protected static final String ERROR_LITERAL_LOGIN = "Failed to login to FTP Server [%s:%d], ReplyString[%s]";

    protected BaseFTPUtils() {

    }

    protected static int checkNoOfRetry(int retryCount, boolean isSuccess) {
        if (!isSuccess && (retryCount < BatchSystemConstant.FTP.MAX_RETRY)) {
            logger.warn(String.format("Retrying. (No. of retry: %d)", ++retryCount));
            try {
                Thread.sleep(BatchSystemConstant.FTP.SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.error(String.format(ERROR_LITERAL, e.getMessage()));
                Thread.currentThread().interrupt();
            }
        }
        return retryCount;
    }

    /**
     * Check No Of Retry Download
     *
     * @param retryCount
     * @param isSuccess
     * @return no of try
     * @deprecated This method is no longer acceptable to retry time between versions.
     */
    @Deprecated
    protected static int checkNoOfRetryDownload(int retryCount, boolean isSuccess) {
        if (!isSuccess && (retryCount < BatchSystemConstant.FTP.MAX_RETRY)) {
            logger.warn(String.format("Error while trying to download file from FTP. Retrying. (No. of retry: %s)",
                    ++retryCount));
            try {
                Thread.sleep(BatchSystemConstant.FTP.SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.error(String.format(ERROR_LITERAL, e.getMessage()));
                Thread.currentThread().interrupt();
            }
        }
        return retryCount;
    }

    /**
     * Check No Of Retry Upload
     *
     * @param isSuccess
     * @param retryCount
     * @return no of try
     * @deprecated This method is no longer acceptable to retry time between
     * versions.
     */
    @Deprecated
    protected static int checkNoOfRetryUpload(int retryCount, boolean isSuccess) {
        if (!isSuccess && (retryCount < BatchSystemConstant.FTP.MAX_RETRY)) {
            logger.warn(String.format("Error while trying to upload file to FTP. Retrying. (No. of retry: %s)",
                    ++retryCount));
            try {
                Thread.sleep(BatchSystemConstant.FTP.SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.error(String.format(ERROR_LITERAL, e.getMessage()));
                Thread.currentThread().interrupt();
            }
        }
        return retryCount;
    }

    protected static int validateNoOfSFTPRetry(boolean isSuccess, int retryCount, Exception e) throws BatchException {
        if (!isSuccess && (retryCount < BatchSystemConstant.FTP.MAX_RETRY)) {
            logger.warn(String.format("Retrying. (No. of retry: %s)", ++retryCount));
            try {
                Thread.sleep(BatchSystemConstant.FTP.SLEEP_TIME);
            } catch (InterruptedException ex) {
                logger.error(String.format(ERROR_LITERAL, ex.getMessage()));
                Thread.currentThread().interrupt();
            }
        } else {
            throw new BatchException(BatchErrorCode.FTP_SYSTEM_ERROR, BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE, e);
        }
        return retryCount;
    }

    /**
     * Check No Of Retry File Not Found
     *
     * @param isSuccess
     * @param retryCount
     * @param exception
     * @return no of try
     * @throws BatchException
     * @deprecated This method is no longer acceptable to retry time between versions.
     */
    @Deprecated
    protected static int validateNoOfSFTPRetryFileNotFound(boolean isSuccess, int retryCount, Exception exception) throws BatchException {
        if (!isSuccess && (retryCount < BatchSystemConstant.FTP.MAX_RETRY)) {
            logger.warn(String.format("Retrying. (No. of retry: %s)", ++retryCount));
            try {
                Thread.sleep(BatchSystemConstant.FTP.SLEEP_TIME);
            } catch (InterruptedException ex) {
                logger.error(String.format(ERROR_LITERAL, ex.getMessage()));
                Thread.currentThread().interrupt();
            }
        } else {
            throw new BatchException(BatchErrorCode.FILE_NOT_FOUND, BatchErrorCode.FILE_NOT_FOUND_MESSAGE, exception);
        }
        return retryCount;
    }

    protected static boolean checkFileToProcess(String name, String asnbDte, boolean postingFileCheck) {
        String date;
        if (postingFileCheck && name.contains(ASNBPOSTINGFILE)) {
            date = name.substring(20, 28);
        } else {
            date = name.substring(10, 18);
        }

        logger.info(date + " ::" + asnbDte);
        if (date.equals(asnbDte)) {
            logger.info(date + " :equal: " + asnbDte);
            return true;
        } else {
            return false;
        }

    }

}
