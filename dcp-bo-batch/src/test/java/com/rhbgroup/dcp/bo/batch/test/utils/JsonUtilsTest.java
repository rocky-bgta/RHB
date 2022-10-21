package com.rhbgroup.dcp.bo.batch.test.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.JsonUtils;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfig;
import freemarker.template.Configuration;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.SocketException;

import static org.jgroups.util.Util.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {BatchTestConfig.class})
@ActiveProfiles("test")
public class JsonUtilsTest {

	private static final Logger logger = Logger.getLogger(JsonUtilsTest.class);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private JsonUtils jsonUtils;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	@Before
	public void setup() throws IOException {
	}

	@After
	public void destroy() {
	}

	@Test
	public void testJsonUtils()  {
		Object testVal = new Object[]{"ob"} ;
		assertNotNull(JsonUtils.convertObjectToString(testVal));
	}

	@Test
	public void testJsonUtilsNegative() throws JsonProcessingException {
		Object testVal = new Object[]{"ob"} ;

		Object mockItem = mock(Object.class);
		when(mockItem.toString()).thenReturn(mockItem.getClass().getName());

		assertNotNull(JsonUtils.convertObjectToString(mockItem));
	}

	@Test
	public void testConstructorIsPrivate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		Constructor<JsonUtils> constructor = JsonUtils.class.getDeclaredConstructor();
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
