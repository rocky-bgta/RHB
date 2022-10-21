package com.rhbgroup.dcp.bo.batch.test.utils;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.job.fieldsetmapper.LoadCardlinkNotificationHeaderFieldSetMapper;
import com.rhbgroup.dcp.bo.batch.job.model.LoadCardlinkNotification;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfig.class})
@ActiveProfiles("test")
public class BatchUtilsTest extends BaseJobTest {

	private static final Logger logger = Logger.getLogger(BatchUtilsTest.class);

    @MockBean
    private Configuration config;

    @MockBean
    private EmailTemplate emailTemplate;

	//@Test
	public void tesGetFixedLineTokenizer() throws FileNotFoundException  {
		// Create LineTokenizer
		String fileContentColumns = "1-2,3-10,11-18,19-24,25-29,30-32";
		String fileContentNames = "recordIndicator,processingDate,systemDate,systemTime,eventCode,keyType";
		
		List<Range> columns=new ArrayList<>();
        
        for(String column : fileContentColumns.split(",",-1)){
            String[] rangeValue=column.split("-",-1);
            columns.add(new Range(Integer.parseInt(rangeValue[0]),Integer.parseInt(rangeValue[1])));
        }
        
        Range[] columnsArr = new Range[columns.size()];
        columnsArr = columns.toArray(columnsArr);
        String[] names=fileContentNames.split(",", -1);
        
        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setColumns(columnsArr);
        tokenizer.setNames(names);
        
        logger.info("Created LineTokenizer");
        
        // Created tokenizer Map
        String headerPrefixPattern = "^DH([0-9]{27})([a-z,A-Z]{2})";
        
        Map<String, LineTokenizer> tokenizerMap = new HashMap<>();
		tokenizerMap.put(headerPrefixPattern, tokenizer);
		
		logger.info("Created tokenizer Map");
		
		// Created mapper header
		FieldSetMapper<LoadCardlinkNotification> headerMapper = new LoadCardlinkNotificationHeaderFieldSetMapper();
		
		Map<String, FieldSetMapper<LoadCardlinkNotification>> fieldSetMapperMap = new HashMap<>();
		fieldSetMapperMap.put(headerPrefixPattern, headerMapper);
		
		PatternMatchingCompositeLineMapper<LoadCardlinkNotification> lineMapper = new PatternMatchingCompositeLineMapper<>();
		lineMapper.setTokenizers(tokenizerMap);
		lineMapper.setFieldSetMappers(fieldSetMapperMap);
		
		logger.info("Created mapper header");
		
		// Set input file
        FlatFileItemReader<LoadCardlinkNotification> reader = new FlatFileItemReader<>();
        String filePath = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20190108.txt";
        File file = getResourceFile(filePath);
		
		logger.info("Set input file completed");
		
		// Create the reader that hold the linemapper and also target with the input file
        reader.setResource(new FileSystemResource(file));
		reader.setLineMapper(lineMapper);
        
		logger.info("Created the reader that hold the linemapper and also target with the input file");
	}
	
	@Test
	public void tesGetOpenLineTokenizer() throws FileNotFoundException  {
		// Create LineTokenizer
		String fileContentColumns = "1-2,3-10,11-18,19-24,25-29,30-32";
		String fileContentNames = "recordIndicator,processingDate,systemDate,systemTime,eventCode,keyType";
		
		
        String[] contentColumns = fileContentColumns.split(",",-1);
        int contentColumnsSize = contentColumns.length;
        int count = 1;
        
        List<Range> columns=new ArrayList<>();
		for(String column : contentColumns){
            String[] rangeValue=column.split("-",-1);
            if (count < contentColumnsSize) {
            	columns.add(new Range(Integer.parseInt(rangeValue[0]),Integer.parseInt(rangeValue[1])));
            } else {
            	columns.add(new Range(Integer.parseInt(rangeValue[0])));
            }
            count++;
        }
        
        Range[] columnsArr = new Range[columns.size()];
        columnsArr = columns.toArray(columnsArr);
        String[] names=fileContentNames.split(",", -1);
        
        FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setColumns(columnsArr);
        tokenizer.setNames(names);
        
        logger.info("Created LineTokenizer");
        
        // Created tokenizer Map
        String headerPrefixPattern = "DH*";
        
        Map<String, LineTokenizer> tokenizerMap = new HashMap<>();
		tokenizerMap.put(headerPrefixPattern, tokenizer);
		
		logger.info("Created tokenizer Map");
		
		// Created mapper header
		FieldSetMapper<LoadCardlinkNotification> headerMapper = new LoadCardlinkNotificationHeaderFieldSetMapper();
		
		Map<String, FieldSetMapper<LoadCardlinkNotification>> fieldSetMapperMap = new HashMap<>();
		fieldSetMapperMap.put(headerPrefixPattern, headerMapper);
		
		PatternMatchingCompositeLineMapper<LoadCardlinkNotification> lineMapper = new PatternMatchingCompositeLineMapper<>();
		lineMapper.setTokenizers(tokenizerMap);
		lineMapper.setFieldSetMappers(fieldSetMapperMap);
		
		logger.info("Created mapper header");
		
		// Set input file
        FlatFileItemReader<LoadCardlinkNotification> reader = new FlatFileItemReader<>();
        String filePath = "ftp/uat_dcp_cardlink_notification_from/DCP_LDCPD6002T_20190108.txt";
        File file = getResourceFile(filePath);
		
		logger.info("Set input file completed");
		
		// Create the reader that hold the linemapper and also target with the input file
        reader.setResource(new FileSystemResource(file));
		reader.setLineMapper(lineMapper);
        
		logger.info("Created the reader that hold the linemapper and also target with the input file");
	}
}
