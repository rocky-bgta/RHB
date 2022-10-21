package com.rhbgroup.dcp.bo.batch.framework.utils;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.FTP;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchValidationException;
import com.rhbgroup.dcp.bo.batch.job.config.properties.HostFtpConfigProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Logger;

public final class FTPUtils extends BaseFTPUtils {

    private static final Logger logger = Logger.getLogger(FTPUtils.class);

    private FTPUtils() {
        throw new IllegalStateException("Utility Class");
    }

    public static void downloadFileFromFTP(String ftpFilePath, String targetFileFullPath, FTPConfigProperties ftpConfigProperties) throws BatchException {

        logger.info("Ftp filepath : " + ftpFilePath + " TargetFullpath: " + targetFileFullPath + " COnfig: " + ftpConfigProperties.toString());
        logger.info("Host : " + ftpConfigProperties.getHost() + " Usr :" + ftpConfigProperties.getUsername() + " Pass: " + ftpConfigProperties.getPassword() + " Port: " + ftpConfigProperties.getPort());

        if (!ftpConfigProperties.isIssecureftp()) {
            logger.info("IN IF Condition");
            downloadFileFromNonSecureFTP(ftpFilePath, targetFileFullPath, ftpConfigProperties);

        } else {
            logger.info("IN ELSE Condition");
            ChannelSftp sftpChannel = null;
            Session session = null;
            boolean isSuccess = false;
            int retryCount = 0;

            while (!isSuccess && retryCount <= FTP.MAX_RETRY) {
                logger.info("INSIDE WHILE LOOOOOOOOP");
                isSuccess = true;

                try {
                    sftpChannel = createChannelSftp(ftpConfigProperties);
                    session = sftpChannel.getSession();
                    logger.info("HOST " + session.getHost() + " :PORT: " + session.getPort() + " : USERNAME:" + session.getUserName());
                    session.setTimeout(ftpConfigProperties.getTimeout());
                    ftpFilePath = setFtpFilePath(ftpFilePath);
                    targetFileFullPath = setSourceFileFullPath(targetFileFullPath);
                    listFileSftpChannel(ftpFilePath, sftpChannel);

                    // Take note the target file path must exist or else exception get throw here
                    logger.info("FTP FILE PATH" + ftpFilePath + " TARGET FILE FULL PATH" + targetFileFullPath);
                    checkFolderExists(targetFileFullPath);
                    sftpChannel.get(ftpFilePath, targetFileFullPath);
                } catch (SftpException e) {
                    isSuccess = false;
                    if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                        String errorMessage = String.format(
                                "Error happened while FTP get file from [%s] to local [%s] using FTP [%s:%s]",
                                ftpFilePath, targetFileFullPath, ftpConfigProperties.getHost(),
                                ftpConfigProperties.getPort());
                        logger.error(errorMessage, e);
                    }
                    retryCount = validateNoOfSFTPRetry(isSuccess, retryCount, e);
                } catch (JSchException e) {
                    isSuccess = false;
                    String errorMessage = String.format(
                            "Error happened while FTP get file from [%s] to local [%s] using FTP [%s:%s]", ftpFilePath,
                            targetFileFullPath, ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
                    logger.error(errorMessage, e);
                    retryCount = validateNoOfSFTPRetry(isSuccess, retryCount, e);
                } finally {
                    disconnectSftpChannel(sftpChannel);
                    disconnectSession(session);
                }
            }
        }
    }

    private static void listFileSftpChannel(String ftpFilePath, ChannelSftp sftpChannel) {
        try {
            logger.info("****************inside try**********************");
            sftpChannel.ls(ftpFilePath);
        } catch (SftpException e) {
            logger.error(e);
        }
    }

