package com.rhbgroup.dcp.bo.batch.framework.jasper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;

import lombok.Getter;
import lombok.Setter;


@Lazy
@Configuration
@ConfigurationProperties(prefix="jasper.config")
@Setter
@Getter
public class JasperReportConfig {

	private final static String FORMAT_SEPERATOR = ",";
	private List<ReportConfig> reports = new ArrayList<>();
	
	@Setter
	@Getter
	public static class ReportConfig{
		private String id;
		private String uri;
		private String format;
		private String filename;
		private String targetpath;
		private List<Parameter> parameters = new ArrayList<>();
		
		@Setter
		@Getter
		public static class Parameter{
			private String name;
			private String value;
		}

		public String[] getFileFormats(){
            String [] formats;
            boolean isCommaSeperatedFormat=	this.format.contains(FORMAT_SEPERATOR);
            if (isCommaSeperatedFormat == true)
                formats = format.split(FORMAT_SEPERATOR);
            else {
                formats = new String[1];
                formats[0] = this.format;
            }
			return formats;
		}

		public JasperReportOutputFormat getJasperReportOutputFormat(String format) {

			return JasperReportOutputFormat.fromString(format);
		}

		public JasperReportOutputFormat getJasperReportOutputFormat() {

			return JasperReportOutputFormat.fromString(getFileFormats()[0]);
		}

		public String getDefaultFileName() {
			return new StringBuffer()
					   .append(this.id)
					   .append("_")
					   .append(DateUtils.formatDateString(new Date(), "yyyyMMddHHmmss"))
					   .append(this.getJasperReportOutputFormat().fileExtension)
					   .toString();
		}
		
		public void addParameter(String name, String value) {
			Parameter parameter = new Parameter();
			parameter.setName(name);
			parameter.setValue(value);
			this.parameters.add(parameter);
		}
	}
	
	public ReportConfig getReportConfigById(String id) {
		for(ReportConfig reportConfig : this.reports) {
			if(reportConfig.id.equalsIgnoreCase(id)) {
				return reportConfig;
			}
		}
		return null;
	}
}
