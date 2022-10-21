package com.rhbgroup.dcp.bo.batch.test.ftp;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

public class MyPasswordAuthenticator implements PasswordAuthenticator {
	
	private String username;
	private String password;
	
	public MyPasswordAuthenticator(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	@Override
	public boolean authenticate(String username, String password, ServerSession session) {
		if(this.username.equals(username) && this.password.equals(password)) {
			return true;
		} else {
			return false;
		}
	}
}
