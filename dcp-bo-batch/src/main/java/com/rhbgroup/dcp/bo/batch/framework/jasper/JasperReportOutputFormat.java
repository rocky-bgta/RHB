package com.rhbgroup.dcp.bo.batch.framework.jasper;

import com.jaspersoft.jasperserver.jaxrs.client.apiadapters.reporting.ReportOutputFormat;

public enum JasperReportOutputFormat {
	PDF(ReportOutputFormat.PDF, ".pdf"),
	HTML(ReportOutputFormat.HTML, ".html"),
	XLS(ReportOutputFormat.XLS, ".xls"),
	XLSX(ReportOutputFormat.XLSX, ".xlsx"),
	DOCX(ReportOutputFormat.DOCX, ".docx"),
	XML(ReportOutputFormat.XML, ".xml");
	
	public final ReportOutputFormat reportOutputFormat;
	public final String fileExtension;
	
	private JasperReportOutputFormat(ReportOutputFormat reportOutputFormat, String fileExtension){
		this.reportOutputFormat = reportOutputFormat;
		this.fileExtension = fileExtension;
	}
	
	public static final JasperReportOutputFormat fromString(String format) {
		return Enum.valueOf(JasperReportOutputFormat.class, format);
	}
}
