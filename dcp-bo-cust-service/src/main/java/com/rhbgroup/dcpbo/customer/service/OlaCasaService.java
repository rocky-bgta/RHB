package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;

public interface OlaCasaService {
    BoData searchOlaCasaValue(String value, Integer pageNo);

    BoData listOlaCasaEvents(String idNo, String fromDate, String toDate, Integer pageNo);

    BoData getAuditDetails(String token);
}