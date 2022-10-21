package com.rhbgroup.dcp.bo.batch.framework.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class JMSConfigProperties {
	protected String host;
	protected int port;
	protected String username;
	protected String password;
}
