package com.rhbgroup.dcp.bo.batch.test.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jgroups.util.Util.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.SocketException;
import java.util.List;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import freemarker.template.Configuration;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import com.rhbgroup.dcp.bo.batch.framework.config.properties.FTPConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.FTPUtils;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfig.class})
@ActiveProfiles("test")
public class FTPUtilsTest {

	private static final Logger logger = Logger.getLogger(FTPUtilsTest.class);

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	private ClassLoader classLoader;
    private FakeFtpServer fakeFtpServer;
    @Autowired
    private FTPConfigProperties ftpConfigProperties;
    private String ftpBasePath;
    private boolean isSuccessLogin;
    private String localWorkingDir;

	@Test
	public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<FTPUtils> constructor = FTPUtils.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);

		ExpectedException expectedException = ExpectedException.none();

		expectedException.expect(InvocationTargetException.class);

		try {
			constructor.newInstance();
		}catch(InvocationTargetException itx){
			assertNotNull(itx.getCause());
		}
	}

	@Before
	public void setup() throws IOException {
		classLoader = getClass().getClassLoader();
		localWorkingDir = System.getProperty("user.dir");
		ftpBasePath = "/data";
		ftpConfigProperties.setHost("localhost");

		fakeFtpServer = new FakeFtpServer();
		fakeFtpServer.addUserAccount(
				new UserAccount(ftpConfigProperties.getUsername()
						, ftpConfigProperties.getPassword()
						, ftpBasePath));

		UnixFakeFileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry(ftpBasePath));
		fileSystem.add(new FileEntry(ftpBasePath + "/testfile.txt", "This is just a saple test."));
		fileSystem.add(new DirectoryEntry(ftpBasePath + "/upload"));
		fakeFtpServer.setFileSystem(fileSystem);
		fakeFtpServer.setServerControlPort(10000);
		if(fakeFtpServer.isStarted()) {
			fakeFtpServer.stop();
		}
		fakeFtpServer.start();

		FTPClient ftpClient = new FTPClient();
		ftpClient.connect(ftpConfigProperties.getHost(), fakeFtpServer.getServerControlPort());
		int reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			ftpClient.disconnect();
			throw new IOException("Exception in connecting to FTP Server");
		}
		isSuccessLogin = ftpClient.login(ftpConfigProperties.getUsername(), ftpConfigProperties.getPassword());
		if (isSuccessLogin) {
			logger.info(ftpClient.printWorkingDirectory());
			logger.info("Listing out file(s): ");
			for (String name : ftpClient.listNames()) {
				logger.info(String.format("::%s", name));
			}
		} else {
			logger.info("Failed to login to FTP Server: ");
		}
	}
	
	@After
	public void destroy() {
		fakeFtpServer.stop();
	}

	@Test
	public void testDownloadFileFromFTP() throws SocketException, IOException, BatchException {
		String localDir = generateFolderPath(localWorkingDir, "target");
		String sourceFtpPath = ftpBasePath + "/testfile.txt";
		String targetFilePath = localDir + "/testfile.txt";
		ftpConfigProperties.setIssecureftp(false);
		FTPUtils.downloadFileFromFTP(sourceFtpPath, targetFilePath, ftpConfigProperties);
		assertThat(new File(targetFilePath)).exists();
	}
	@Test
	public void testUploadFileToFTP() throws SocketException, IOException, BatchException {
		String ftpFilePath = ftpBasePath + "/upload/testfile.txt";
		String sourceFileFullPath = ResourceUtils.getFile(this.getClass().getClassLoader().getResource("ftp/UNIT_TEST/testfile.txt")).getAbsolutePath();
		ftpConfigProperties.setIssecureftp(false);
		FTPUtils.uploadFileToFTP(sourceFileFullPath, ftpFilePath, ftpConfigProperties);
		assertThat(fakeFtpServer.getFileSystem().exists(ftpFilePath)).isTrue();
	}
	private String generateFolderPath(String... sources) {
		StringBuilder strBuilder = new StringBuilder();
		for(String source : sources) {
			strBuilder.append(source).append(File.separator);
		}
		return strBuilder.toString();
	}
	@Test
	public void testListFilesFromFTP() throws SocketException, IOException, BatchException {
		testUploadFileToFTP();
		ftpConfigProperties.setIssecureftp(false);
		String ftpFolderToList = ftpBasePath + "/upload/";
		List<String> fileList = FTPUtils.listFilesFromFtpFolder(ftpFolderToList, ftpConfigProperties);
		logger.info("File list : " + fileList.get(0));
		assertEquals(1,fileList.size());
	}
	
}
