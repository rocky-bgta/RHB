package com.rhbgroup.dcp.bo.batch.framework.utils;

import com.rhbgroup.dcp.bo.batch.framework.enums.DateRange;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.test.MetaDataInstanceFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;

import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.BatchJobParameter.*;
import static com.rhbgroup.dcp.bo.batch.framework.constants.BatchSystemConstant.General.DEFAULT_DATE_FORMAT;
import static org.jgroups.util.Util.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateUtilsTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<DateUtils> constructor = DateUtils.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);

		ExpectedException expectedException = ExpectedException.none();

		expectedException.expect(InvocationTargetException.class);

		try {
			constructor.newInstance();
		}catch(InvocationTargetException itx){
			assertNotNull(itx.getCause());
		}
	}

	@Test
	public void testDateUtils()  {
		LocalDateTime fromDateTime = LocalDateTime.of(2018, 9, 20, 01, 01, 01);
		LocalDateTime toDateTime = LocalDateTime.of(2018, 9, 24, 01, 01, 01);
		DateRange dr = DateRange.WEEKLY;

		assertNotNull(DateUtils.isValidFromToDateTimes(fromDateTime, toDateTime, dr));
	}

	@Test
	public void testDateUtilsfromDateTimeisAfterToDateTime()  {
		LocalDateTime fromDateTime = LocalDateTime.of(2018, 9, 25, 01, 01, 01);
		LocalDateTime toDateTime = LocalDateTime.of(2018, 9, 24, 01, 01, 01);
		DateRange dr = DateRange.WEEKLY;

		assertEquals(false, DateUtils.isValidFromToDateTimes(fromDateTime, toDateTime, dr));
	}

	@Test
	public void testDateUtilsFromDateIsNotMonday()  {
		LocalDateTime fromDateTime = LocalDateTime.of(2018, 9, 20, 01, 01, 01);
		LocalDateTime toDateTime = LocalDateTime.of(2018, 9, 24, 01, 01, 01);
		DateRange dr = DateRange.WEEKLY;

		assertEquals(false, DateUtils.isValidFromToDateTimes(fromDateTime, toDateTime, dr));
	}

	@Test
	public void testDateUtilsDateRangeIsMonthly()  {
		LocalDateTime fromDateTime = LocalDateTime.of(2018, 9, 20, 01, 01, 01);
		LocalDateTime toDateTime = LocalDateTime.of(2018, 9, 24, 01, 01, 01);
		DateRange dr = DateRange.MONTHLY;

		assertEquals(true, DateUtils.isValidFromToDateTimes(fromDateTime, toDateTime, dr));
	}

	@Test
	public void testDateUtilsIsNotMonday()  {
		LocalDateTime fromDateTime = LocalDateTime.of(2018, 9, 24, 01, 01, 01);
		LocalDateTime toDateTime = LocalDateTime.of(2018, 9, 25, 01, 01, 01);
		DateRange dr = DateRange.WEEKLY;

		assertEquals(false, DateUtils.isValidFromToDateTimes(fromDateTime, toDateTime, dr));
	}

	@Test
	public void testDateUtilsIsNotSunday()  {
		LocalDateTime fromDateTime = LocalDateTime.of(2018, 9, 23, 01, 01, 01);
		LocalDateTime toDateTime = LocalDateTime.of(2018, 9, 25, 01, 01, 01);
		DateRange dr = DateRange.WEEKLY;

		assertEquals(false, DateUtils.isValidFromToDateTimes(fromDateTime, toDateTime, dr));
	}

	@Test
	public void testDateUtilsNotSevenDays()  {
		LocalDateTime fromDateTime = LocalDateTime.of(2018, 9, 10, 01, 01, 01);
		LocalDateTime toDateTime = LocalDateTime.of(2018, 9, 23, 01, 01, 01);
		DateRange dr = DateRange.WEEKLY;

		assertEquals(false, DateUtils.isValidFromToDateTimes(fromDateTime, toDateTime, dr));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDateUtilsJobParameterFromToDateTimes()  {
		String fromToDateStr = "2019-300-239123";

		assertNotNull(DateUtils.getJobParameterFromToDateTimes(fromToDateStr));
	}

	@Test
	public void testDateUtilsAdd()  {
		Date date = new Date();
		int amountToAdd = 1;

		assertNotNull(DateUtils.add(date, amountToAdd, ChronoUnit.DAYS));
	}

	@Test
	public void testGetBatchProcessingDateWithExternalDateParameter() throws BatchException, ParseException {

		//Prepare mock data
		StepExecution stepExecution = Mockito.mock(StepExecution.class);
		StepContext stepContext = Mockito.mock(StepContext.class);
		ChunkContext chunkContext = Mockito.mock(ChunkContext.class);
		JobExecution jobExecution = Mockito.mock(JobExecution.class);
		JobParameters jobParameters = new JobParametersBuilder()
				.addString(BATCH_JOB_PARAMETER_EXTERNAL_SYSTEM_DATE, "2020-11-11")
				.toJobParameters();
		Mockito.when(chunkContext.getStepContext()).thenReturn(stepContext);
		Mockito.when(stepContext.getStepExecution()).thenReturn(stepExecution);
		Mockito.when(stepExecution.getJobExecution()).thenReturn(jobExecution);
		Mockito.when(jobExecution.getJobParameters()).thenReturn(jobParameters);

		assertEquals(DateUtils.getDateFromString("2020-11-11", DEFAULT_DATE_FORMAT), DateUtils.getBatchProcessingDate(chunkContext));
	}

	@Test
	public void testGetBatchProcessingDateWithDBDateParameter() throws BatchException, ParseException {

		//Prepare mock data
		StepExecution stepExecution = Mockito.mock(StepExecution.class);
		StepContext stepContext = Mockito.mock(StepContext.class);
		ChunkContext chunkContext = Mockito.mock(ChunkContext.class);
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.getExecutionContext().putString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2020-12-12");

		Mockito.when(chunkContext.getStepContext()).thenReturn(stepContext);
		Mockito.when(stepContext.getStepExecution()).thenReturn(stepExecution);
		Mockito.when(stepExecution.getJobExecution()).thenReturn(jobExecution);

		assertEquals(DateUtils.getDateFromString("2020-12-11", DEFAULT_DATE_FORMAT), DateUtils.getBatchProcessingDate(chunkContext));
	}

	@Test
	public void testGetBatchProcessingDateWithOffsetParameter() throws BatchException, ParseException {

		//Prepare mock data
		StepExecution stepExecution = Mockito.mock(StepExecution.class);
		StepContext stepContext = Mockito.mock(StepContext.class);
		ChunkContext chunkContext = Mockito.mock(ChunkContext.class);
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();
		jobExecution.getExecutionContext().putString(BATCH_JOB_PARAMETER_DB_BATCH_SYSTEM_DATE_KEY, "2020-12-12");

		HashMap<String,Object> jobParameters = new HashMap<>();
		jobParameters.put(BATCH_JOB_PARAMETER_JOB_BATCH_OFFSET_DAY_KEY, "-10");

		Mockito.when(chunkContext.getStepContext()).thenReturn(stepContext);
		Mockito.when(stepContext.getJobParameters()).thenReturn(jobParameters);
		Mockito.when(stepContext.getStepExecution()).thenReturn(stepExecution);
		Mockito.when(stepExecution.getJobExecution()).thenReturn(jobExecution);

		assertEquals(DateUtils.getDateFromString("2020-12-02", DEFAULT_DATE_FORMAT), DateUtils.getBatchProcessingDate(chunkContext));
	}

	@Test(expected = BatchException.class)
	public void testGetBatchProcessingDateWithException() throws BatchException, ParseException {

		//Prepare mock data
		StepExecution stepExecution = Mockito.mock(StepExecution.class);
		StepContext stepContext = Mockito.mock(StepContext.class);
		ChunkContext chunkContext = Mockito.mock(ChunkContext.class);
		JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution();

		Mockito.when(chunkContext.getStepContext()).thenReturn(stepContext);
		Mockito.when(stepContext.getStepExecution()).thenReturn(stepExecution);
		Mockito.when(stepExecution.getJobExecution()).thenReturn(jobExecution);
		DateUtils.getBatchProcessingDate(chunkContext);
	}
}
