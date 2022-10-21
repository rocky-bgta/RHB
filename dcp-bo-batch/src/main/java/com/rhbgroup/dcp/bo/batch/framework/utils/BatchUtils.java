package com.rhbgroup.dcp.bo.batch.framework.utils;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.Statistics.COMMIT_COUNT_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.Statistics.READ_COUNT_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.Statistics.SKIP_COUNT_KEY;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.Statistics.WRITE_COUNT_KEY;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.rhbgroup.dcp.bo.batch.framework.core.DcpBatchApplicationContext;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Autowired;

public final class BatchUtils {
	
	private BatchUtils() {
		throw new IllegalStateException("Utility class");
	}

	@Autowired
	private static DcpBatchApplicationContext dcpBatchApplicationContext;

    public static synchronized void addReadCount(int readCount, JobExecution jobExecution) {
        int existingReadCount=0;
        if(jobExecution.getExecutionContext().containsKey(READ_COUNT_KEY))
        {
            existingReadCount=jobExecution.getExecutionContext().getInt(READ_COUNT_KEY);
        }
        existingReadCount=existingReadCount+readCount;
        jobExecution.getExecutionContext().putInt(READ_COUNT_KEY,existingReadCount);
    }

    public static synchronized void addWriteCount(int writeCount, JobExecution jobExecution) {
        int existingWriteCount=0;
        if(jobExecution.getExecutionContext().containsKey(WRITE_COUNT_KEY))
        {
            existingWriteCount=jobExecution.getExecutionContext().getInt(WRITE_COUNT_KEY);
        }
        existingWriteCount=existingWriteCount+writeCount;
        jobExecution.getExecutionContext().putInt(WRITE_COUNT_KEY,existingWriteCount);
    }

    public static synchronized void addSkipCount(int skipCount, JobExecution jobExecution) {
        int existingSkipCount=0;
        if(jobExecution.getExecutionContext().containsKey(SKIP_COUNT_KEY))
        {
            existingSkipCount=jobExecution.getExecutionContext().getInt(SKIP_COUNT_KEY);
        }
        existingSkipCount=existingSkipCount+skipCount;
        jobExecution.getExecutionContext().putInt(SKIP_COUNT_KEY,existingSkipCount);
    }

    public static synchronized void addCommitCount(int commitCount, JobExecution jobExecution) {
        int existingCommitCount=0;
        if(jobExecution.getExecutionContext().containsKey(COMMIT_COUNT_KEY))
        {
            existingCommitCount=jobExecution.getExecutionContext().getInt(COMMIT_COUNT_KEY);
        }
        existingCommitCount=existingCommitCount+commitCount;
        jobExecution.getExecutionContext().putInt(COMMIT_COUNT_KEY,existingCommitCount);
    }

    public static String getSummaryStatistics(JobExecution jobExecution)
    {
        int existingReadCount=0;
        int existingWriteCount=0;
        int existingSkipCount=0;
        int existingCommitCount=0;
        if(jobExecution.getExecutionContext().containsKey(READ_COUNT_KEY))
        {
            existingReadCount=jobExecution.getExecutionContext().getInt(READ_COUNT_KEY);
        }
        if(jobExecution.getExecutionContext().containsKey(WRITE_COUNT_KEY))
        {
            existingWriteCount=jobExecution.getExecutionContext().getInt(WRITE_COUNT_KEY);
        }
        if(jobExecution.getExecutionContext().containsKey(SKIP_COUNT_KEY))
        {
            existingSkipCount=jobExecution.getExecutionContext().getInt(SKIP_COUNT_KEY);
        }

        if(jobExecution.getExecutionContext().containsKey(COMMIT_COUNT_KEY))
        {
            existingCommitCount=jobExecution.getExecutionContext().getInt(COMMIT_COUNT_KEY);
        }

        return String.format("Read Count:%d, Write Count:%d, Skip Count: %d, Commit Count: %d"
                ,existingReadCount
                ,existingWriteCount
                ,existingSkipCount
                ,existingCommitCount);
    }
    
    public static LineTokenizer getFixedLengthTokenizer(String fileContentNames, String fileContentColumns) {
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
        
        return tokenizer;
    }
    
    public static LineTokenizer getFixedLengthWithOpenRangeTokenizer(String fileContentNames, String fileContentColumns) {
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
        
        return tokenizer;
    }
    
	public static LineTokenizer getDelimiterTokenizer(String fileContentNames, String delimiter) {
		String[] names = fileContentNames.split(",", -1);
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setDelimiter(delimiter);
		tokenizer.setNames(names);
		return tokenizer;
	}
	
    public static String generateSourceFileName(String fileNameFormat, Date date) {
    	String sourceFilename = null;
    	
    	int beginIndex = fileNameFormat.indexOf("${") + 2;
		int endIndex = fileNameFormat.indexOf('}');
		String dateFormat = fileNameFormat.substring(beginIndex, endIndex);
		String dateStr = new SimpleDateFormat(dateFormat).format(date);
		sourceFilename = fileNameFormat.replaceAll("\\$\\{.*\\}", dateStr);
    	
    	return sourceFilename;
    }

    public static Date getProcessingDate(String batchSystemDate, String dateFormat) throws ParseException {
        Map<String, String> jobParameters=null;
        if(dcpBatchApplicationContext!=null) {
            jobParameters = dcpBatchApplicationContext.getInitialJobArguments();
        }
        if(jobParameters!=null && jobParameters.containsKey(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY))
            return (DateUtils.getDateFromString(jobParameters.get(BATCH_JOB_PARAMETER_JOB_BATCH_PROCESS_DATE_KEY), dateFormat));
        else
        if (null != batchSystemDate) {
            return DateUtils.addDays(DateUtils.getDateFromString(batchSystemDate, dateFormat), -1);
        } else {
            return DateUtils.addDays(new Date(), -1);
        }
    }
    
	public static double roundDecimal(double value, int places) {
		BigDecimal bd = new BigDecimal(Double.toString(value));
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	public static String getFormatedDateEV(Date dte) throws ParseException{
		String pattern = "E MMM dd HH:mm:ss z yyyy";
		DateFormat df = new SimpleDateFormat(pattern);     
		String todayAsString = df.format(dte);
		DateFormat originalFormat = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
		DateFormat targetFormat = new SimpleDateFormat("ddMMyyyy");
		Date date = originalFormat.parse(todayAsString);
		return targetFormat.format(date);
	}
	
	public static String getFormatedDateDB(String dte) throws ParseException{
		DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat targetFormat = new SimpleDateFormat("ddMMyyyy");
		Date date = originalFormat.parse(dte);
		return targetFormat.format(date);
	}
}
