package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.BoAuditService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bo")
public class BoAuditController {

    private static Logger logger = LogManager.getLogger(BoAuditController.class);

    @Autowired
    BoAuditService boAuditService;

    @GetMapping(path = "/audit/list",
            produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public BoData getAuditDetails(
            @RequestParam(value = "moduleList") List<Integer> moduleList,
            @RequestParam(value = "username", required = false, defaultValue = "") String username,
            @RequestParam(value = "selectedDate", required = false, defaultValue = "") String selectedDate,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") int pageNo) {

        logger.info("getAuditDetails.. ");
        logger.info("moduleList: " + moduleList);
        logger.info("username: " + username);
        logger.info("selectedDate: " + selectedDate);
        logger.info("pageNo: " + pageNo);

        return boAuditService.fetchAuditListBy(moduleList,
                username,
                selectedDate,
                pageNo);
    }
}
