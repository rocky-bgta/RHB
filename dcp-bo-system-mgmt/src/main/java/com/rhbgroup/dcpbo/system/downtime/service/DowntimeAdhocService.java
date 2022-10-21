package com.rhbgroup.dcpbo.system.downtime.service;

import org.springframework.http.ResponseEntity;

import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.vo.AddDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.DeleteDowntimeAdhocRequestVo;
import com.rhbgroup.dcpbo.system.downtime.vo.UpdateDowntimeAdhocRequestVo;

public interface DowntimeAdhocService {
	public ResponseEntity<BoData> addDowntimeAdhoc(AddDowntimeAdhocRequestVo request, String userId);
	public ResponseEntity<BoData> updateDowntimeAdhoc(UpdateDowntimeAdhocRequestVo request, int id, String userId);
	public ResponseEntity<BoData> deleteDowntimeAdhoc(DeleteDowntimeAdhocRequestVo request, int id, String userId);
	public BoData getDowntimeAdhocs(Integer pageNo, String startTime, String endTime, String adhocCategories, String status);
	public ResponseEntity<BoData> getAdhocCategoryList();
	public BoData getAdhocTypesList(String category);

}
