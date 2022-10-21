package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcpbo.customer.model.AuditDetailsRecord;

public interface AuditDetailsTableRepository {
	public AuditDetailsRecord findByAuditId(Integer auditId);
}
