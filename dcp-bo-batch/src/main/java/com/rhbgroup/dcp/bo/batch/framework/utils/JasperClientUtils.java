package com.rhbgroup.dcp.bo.batch.framework.utils;

import java.io.InputStream;

import com.jaspersoft.jasperserver.dto.serverinfo.ServerInfo;
import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.reporting.RunReportAdapter;
import com.jaspersoft.jasperserver.jaxrs.client.core.JasperserverRestClient;
import com.jaspersoft.jasperserver.jaxrs.client.core.RestClientConfiguration;
import com.jaspersoft.jasperserver.jaxrs.client.core.Session;
import com.jaspersoft.jasperserver.jaxrs.client.core.operationresult.OperationResult;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.jasper.JasperClientConfigProperties;
import com.rhbgroup.dcp.bo.batch.framework.jasper.JasperReportConfig.ReportConfig;
import com.rhbgroup.dcp.bo.batch.framework.jasper.JasperReportConfig.ReportConfig.Parameter;

public final class JasperClientUtils {

	private JasperClientUtils() {
		throw new IllegalStateException("Utility Class");
	}
	
	public static Session initSession(JasperClientConfigProperties configProp) throws BatchException {
		RestClientConfiguration clientConfig;
		JasperserverRestClient client;
		Session session = null;
		try {
			clientConfig = RestClientConfiguration.loadConfiguration(configProp.toProperties());
			client = new JasperserverRestClient(clientConfig);
			session = client.authenticate(configProp.getUsername(), configProp.getPassword());
		} catch (Exception ex) {
			throw new BatchException(BatchErrorCode.JASPER_CLIENT_ERROR, "Unable to initate Jasper Server session", ex);
		}
		return session;
	}
	
	public static ServerInfo getServerInfo(Session session) {
		OperationResult<ServerInfo> result = session.serverInfoService().details();
		return result.getEntity();
	}

	public static InputStream runReport(Session session, ReportConfig reportConfig, String format) throws BatchException{
		try {
			RunReportAdapter runReportAdapter =  session
					.reportingService()
					.report(reportConfig.getUri())
					.prepareForRun(reportConfig.getJasperReportOutputFormat(format).reportOutputFormat);
			for(Parameter param : reportConfig.getParameters()) {
				runReportAdapter.parameter(param.getName(),param.getValue());
			}
			OperationResult<InputStream> result = runReportAdapter.run();
			return result.getEntity();
		}catch(Exception ex) {
			throw new BatchException(BatchErrorCode.JASPER_CLIENT_ERROR, "Error during run report execution", ex);
		}
	}
}
