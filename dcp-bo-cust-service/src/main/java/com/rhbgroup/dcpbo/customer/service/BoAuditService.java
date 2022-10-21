package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcpbo.customer.contract.BoData;

import java.util.List;

public interface BoAuditService {
    BoData fetchAuditListBy(List<Integer> moduleList,
                            String username,
                            String selectedDate,
                            Integer pageNo);
}
