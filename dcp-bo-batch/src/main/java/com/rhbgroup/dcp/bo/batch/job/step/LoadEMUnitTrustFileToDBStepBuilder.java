package com.rhbgroup.dcp.bo.batch.job.step;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.STATUS_INITIAL;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.EMUnitTrustParameter.TARGET_DATA_SET;

import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;

import com.rhbgroup.dcp.bo.batch.framework.core.BaseStepBuilder;
import com.rhbgroup.dcp.bo.batch.framework.utils.BatchUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadEMUnitTrustJobConfigProperties;
import com.rhbgroup.dcp.bo.batch.job.config.properties.LoadEMUnitTrustJobConfigProperties.UTFile;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.UnitTrustFileHeaderMapper;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.UnitTrustFileTrailerMapper;
import com.rhbgroup.dcp.bo.batch.job.model.JompayEmatchingReportOutDetail;
import com.rhbgroup.dcp.bo.batch.job.model.JompayEmatchingReportPaymentTxn;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrust;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileAbs;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileHeader;
import com.rhbgroup.dcp.bo.batch.job.model.UnitTrustFileTrailer;

public abstract class LoadEMUnitTrustFileToDBStepBuilder extends BaseStepBuilder {
	private static final Logger logger = Logger.getLogger(LoadEMUnitTrustFileToDBStepBuilder.class);

	@Autowired
	LoadEMUnitTrustJobConfigProperties configProperties;
	
	protected String processingDate;
	protected String systemDate;
	protected String systemTime;
	protected int errorCount=0;
	protected int targetDataSet;
	protected long jobExecutionId;
	protected String utFilePath;
	protected String fileName;
	protected int line;
	protected UTFile utFileConfig;

	protected void getStepParam(StepExecution stepExecution, String fileNameFormat) {
		targetDataSet = stepExecution.getJobExecution().getExecutionContext().getInt(TARGET_DATA_SET);
		jobExecutionId = stepExecution.getJobExecution().getId();
		utFileConfig = getUTFileConfig(fileNameFormat);
		utFilePath = utFileConfig.getDownloadFilePath();
		logger.info(String.format("Creating Reader for file %s", utFilePath));
		fileName = new File(utFilePath).getName();
		errorCount=0;
		line=0;
	}
	
	
	protected void processUTHeader(UnitTrustFileHeader header) {
		line++;
		processingDate = header.getProcessingDate();
		systemDate = header.getSystemDate();
		systemTime=header.getSystemTime();
		if(StringUtils.isBlank(processingDate)) {
			logger.error("Header process date cannot be null or empty");
			++errorCount;
		}
		if(StringUtils.isBlank(systemDate)) {
			logger.error("Header system date cannot be null or empty");
			++errorCount;
		}
		if(StringUtils.isBlank(systemDate)) {
			logger.error("Header system time annot be null or empty");
			++errorCount;
		}
	}
	
	protected void setCommonUTField(UnitTrust utRecord) throws ParseException {
		utRecord.setJobExecutionId(jobExecutionId);
		utRecord.setProcessDate(processingDate);
		utRecord.setStatus(STATUS_INITIAL);
		utRecord.setBatchExtractionTime(getBatchExtractionTime());
		utRecord.setCreatedBy(configProperties.getBatchCode());
		utRecord.setCreatedTime(new Date());
		utRecord.setUpdatedBy(configProperties.getBatchCode());
		utRecord.setUpdatedTime(new Date());
		utRecord.setFileName(fileName);
	}
	protected Date getBatchExtractionTime() throws ParseException {
		return DateUtils.getDateFromString(systemDate.concat(systemTime), "yyyyMMddHHmmss");
	}
	
	protected FlatFileItemReader<UnitTrustFileAbs>  getReader(UTFile utFileConfig, FieldSetMapper<UnitTrustFileAbs>  detailMapper){
		LineTokenizer headerTokenizer = BatchUtils.getDelimiterTokenizer(utFileConfig.getHeaderNames(), configProperties.getDelimiter() );
		LineTokenizer detailTokenizer = BatchUtils.getDelimiterTokenizer(utFileConfig.getDetailNames(), configProperties.getDelimiter() );
		LineTokenizer trailerTokenizer = BatchUtils.getDelimiterTokenizer(utFileConfig.getTrailerNames(), configProperties.getDelimiter());
		logger.info(String.format("Created LineTokenizer for file [%s]", utFileConfig.getName()));
		
		String headerPrefixPattern  = utFileConfig.getHeaderPrefix() + "*";
		String detailPrefixPattern  = utFileConfig.getDetailPrefix() + "*";
		String trailerPrefixPattern  = utFileConfig.getTrailerPrefix() + "*";
		logger.info(String.format("Created Pattern for file [%s]", utFileConfig.getName()));

		Map<String, LineTokenizer> tokenizerMap = new HashMap<>();
		tokenizerMap.put(headerPrefixPattern, headerTokenizer);
		tokenizerMap.put(detailPrefixPattern, detailTokenizer);
		tokenizerMap.put(trailerPrefixPattern, trailerTokenizer);
		logger.info(String.format("Created Tokenizer for file [%s]", utFileConfig.getName()));

		FieldSetMapper<UnitTrustFileAbs> headerMapper = new UnitTrustFileHeaderMapper();
		FieldSetMapper<UnitTrustFileAbs> trailerMapper = new UnitTrustFileTrailerMapper();
		logger.info(String.format("Created Mapper for file [%s]", utFileConfig.getName()));

		Map<String, FieldSetMapper<UnitTrustFileAbs>> fieldSetMapperMap = new HashMap<>();
		fieldSetMapperMap.put(headerPrefixPattern, headerMapper);
		fieldSetMapperMap.put(detailPrefixPattern, detailMapper);
		fieldSetMapperMap.put(trailerPrefixPattern, trailerMapper);
		
		PatternMatchingCompositeLineMapper<UnitTrustFileAbs> lineMapper = new PatternMatchingCompositeLineMapper<>();
		lineMapper.setTokenizers(tokenizerMap);
		lineMapper.setFieldSetMappers(fieldSetMapperMap);
		
		FlatFileItemReader<UnitTrustFileAbs> reader = new FlatFileItemReader<>();
		reader.setResource(new FileSystemResource(utFileConfig.getDownloadFilePath()));
		reader.setLineMapper(lineMapper);
		logger.info(String.format("Created File Item Reader for file [%s]", utFileConfig.getName()));

		return reader;
	}

	protected UTFile getUTFileConfig(String fileFmtName) {
		for(UTFile utFile:configProperties.getUtFiles()) {
			if(utFile.getName().contains(fileFmtName)) {
				return utFile;
			}
		}
		return null;
	}
}
