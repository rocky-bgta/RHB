package com.rhbgroup.dcpbo.customer.audit.collector;

/**
 * Audit additional data factory.
 * @author faisal
 */
public interface AuditAdditionalDataFactory {

    AuditAdditionalDataRetriever getService(String serviceName);
}
