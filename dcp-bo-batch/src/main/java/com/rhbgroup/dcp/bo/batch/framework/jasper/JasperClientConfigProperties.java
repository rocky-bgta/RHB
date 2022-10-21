package com.rhbgroup.dcp.bo.batch.framework.jasper;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import lombok.Getter;
import lombok.Setter;

@Lazy
@Configuration
@ConfigurationProperties(prefix="jasper.server")
@Setter
@Getter
public class JasperClientConfigProperties{
	private String url;
	private String connectionTimeout;
	private String readTimeout;
	private String jasperserverVersion;
	private String authenticationType;
	private String logHttp;
	private String logHttpEntity;
	private String restrictedHttpMethods;
	private String handleErrors;
	private String contentMimeType;
	private String acceptMimeType;
	private String username;
	private String password;
	@Override
	public String toString() {
		return String.format("JasperClientConfigProperties: url='%s', username='%s'", url, username);
	}
	public Properties toProperties() {
		Properties prop = new Properties();
		if(url != null) {
			prop.setProperty("url", url);
		}
		if(connectionTimeout != null) {
			prop.setProperty("connectionTimeout", connectionTimeout);
		}
		if(readTimeout != null) {
			prop.setProperty("readTimeout", readTimeout);
		}
		if(jasperserverVersion != null) {
			prop.setProperty("jasperserverVersion", jasperserverVersion);
		}
		if(authenticationType != null) {
			prop.setProperty("authenticationType", authenticationType);
		}
		if(logHttp != null) {
			prop.setProperty("logHttp", logHttp);
		}
		if(logHttpEntity != null) {
			prop.setProperty("logHttpEntity", logHttpEntity);
		}
		if(restrictedHttpMethods != null) {
			prop.setProperty("restrictedHttpMethods", restrictedHttpMethods);
		}
		if(handleErrors != null) {
			prop.setProperty("handleErrors", handleErrors);
		}
		if(contentMimeType != null) {
			prop.setProperty("contentMimeType", contentMimeType);
		}
		if(acceptMimeType != null) {
			prop.setProperty("acceptMimeType", acceptMimeType);
		}
		if(username != null) {
			prop.setProperty("username", username);
		}
		if(password != null) {
			prop.setProperty("password", password);
		}
		return prop;
	}
}
