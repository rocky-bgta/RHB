package com.rhbgroup.dcpbo.customer.repository;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhbgroup.dcpbo.customer.model.Audit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BoRepositoryHelperTest.class, BoRepositoryHelper.class })
public class BoRepositoryHelperTest {
	
	@Autowired
	BoRepositoryHelper boRepositoryHelper;
	
	@Test
	public void testConstructEventCodesFromAuditList() {
		
		// '90001','90005','90002','90004','10001','10004','10005','10002'
		List<Audit> dcpAudits = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			for(int j = 0; j < 8; j++) {
				Audit audit = new Audit();
				String eventCode = "9000" + Integer.toString(j);
				audit.setEventCode(eventCode);
				dcpAudits.add(audit);
			}
		}
		
		assertEquals(24, dcpAudits.size());
		assertEquals(8, boRepositoryHelper.constructEventCodesFromAuditList(dcpAudits).size());
	}

}
