package com.rhbgroup.dcp.bo.batch.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.utils.BeanUtils;
import com.rhbgroup.dcp.bo.batch.framework.utils.JsonUtils;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfig;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.util.BeanUtil;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.jgroups.util.Util.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfig.class})
@ActiveProfiles("test")
public class BeanUtilsTest {

	private static final Logger logger = Logger.getLogger(BeanUtilsTest.class);

	private static final ObjectMapper OBJECT_MAPPER_SINGLETON = new ObjectMapper();

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	@Test
	public void testBeanUtils()  {
		Object testVal = new Object[]{"ob"} ;
		assertNotNull(BeanUtils.toStringUsingJackson(testVal));
	}

	@Test
	public void testBeanUtilsNegative() throws JsonProcessingException {
		Object testVal = new Object[]{"ob"} ;

		Object mockItem = mock(Object.class);
		when(mockItem.toString()).thenReturn(mockItem.getClass().getName());

		assertNotNull(BeanUtils.toStringUsingJackson(mockItem));
	}

	@Test
	public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<BeanUtils> constructor = BeanUtils.class.getDeclaredConstructor();
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
}