    public static List<String> listFilesFromFtpFolder(String ftpFolderToList, FTPConfigProperties ftpConfigProperties) throws BatchException {
        ChannelSftp sftpChannel = null;
        Session session = null;
        boolean isSuccess = false;
        int retryCount = 0;
        List<String> fileList = null;

        if (!ftpConfigProperties.isIssecureftp()) {
            fileList = listFilesFromNonSecureFtpFolder(ftpFolderToList, ftpConfigProperties);
        } else {
            while (!isSuccess && retryCount <= FTP.MAX_RETRY) {
                isSuccess = true;
                // Each loop shall start with a fresh list
                fileList = new ArrayList<>();
                try {
                    sftpChannel = createChannelSftp(ftpConfigProperties);
                    session = sftpChannel.getSession();
                    session.setTimeout(ftpConfigProperties.getTimeout());
                    ftpFolderToList = setFtpFilePath(ftpFolderToList);
                    ArrayList<LsEntry> filelist = new ArrayList<>(sftpChannel.ls(ftpFolderToList));
                    for (LsEntry entry : filelist) {
                        // For directory, permission string start with 'd', symbolic start with 'l'
                        if (entry.getAttrs().getPermissionsString().charAt(0) == '-') {
                            fileList.add(entry.getFilename());
                        }
                    }
                } catch (JSchException | SftpException e) {
                    isSuccess = false;
                    String errorMessage = String.format("Error happened while list file(s) in [%s] from FTP [%s:%d]",
                            ftpFolderToList, ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
                    logger.error(errorMessage, e);
                    retryCount = validateNoOfSFTPRetry(isSuccess, retryCount, e);
                } finally {
                    disconnectSftpChannel(sftpChannel);
                    disconnectSession(session);
                }
            }
        }
        return fileList;
    }

    public static void createFTPFolderIfNotExists(String ftpFolderPath, FTPConfigProperties ftpConfigProperties) throws BatchValidationException {
        logger.debug(String.format("Create FTP folder [%s] if not exists in FTP [%s:%s]", ftpFolderPath,
                ftpConfigProperties.getHost(), ftpConfigProperties.getPort()));

        ChannelSftp sftpChannel = null;
        Session session = null;
        try {
            sftpChannel = createChannelSftp(ftpConfigProperties);
            session = sftpChannel.getSession();
            session.setTimeout(ftpConfigProperties.getTimeout());
            sftpChannel.cd(ftpFolderPath);
        } catch (SftpException e) {
            String errorMessage = String.format("Error happened traverse on FTP directory [%s] using FTP [%s:%s]",
                    ftpFolderPath, ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
            try {
                // Previous SftpException will cause the SFTP channel terminated, we need to
                // recreate a new one
                sftpChannel = createChannelSftp(ftpConfigProperties);
            } catch (JSchException e1) {
                errorMessage = String.format("Error happened getting SFTP channel using FTP [%s:%s]",
                        ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
                logger.error(errorMessage, e);
                throw new BatchValidationException(BatchErrorCode.FTP_SYSTEM_ERROR,
                        BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE, e);
            }
            // If SftpException happened, means folder not exists and we need to create for
            // it
            createFTPFolder(ftpFolderPath, sftpChannel);
        } catch (JSchException e) {
            String errorMessage = String.format("Error happened getting SFTP channel using FTP [%s:%s]",
                    ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
            logger.error(errorMessage, e);
            throw new BatchValidationException(BatchErrorCode.FTP_SYSTEM_ERROR, BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE,
                    e);
        } finally {
            disconnectSftpChannel(sftpChannel);
            disconnectSession(session);
        }
    }

    public static void uploadFileToFTP(String sourceFileFullPath, String ftpFilePath, FTPConfigProperties ftpConfigProperties) throws BatchException {
        logger.info(" uploadFileToFTP()");
        logger.info(" sourceFileFullPath: " + sourceFileFullPath);
        logger.info(" ftpFilePath: " + ftpFilePath);
        logger.info(" FTP host: " + ftpConfigProperties.getHost());
        logger.info(" FTP port: " + ftpConfigProperties.getPort());
        logger.info(" username: " + ftpConfigProperties.getUsername());
        logger.info(" issecure: " + ftpConfigProperties.isIssecureftp());
        if (!ftpConfigProperties.isIssecureftp()) {
            uploadFileToNonSecureFTP(sourceFileFullPath, ftpFilePath, ftpConfigProperties);
        } else {
            ChannelSftp sftpChannel = null;
            Session session = null;
            boolean isSuccess = false;
            int retryCount = 0;
            while (!isSuccess && retryCount <= FTP.MAX_RETRY) {
                isSuccess = true;
                try {
                    sftpChannel = createChannelSftp(ftpConfigProperties);
                    session = sftpChannel.getSession();
                    session.setTimeout(ftpConfigProperties.getTimeout());
                    ftpFilePath = setFtpFilePath(ftpFilePath);
                    sourceFileFullPath = setSourceFileFullPath(sourceFileFullPath);
                    sftpChannel.put(sourceFileFullPath, ftpFilePath);
                } catch (JSchException | SftpException e) {
                    isSuccess = false;
                    String errorMessage = String.format(ERROR_LITERAL_UPLOAD, sourceFileFullPath, ftpFilePath, ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
                    logger.error(errorMessage, e);
                    retryCount = validateNoOfSFTPRetry(isSuccess, retryCount, e);
                } finally {
                    disconnectSftpChannel(sftpChannel);
                    disconnectSession(session);
                }
            }
        }
    }

    private static String setFtpFilePath(String ftpFilePath) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return FTP.FTP_SEPARATOR + convertFTPForWindows(ftpFilePath);
        }
        return ftpFilePath;
    }

    private static String setSourceFileFullPath(String sourceFileFullPath) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return sourceFileFullPath.replace("%20", " ");
        }
        return sourceFileFullPath;
    }

    private static void disconnectSftpChannel(ChannelSftp sftpChannel) {
        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
        }
    }

