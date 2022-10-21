package com.rhbgroup.dcp.bo.batch.framework.utils;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.*;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_TIME_FORMAT;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.batch.core.scope.context.ChunkContext;

import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General;
import com.rhbgroup.dcp.bo.batch.framework.enums.DateRange;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;

public final class DateUtils {

	private static final Logger logger = Logger.getLogger(DateUtils.class);

	private DateUtils() {
		throw new IllegalStateException("Utility Class");
	}

	public static final Date getDateFromString(String dateStr, String dateFormat) throws ParseException {
		return new SimpleDateFormat(dateFormat).parse(dateStr);
	}

	public static final LocalDate getLocalDateFromString(String dateStr, String dateFormat) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
		return LocalDate.parse(dateStr, formatter);
	}

	public static String getFormattedCurrentDateTimeString() {
		return getFormattedDateTimeString(new Date());
	}

	public static String getFormattedDateTimeString(Date date) {
		return formatDateString(date, DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT);
	}

	public static String formatDateString(Date date, String format) {
		SimpleDateFormat sdfDate = new SimpleDateFormat(format);
		return sdfDate.format(date);
	}

	public static String convertDateFormat(String dateStr, String format, String newFormat) throws ParseException {
		Date date = getDateFromString(dateStr, format);
		return formatDateString(date, newFormat);
	}

	public static Date addDays(Date date, int days) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}

	public static Date addMonths(Date date, int months) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		cal.add(Calendar.MONTH, months);
		return cal.getTime();
	}

	public static Date add(Date date, int amountToAdd, TemporalUnit unit) {
		LocalDateTime localDateTime = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault())
				.toLocalDateTime();
		localDateTime = localDateTime.plus(amountToAdd, unit);
		Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	public static LocalDateTime[] getJobParameterFromToDateTimes(String fromToDateStr) {
		// Validate to ensure the regex matched
		// Expected Format: (yyyyMmdd,yyyyMMdd), there is no space between
		if (!fromToDateStr.matches(General.DEFAULT_JOB_PARAMETER_FROMDATE_TODATE_REGEX)) {
			throw new IllegalArgumentException(String.format("[%s] not matched the required regex [%s]", fromToDateStr,
					General.DEFAULT_JOB_PARAMETER_FROMDATE_TODATE_REGEX));
		}

		String[] dateStrs = fromToDateStr.replace("(", "").replace(")", "").split(",");
		String fromDateStr = dateStrs[0];
		String toDateStr = dateStrs[1];

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(General.DEFAULT_JOB_PARAMETER_DATE_FORMAT);
		LocalDate fromDate = LocalDate.parse(fromDateStr, formatter);
		LocalDate toDate = LocalDate.parse(toDateStr, formatter);
		// Set the time to be beginning of the day
		LocalDateTime fromDateTime = fromDate.atTime(0, 0, 0, 0);
		// Adjust the time close to end of day
		LocalDateTime toDateTime = toDate.plusDays(1).atTime(0, 0, 0, 0).minusNanos(1);

		LocalDateTime[] fromToDateTimes = new LocalDateTime[2];
		fromToDateTimes[0] = fromDateTime;
		fromToDateTimes[1] = toDateTime;

		return fromToDateTimes;
	}

	public static boolean isValidFromToDateTimes(LocalDateTime fromDateTime, LocalDateTime toDateTime,
			DateRange dateRange) {
		// Ensure the From Date must be before To Date
		if (fromDateTime.isAfter(toDateTime)) {
			logger.error(String.format("From Date [%s] is after To Date [%s]", fromDateTime, toDateTime));
			return false;
		}

		if (dateRange.equals(DateRange.WEEKLY)) {
			// Ensure the From Date is MONDAY
			if (!fromDateTime.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
				logger.error(String.format("From Date [%s] is not a valid MONDAY", fromDateTime));
				return false;
			}

			// Ensure to To Date is SUNDAY
			if (!toDateTime.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
				logger.error(String.format("To Date [%s] is not a valid SUNDAY", toDateTime));
				return false;
			}

			// Ensure the duration between From Date and To Date is exactly one week
			long daysDifference = Duration.between(fromDateTime, toDateTime.plusSeconds(1)).toDays();
			if (daysDifference != 7) {
				logger.error(String.format("The duration between [%s] and [%s] is [%d] instead of 7 days", fromDateTime,
						toDateTime, daysDifference));
				return false;
			}
		}

		return true;
	}

	public static String getProcessDate(ChunkContext chunkContext) throws ParseException, BatchException {
		String datetobeProcess = null;
		String externalDate = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()
				.getString(BATCH_JOB_PARAMETER_EXTERNAL_SYSTEM_DATE);
		Date processDate = null;
		if (externalDate != null) {
			processDate = new SimpleDateFormat(DEFAULT_DATE_FORMAT).parse(externalDate);
			datetobeProcess = BatchUtils.getFormatedDateEV(processDate);
			logger.info("EV date is  :: " + datetobeProcess);
		}

		if (processDate == null) {
			String batchSystemDateStr = (String) chunkContext.getStepContext().getStepExecution().getJobExecution()
					.getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			if (batchSystemDateStr == null) {
				throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, "Missing mandatory batch parameter");
			}
			Date batchSystemDate = DateUtils.getDateFromString(batchSystemDateStr, DEFAULT_DATE_FORMAT);
			Date batchProcessDate = null;

			// Process Offset Date from job param
			if (chunkContext.getStepContext().getJobParameters()
					.containsKey(BATCH_JOB_PARAMETER_JOB_BATCH_OFFSET_DAY_KEY)) {
				int offsetDay = Integer.parseInt(chunkContext.getStepContext().getJobParameters()
						.get(BATCH_JOB_PARAMETER_JOB_BATCH_OFFSET_DAY_KEY).toString());
				batchProcessDate = DateUtils.addDays(batchSystemDate, offsetDay);
			} else {
				batchProcessDate = BatchUtils.getProcessingDate(batchSystemDateStr, DEFAULT_DATE_FORMAT);
			}
			String date = DateUtils.formatDateString(batchProcessDate, DEFAULT_DATE_FORMAT);
			datetobeProcess = BatchUtils.getFormatedDateDB(date);
			logger.info("DB date iss :: " + datetobeProcess);
		}
		// 21401360014612

		return datetobeProcess;
	}

	public static String getFormattedDate(String date2) {

		SimpleDateFormat format1 = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		SimpleDateFormat format2 = new SimpleDateFormat("ddMMyyyy");
		Date date = null;
		try {
			date = format2.parse(date2);
		} catch (ParseException e) {
			logger.error(e);
		}
		return format1.format(date);
	}

	public static String gettemplateDate(String date2) {

		SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		SimpleDateFormat format2 = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		Date date = null;
		try {
			date = format2.parse(date2);
		} catch (ParseException e) {
			logger.error(e);
		}
		return format1.format(date);
	}

	public static String getBatchCurrentDate() {
		Timestamp date = new Timestamp(System.currentTimeMillis());
		return new SimpleDateFormat("dd/MM/yyyy hh:mm aa").format(date);

	}
	
	public static String getPostingFileCurrentDate() {
		SimpleDateFormat format2 = new SimpleDateFormat("yyyyMMdd");
		Timestamp now = new Timestamp(System.currentTimeMillis());
		return format2.format(now);

	}
	
	public static String getPostingFileEVDate(String date) {
		
		SimpleDateFormat format1 = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
		SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy");//09/06/2020
		Date date4 = null;
		try {
			date4 = format1.parse(date);
		} catch (ParseException e) {
			logger.error(e);
		}
		return format2.format(date4);

	}

	/**
	 * This function is to check whether the date is retrieve from external date or DB date
	 * Additional, it also handle the offset date if using DB date
	 * In future, please point others class to this common date util
	 *
	 * @param chunkContext Spring chunkContext
	 * @return Date
	 * @throws BatchException Date not found
	 * @throws ParseException Date parcing error
	 */
	public static Date getBatchProcessingDate(ChunkContext chunkContext) throws BatchException, ParseException {
		Date batchProcessDate;
		String externalDateStr = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters()
				.getString(BATCH_JOB_PARAMETER_EXTERNAL_SYSTEM_DATE);
		logger.info("getBatchProcessingDate - externalDateStr : " + externalDateStr);

		// Check external date whether want to process by DB date or environment parameter
		if(externalDateStr == null || externalDateStr.isEmpty()) {

			// is DB date available?
			String batchSystemDateStr = (String) chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY);
			logger.info("getBatchProcessingDate - batchSystemDateStr : " + batchSystemDateStr);
			if (batchSystemDateStr == null) {
				throw new BatchException(BatchErrorCode.GENERIC_SYSTEM_ERROR, "Missing mandatory batch parameter");
			}
			Date batchSystemDate = getDateFromString(batchSystemDateStr, DEFAULT_DATE_FORMAT);

			// Process Offset Date from job param
			if (chunkContext.getStepContext().getJobParameters().containsKey(BATCH_JOB_PARAMETER_JOB_BATCH_OFFSET_DAY_KEY)) {
				int offsetDay = Integer.parseInt(chunkContext.getStepContext().getJobParameters().get(BATCH_JOB_PARAMETER_JOB_BATCH_OFFSET_DAY_KEY).toString());
				batchProcessDate = addDays(batchSystemDate, offsetDay);
			} else {
				batchProcessDate = BatchUtils.getProcessingDate(batchSystemDateStr, DEFAULT_DATE_FORMAT);
			}

		} else {
			batchProcessDate = getDateFromString(externalDateStr, DEFAULT_DATE_FORMAT);
		}

		logger.info("getBatchProcessingDate - batchProcessDate : " + batchProcessDate);
		return batchProcessDate;
	}
}
