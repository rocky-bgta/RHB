package com.rhbgroup.dcpbo.customer.audit.collector;

public interface BoAuditLogQueue {
    void send(String jsonStr);
}
