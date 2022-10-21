package com.rhbgroup.dcp.bo.batch.test.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchValidationException;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.test.ftp.MyPasswordAuthenticator;

public abstract class BaseFTPJobTest extends BaseJobTest {
	
	private static final int MAX_FTP_RECONNECT_RETRY = 3;
	protected SshServer sshd;
	
	@Autowired
	protected FTPConfigProperties defaultFTPConfigProperties;
	
	private FTPConfigProperties customFTPConfigProperties;
	
	@Before
	public void beforeFTPTest() throws IOException {
		super.beforeTest();
		setupSshServer();
	}
	
	@After
	public void afterFTPTest() throws Exception {
		shutdownSSHServer();
	}
	
	protected void setCustomFTP(FTPConfigProperties ftpConfigProperties) {
		customFTPConfigProperties = ftpConfigProperties;
	}
	
	protected void uploadFileToFTPFolder(File file, String targetFolder) throws BatchValidationException {
		FTPConfigProperties ftpConfigPropertiesToBeUsed = (customFTPConfigProperties != null) ? customFTPConfigProperties : defaultFTPConfigProperties;
		// Sometime the local FTP used for testing might not be stable, since it is only for testing we can use retry here
		int retry = MAX_FTP_RECONNECT_RETRY;
		do {
			try {
				FTPUtils.createFTPFolderIfNotExists(targetFolder, ftpConfigPropertiesToBeUsed);
				FTPUtils.uploadFileToFTP(file.getAbsolutePath(), targetFolder, ftpConfigPropertiesToBeUsed);
				break;
			} catch(Exception e) {
				retry--;
			}
		} while(retry != 0);
	}
	
	protected void setupSshServer() throws IOException {
		// Already started, skip it
		if(sshd != null && !sshd.isClosed()) {
			return;
		}
		
		FTPConfigProperties ftpConfigPropertiesToBeUsed = (customFTPConfigProperties != null) ? customFTPConfigProperties : defaultFTPConfigProperties;
		
		sshd = SshServer.setUpDefaultServer();
		MyPasswordAuthenticator myPassAuthenticator = new MyPasswordAuthenticator(ftpConfigPropertiesToBeUsed.getUsername(), ftpConfigPropertiesToBeUsed.getPassword());
		sshd.setPasswordAuthenticator(myPassAuthenticator);
		sshd.setPort(ftpConfigPropertiesToBeUsed.getPort());
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("target/hostkey.ser"));
		//sshd.setShellFactory(new ProcessShellFactory(new String[] { "/bin/sh", "-i", "-l" }));
		sshd.setCommandFactory(new ScpCommandFactory());
		
		List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>(); 
		namedFactoryList.add(new SftpSubsystem.Factory()); 
		sshd.setSubsystemFactories(namedFactoryList);
		
		sshd.start();
	}
	
	protected void shutdownSSHServer() throws InterruptedException {
		if(!sshd.isClosed()) {
			sshd.stop();
		}
	}
	
}
