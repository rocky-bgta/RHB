package com.rhbgroup.dcp.bo.batch.test.repository;

import com.rhbgroup.dcp.bo.batch.email.EmailTemplate;
import com.rhbgroup.dcp.bo.batch.job.model.SnapshotBoUsersGroup;
import com.rhbgroup.dcp.bo.batch.job.repository.SnapshotBoUsersGroupRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.test.common.BaseJobTest;
import com.rhbgroup.dcp.bo.batch.test.config.BatchTestConfigHSQL;
import freemarker.template.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchTestConfigHSQL.class })
@ActiveProfiles("test")
public class SnapshotBoUsersGroupRepositoryImplTest extends BaseJobTest {

	@Autowired
	SnapshotBoUsersGroupRepositoryImpl snapshotBoUsersGroupRepository;
	
	@MockBean
	JdbcTemplate mockJdbcTemplate;

	@MockBean
	private Configuration config;

	@MockBean
	private EmailTemplate emailTemplate;

	private SnapshotBoUsersGroup snapshotBoUsersGroup = new SnapshotBoUsersGroup();

	public SnapshotBoUsersGroup getSnapshotObject(){
		snapshotBoUsersGroup.setJobExecutionId(1);
		snapshotBoUsersGroup.setUserGroup("1");
		snapshotBoUsersGroup.setRole("1");
		snapshotBoUsersGroup.setFunctionName("1");
		snapshotBoUsersGroup.setCreatedBy("1");
		snapshotBoUsersGroup.setCreatedTime(new Date().toString());
		snapshotBoUsersGroup.setDeptName("1");
		snapshotBoUsersGroup.setUserId("1");
		snapshotBoUsersGroup.setUserName("1");
		snapshotBoUsersGroup.setStatus("1");
		snapshotBoUsersGroup.setUserCreatedDate("1");
		snapshotBoUsersGroup.setUserCreatedTime("1");
		snapshotBoUsersGroup.setUserUpdatedTime("1");
		snapshotBoUsersGroup.setUserUpdatedBy("1");
		snapshotBoUsersGroup.setLastLoginDate("1");
		snapshotBoUsersGroup.setLastLoginTime("1");
		return snapshotBoUsersGroup;
	}

	@Test
	public void testPositiveinsertBoUser() throws Exception {

		when(mockJdbcTemplate.update(
				Mockito.anyString(),
				Mockito.anyInt(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString())).thenReturn(1);
		
		assertTrue(snapshotBoUsersGroupRepository.insertBoUser(getSnapshotObject()));
	}
	
	@Test
	public void testNegativeinsertBoUser() throws Exception {

		when(mockJdbcTemplate.update(
				Mockito.anyString(),
				Mockito.anyInt(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString())).thenThrow(UncategorizedSQLException.class);

		assertFalse(snapshotBoUsersGroupRepository.insertBoUser(getSnapshotObject()));
	}

	@Test
	public void testPositiveinsertBoUserGroup() throws Exception {

		when(mockJdbcTemplate.update(
				Mockito.anyString(),
				Mockito.anyInt(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString())).thenReturn(1);

		assertTrue(snapshotBoUsersGroupRepository.insertBoUserGroup(getSnapshotObject()));
	}

	@Test
	public void testNegativeinsertBoUserGroup() throws Exception {

		when(mockJdbcTemplate.update(
				Mockito.anyString(),
				Mockito.anyInt(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString(),
				Mockito.anyString())).thenThrow(UncategorizedSQLException.class);

		assertFalse(snapshotBoUsersGroupRepository.insertBoUserGroup(getSnapshotObject()));
	}

	@Test
	public void testPositiveDeleteUserGroupSameDayRecords() throws Exception {

		ArrayList list = new ArrayList();
		list.add(getSnapshotObject());
		when(mockJdbcTemplate.queryForList(Mockito.anyString())).thenReturn(list);

		assertTrue(snapshotBoUsersGroupRepository.deleteUserGroupSameDayRecords(1, new Date()));
	}

	@Test
	public void testNegativeDeleteUserGroupSameDayRecords() throws Exception {

		when(mockJdbcTemplate.queryForList(Mockito.anyString())).thenThrow(UncategorizedSQLException.class);
		assertFalse(snapshotBoUsersGroupRepository.deleteUserGroupSameDayRecords(1, new Date()));
	}
}