    private static void disconnectSession(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    /**
     * Upload FTP file
     *
     * @param sourceFileFullPath
     * @param ftpFilePath
     * @param ftpConfigProperties
     * @param check
     * @throws BatchException
     * @deprecated This method will be remove in next release.
     */
    @Deprecated
    public static void uploadFileToFTP(String sourceFileFullPath, String ftpFilePath, FTPConfigProperties ftpConfigProperties, boolean check) throws BatchException {
        logger.info("  uploadFileToFTP()");
        logger.info("  sourceFileFullPath: " + sourceFileFullPath);
        logger.info("  ftpFilePath: " + ftpFilePath);
        logger.info("  FTP host: " + ftpConfigProperties.getHost());
        logger.info("  FTP port: " + ftpConfigProperties.getPort());
        logger.info("  username: " + ftpConfigProperties.getUsername());
        logger.info("  issecure: " + ftpConfigProperties.isIssecureftp());
        if (!ftpConfigProperties.isIssecureftp()) {
            uploadFileToNonSecureFTP(sourceFileFullPath, ftpFilePath, ftpConfigProperties);
        } else {
            ChannelSftp sftpChannel = null;
            Session session = null;
            boolean isSuccess = false;
            int retryCount = 0;
            while (!isSuccess && retryCount <= FTP.MAX_RETRY) {
                isSuccess = true;
                try {
                    sftpChannel = createChannelSftp(ftpConfigProperties);
                    session = sftpChannel.getSession();
                    session.setTimeout(ftpConfigProperties.getTimeout());
                    ftpFilePath = setFtpFilePath(ftpFilePath);
                    sourceFileFullPath = setSourceFileFullPath(sourceFileFullPath);
                    setSftpFilePath(sourceFileFullPath, ftpFilePath, check, sftpChannel);
                } catch (JSchException | SftpException e) {
                    isSuccess = false;
                    String errorMessage = String.format(
                            ERROR_LITERAL_UPLOAD,
                            sourceFileFullPath, ftpFilePath, ftpConfigProperties.getHost(),
                            ftpConfigProperties.getPort());
                    logger.error(errorMessage, e);
                    retryCount = validateNoOfSFTPRetry(isSuccess, retryCount, e);
                } finally {
                    disconnectSftpChannel(sftpChannel);
                    disconnectSession(session);
                }
            }
        }
    }

    /**
     * Upload FTP file
     *
     * @param sourceFileFullPath
     * @param ftpFilePath
     * @param ftpConfigProperties
     * @param check
     * @param date
     * @throws BatchException
     * @deprecated This method will be remove in next release.
     */
    @Deprecated
    public static void uploadFileToFTP(String sourceFileFullPath, String ftpFilePath, FTPConfigProperties ftpConfigProperties, boolean check, String date) throws BatchException {
        logger.info("   uploadFileToFTP()");
        logger.info("   sourceFileFullPath: " + sourceFileFullPath);
        logger.info("   ftpFilePath: " + ftpFilePath);
        logger.info("   FTP host: " + ftpConfigProperties.getHost());
        logger.info("   FTP port: " + ftpConfigProperties.getPort());
        logger.info("   username: " + ftpConfigProperties.getUsername());
        logger.info("   issecure: " + ftpConfigProperties.isIssecureftp());

        if (!ftpConfigProperties.isIssecureftp()) {
            uploadFileToNonSecureFTP(sourceFileFullPath, ftpFilePath, ftpConfigProperties);
        } else {
            ChannelSftp sftpChannel = null;
            Session session = null;
            boolean isSuccess = false;
            int retryCount = 0;
            while (!isSuccess && retryCount <= FTP.MAX_RETRY) {
                isSuccess = true;
                try {
                    sftpChannel = createChannelSftp(ftpConfigProperties);
                    session = sftpChannel.getSession();
                    session.setTimeout(ftpConfigProperties.getTimeout());
                    ftpFilePath = setFtpFilePath(ftpFilePath);
                    sourceFileFullPath = setSourceFileFullPath(sourceFileFullPath);
                    setSftpChannelFile(check, sourceFileFullPath, sftpChannel, ftpFilePath);
                } catch (JSchException | SftpException e) {
                    isSuccess = false;
                    String errorMessage = String.format(
                            ERROR_LITERAL_UPLOAD,
                            sourceFileFullPath, ftpFilePath, ftpConfigProperties.getHost(),
                            ftpConfigProperties.getPort());
                    logger.error(errorMessage, e);
                    retryCount = validateNoOfSFTPRetry(isSuccess, retryCount, e);
                } finally {
                    disconnectSftpChannel(sftpChannel);
                    disconnectSession(session);
                }
            }
        }
    }

    /**
     * Upload FTP file
     *
     * @param sourceFileFullPath
     * @param ftpFilePath
     * @param hostFtpConfigProperties
     * @param check
     * @throws BatchException
     * @deprecated This method will be remove in next release.
     */
    @Deprecated
    public static void uploadFileToFTP(String sourceFileFullPath, String ftpFilePath, HostFtpConfigProperties hostFtpConfigProperties, boolean check) throws BatchException {
        logger.info("    uploadFileToFTP()");
        logger.info("    sourceFileFullPath: " + sourceFileFullPath);
        logger.info("    ftpFilePath: " + ftpFilePath);
        logger.info("    FTP host: " + hostFtpConfigProperties.getHost());
        logger.info("    FTP port: " + hostFtpConfigProperties.getPort());
        logger.info("    username: " + hostFtpConfigProperties.getUsername());
        logger.info("    password: " + hostFtpConfigProperties.getPassword());
        logger.info("    issecure: " + hostFtpConfigProperties.isIssecureftp());

        if (!hostFtpConfigProperties.isIssecureftp()) {
            uploadFileToNonSecureFTP(sourceFileFullPath, ftpFilePath, hostFtpConfigProperties);
        } else {
            ChannelSftp sftpChannel = null;
            Session session = null;
            boolean isSuccess = false;
            int retryCount = 0;
            while (!isSuccess && retryCount <= FTP.MAX_RETRY) {
                isSuccess = true;
                try {
                    sftpChannel = createChannelSftp(hostFtpConfigProperties);
                    session = sftpChannel.getSession();
                    session.setTimeout(hostFtpConfigProperties.getTimeout());
                    ftpFilePath = setFtpFilePath(ftpFilePath);
                    sourceFileFullPath = setSourceFileFullPath(sourceFileFullPath);
                    setSftpChannelFile(check, sourceFileFullPath, sftpChannel, ftpFilePath);
                } catch (JSchException | SftpException e) {
                    isSuccess = false;
                    String errorMessage = String.format(
                            ERROR_LITERAL_UPLOAD,
                            sourceFileFullPath, ftpFilePath, hostFtpConfigProperties.getHost(),
                            hostFtpConfigProperties.getPort());
                    logger.error(errorMessage, e);
                    retryCount = validateNoOfSFTPRetry(isSuccess, retryCount, e);
                } finally {
                    disconnectSftpChannel(sftpChannel);
                    disconnectSession(session);
                }
            }
        }
    }

    private static void setSftpFilePath(String sourceFileFullPath, String ftpFilePath, boolean check, ChannelSftp sftpChannel) throws SftpException {
        if (check) {
            File direc = new File(sourceFileFullPath);
            File[] fList = direc.listFiles();
            for (File file : fList) {
                if (file.isFile()) {
                    String fileName = file.getAbsolutePath();
                    logger.info("File Name : " + fileName);
                    sftpChannel.put(fileName, ftpFilePath, ChannelSftp.OVERWRITE);
                }
            }
        } else {
            logger.info("inside else()");
            sftpChannel.put(sourceFileFullPath, ftpFilePath);
        }
    }

    private static void setSftpChannelFile(boolean check, String sourceFileFullPath, ChannelSftp sftpChannel, String ftpFilePath) throws SftpException {
        if (check) {
            List<String> fileList = new ArrayList<>();
            File direc = new File(sourceFileFullPath);
            File[] fList = direc.listFiles();
            for (File entry : fList) {
                fileList.add(entry.getName());
            }
            if (!fileList.isEmpty()) {
                for (String name : fileList) {
                    if (checkFileToProcess(name, DateUtils.getPostingFileCurrentDate(), true)) {
                        logger.info("File to be transferred is: " + sourceFileFullPath + "/" + name);
                        sftpChannel.put(sourceFileFullPath + "/" + name, checkFolderExists(ftpFilePath), ChannelSftp.OVERWRITE);
                    }
                }
            }
        } else {
            logger.info("inside else()");
            sftpChannel.put(sourceFileFullPath, ftpFilePath);
        }
    }

    private static void uploadFileToNonSecureFTP(String sourceFileFullPath, String ftpFilePath, FTPConfigProperties ftpConfigProperties) throws BatchException {
        FTPClient ftpClient = new FTPClient();
        boolean isSuccessLogin = false;
        boolean isSuccess = false;
        int retryCount = 0;
        while (!isSuccess && retryCount <= FTP.MAX_RETRY) {
            try (FileInputStream fis = new FileInputStream(sourceFileFullPath)) {
                ftpClient.connect(ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
                ftpClient.setConnectTimeout(ftpConfigProperties.getTimeout());
                isSuccessLogin = ftpClient.login(ftpConfigProperties.getUsername(), ftpConfigProperties.getPassword());
                if (isSuccessLogin) {
                    isSuccess = ftpClient.storeFile(ftpFilePath, fis);
                    if (!isSuccess) {
                        logger.warn(String.format("Failed to upload file to FTP Server [%s:%d], ReplyString[%s]",
                                ftpConfigProperties.getHost(), ftpConfigProperties.getPort(),
                                ftpClient.getReplyString()));
                        retryCount = checkNoOfRetry(retryCount, isSuccess);
                        checkRetry("", ftpConfigProperties, ftpClient, retryCount, "Failed to upload file to FTP Server [%s:%d], ReplyString[%s], exceeded MAX retry", BatchErrorCode.FILE_NOT_FOUND, BatchErrorCode.FILE_NOT_FOUND_MESSAGE);
                    }
                } else {
                    logger.warn(String.format(ERROR_LITERAL_LOGIN,
                            ftpConfigProperties.getHost(), ftpConfigProperties.getPort(), ftpClient.getReplyString()));
                    retryCount = checkNoOfRetry(retryCount, isSuccess);
                    checkRetry("", ftpConfigProperties, ftpClient, retryCount, ERROR_LITERAL_MAX_RETRY, BatchErrorCode.FTP_SYSTEM_ERROR, BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                logger.warn(String.format("Failed to upload file to FTP Server [%s:%d]", ftpConfigProperties.getHost(),
                        ftpConfigProperties.getPort()), ex);
                retryCount = checkNoOfRetry(retryCount, isSuccess);
                if (retryCount == FTP.MAX_RETRY) {
                    logger.error(String.format("Failed to upload file to FTP Server [%s:%d], exceeded MAX retry",
                            ftpConfigProperties.getHost(), ftpConfigProperties.getPort()));
                    throw new BatchException(BatchErrorCode.FTP_SYSTEM_ERROR, BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE,
                            ex);
                }
            } finally {
                closeFtpCLient(ftpClient, ftpConfigProperties);
            }
        }
    }

    private static void downloadFileFromNonSecureFTP(String ftpFilePath, String targetFileFullPath, FTPConfigProperties ftpConfigProperties) throws BatchException {
        FTPClient ftpClient = new FTPClient();
        boolean isSuccessLogin = false;
        boolean isSuccess = false;
        int retryCount = 0;
        while (!isSuccess && (retryCount <= FTP.MAX_RETRY)) {
            logger.info("INSIDE WHILE LOOP");
            try (FileOutputStream fos = new FileOutputStream(targetFileFullPath)) {
                ftpClient.connect(ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
                ftpClient.setConnectTimeout(ftpConfigProperties.getTimeout());
                isSuccessLogin = ftpClient.login(ftpConfigProperties.getUsername(), ftpConfigProperties.getPassword());
                if (isSuccessLogin) {
                    isSuccess = ftpClient.retrieveFile(ftpFilePath, fos);
                    if (!isSuccess) {
                        logger.warn(String.format("Failed to retrieve file from FTP Server [%s:%d], ReplyString[%s]",
                                ftpConfigProperties.getHost(), ftpConfigProperties.getPort(),
                                ftpClient.getReplyString()));
                        retryCount = checkNoOfRetry(retryCount, isSuccess);
                        checkRetry("", ftpConfigProperties, ftpClient, retryCount, "Failed to retrieve file from FTP Server [%s:%d], ReplyString[%s], exceeded MAX retry", BatchErrorCode.FILE_NOT_FOUND, BatchErrorCode.FILE_NOT_FOUND_MESSAGE);
                    }
                } else {
                    logger.warn(String.format(ERROR_LITERAL_LOGIN,
                            ftpConfigProperties.getHost(), ftpConfigProperties.getPort(), ftpClient.getReplyString()));
                    retryCount = checkNoOfRetry(retryCount, isSuccess);
                    checkRetry("", ftpConfigProperties, ftpClient, retryCount, ERROR_LITERAL_MAX_RETRY, BatchErrorCode.FTP_SYSTEM_ERROR, BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                logger.warn(String.format("Failed to download file from FTP Server [%s:%d]",
                        ftpConfigProperties.getHost(), ftpConfigProperties.getPort()), ex);
                retryCount = checkNoOfRetry(retryCount, isSuccess);
                if (retryCount == FTP.MAX_RETRY) {
                    logger.error(String.format("Failed to download file from FTP Server [%s:%d], exceeded MAX retry",
                            ftpConfigProperties.getHost(), ftpConfigProperties.getPort()));
                    throw new BatchException(BatchErrorCode.FTP_SYSTEM_ERROR, BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE,
                            ex);
                }
            } finally {
                closeFtpCLient(ftpClient, ftpConfigProperties);
            }
        }

        logger.info("DID NOthing in while loop");
    }

    private static List<String> listFilesFromNonSecureFtpFolder(String ftpFolderToList, FTPConfigProperties ftpConfigProperties) throws BatchException {
        FTPClient ftpClient = new FTPClient();
        boolean isSuccessLogin = false;
        boolean isSuccess = false;
        int retryCount = 0;
        List<String> fileList = null;
        while (!isSuccess && (retryCount <= FTP.MAX_RETRY)) {
            fileList = new ArrayList<>();
            try {
                ftpClient.connect(ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
                ftpClient.setConnectTimeout(ftpConfigProperties.getTimeout());
                isSuccessLogin = ftpClient.login(ftpConfigProperties.getUsername(), ftpConfigProperties.getPassword());
                if (isSuccessLogin) {
                    isSuccess = ftpClient.changeWorkingDirectory(ftpFolderToList);
                    if (!isSuccess) {
                        logger.warn(String.format(
                                "Failed to change working directory to [%s] from FTP Server [%s:%d], ReplyString[%s]",
                                ftpFolderToList, ftpConfigProperties.getHost(), ftpConfigProperties.getPort(),
                                ftpClient.getReplyString()));
                        retryCount = checkNoOfRetry(retryCount, isSuccess);
                        checkRetry(ftpFolderToList, ftpConfigProperties, ftpClient, retryCount, "Failed to change working directory to [%s] from FTP Server [%s:%d], ReplyString[%s], exceeded MAX retry", BatchErrorCode.FILE_NOT_FOUND, BatchErrorCode.FILE_NOT_FOUND_MESSAGE);
                    } else {
                        fileList = setFileList(ftpClient);
                    }
                } else {
                    logger.warn(String.format(ERROR_LITERAL_LOGIN,
                            ftpConfigProperties.getHost(), ftpConfigProperties.getPort(), ftpClient.getReplyString()));
                    retryCount = checkNoOfRetry(retryCount, isSuccess);
                    checkRetry("", ftpConfigProperties, ftpClient, retryCount, ERROR_LITERAL_MAX_RETRY, BatchErrorCode.FTP_SYSTEM_ERROR, BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                logger.warn(String.format("Failed to list file(s) in [%s] from FTP Server [%s:%d]",
                                ftpFolderToList,
                                ftpConfigProperties.getHost(),
                                ftpConfigProperties.getPort()),
                        ex);
                retryCount = checkNoOfRetry(retryCount, isSuccess);
                if (retryCount == FTP.MAX_RETRY) {
                    logger.error(
                            String.format("Failed to list file(s) in [%s] from FTP Server [%s:%d], exceeded MAX retry",
                                    ftpFolderToList,
                                    ftpConfigProperties.getHost(),
                                    ftpConfigProperties.getPort()));
                    throw new BatchException(BatchErrorCode.FTP_SYSTEM_ERROR,
                            BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE,
                            ex);
                }
            } finally {
                closeFtpCLient(ftpClient, ftpConfigProperties);
            }
        }
        return fileList;
    }

    private static void checkRetry(String ftpFolderToList, FTPConfigProperties ftpConfigProperties, FTPClient ftpClient, int retryCount, String errMsg, String errCode, String exMsg) throws BatchValidationException {
        if (retryCount == FTP.MAX_RETRY) {
            logger.error(String.format(
                    errMsg,
                    ftpFolderToList,
                    ftpConfigProperties.getHost(),
                    ftpConfigProperties.getPort(),
                    ftpClient.getReplyString()));
            throw new BatchValidationException(errCode, exMsg);
        }
    }

    private static void closeFtpCLient(FTPClient ftpClient, FTPConfigProperties ftpConfigProperties) {
        ftpClient.setConnectTimeout(0);
        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                logger.warn(String.format(ERROR_LITERAL_CLOSE,
                        ftpConfigProperties.getHost(), ftpConfigProperties.getPort()), e);
            }
        }
    }

    private static List<String> setFileList(FTPClient ftpClient) throws IOException {
        List<String> fileList = new ArrayList<>();
        FTPFile[] ftpFiles = ftpClient.listFiles();
        for (FTPFile ftpFile : ftpFiles) {
            if (ftpFile.getType() == FTPFile.FILE_TYPE) {
                fileList.add(ftpFile.getName());
            }
        }
        return fileList;
    }

    private static ChannelSftp createChannelSftp(FTPConfigProperties ftpConfigProperties) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(ftpConfigProperties.getUsername(), ftpConfigProperties.getHost(), ftpConfigProperties.getPort());
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(ftpConfigProperties.getPassword());
        session.connect();
        Channel channel = session.openChannel("sftp");
        channel.connect();
        return (ChannelSftp) channel;
    }

    private static String checkFolderExists(String targetFileFullPath) {
        File directory = new File(targetFileFullPath);
        if (!directory.exists()) {
            directory.mkdir();
        }
        logger.info("Target File Full Path ::::  " + targetFileFullPath);
        return targetFileFullPath;
    }

    private static void createFTPFolder(String folderPath, ChannelSftp sftpChannel) throws BatchValidationException {
        logger.debug(String.format("Creating FTP folder [%s]", folderPath));

        // Start the current folder with a backlash which expected by FTP no matter is
        // Windows or Linux
        StringBuilder currentFolder = new StringBuilder(FTP.FTP_SEPARATOR);
        boolean isDrive = true;

        if (SystemUtils.IS_OS_WINDOWS) {
            folderPath = convertFTPForWindows(folderPath);
            logger.debug(String.format("Converted FTP folder for Windows [%s]", folderPath));
        }

        String[] folders = folderPath.split(FTP.FTP_SEPARATOR);
        for (String folder : folders) {
            if (folder.length() > 0) {
                // For Windows, we need to skip the Drive
                if (SystemUtils.IS_OS_WINDOWS && isDrive) {
                    currentFolder.append(folder).append(FTP.FTP_SEPARATOR);
                    isDrive = false;
                    continue;
                }
                // FTP not support to create subfolders directly, we need to create from root
                // and each subfolder step by step
                try {
                    currentFolder.append(folder).append(FTP.FTP_SEPARATOR);
                    sftpChannel.cd(currentFolder.toString());
                } catch (SftpException e) {
                    try {
                        sftpChannel.mkdir(folder);
                        sftpChannel.cd(folder);
                    } catch (SftpException e2) {
                        String errorMessage = String.format(
                                "Error happened while creating or traverse the FTP folder [%s]",
                                currentFolder);
                        logger.error(errorMessage, e2);
                        throw new BatchValidationException(BatchErrorCode.FTP_SYSTEM_ERROR,
                                BatchErrorCode.FTP_SYSTEM_ERROR_MESSAGE, e2);
                    }

                }
            }
        }
    }

    public static String convertFTPForWindows(String folderPath) {
        // For FTP in Windows environment, we always assume the first letter found in
        // path is the Drive letter
        // Since FTP only support backslash '/', we will need to convert all the slash
        // to backslash instead
        folderPath = folderPath.replace("\\\\", BatchSystemConstant.FTP.FTP_SEPARATOR);
        // Remove the colon from the drive, FTP will not expect got colon in the path
        folderPath = folderPath.replaceFirst(":", "");
        return folderPath;
    }

}
