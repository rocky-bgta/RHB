package com.rhbgroup.dcpbo.customer.audit.collector;

import java.util.Map;

/**
 * For any audit which require additional data inside its json to audit queue need to implement this.
 * @author faisal
 */
public interface AuditAdditionalDataRetriever {

    Map<String, String> retrieve();
}
