package com.rhbgroup.dcpbo.customer.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface InvestigationAuditListCSVService {

    void listing(HttpServletResponse response, String eventCodes, String fromDate, String toDate, String status) throws IOException;
}
