package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.OlaCasaService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/bo/cs/customer/olacasa")
public class OlaCasaController {
    private static Logger logger = LogManager.getLogger(OlaCasaController.class);

    @Autowired
    OlaCasaService olaCasaService;

    @BoControllerAudit(eventCode = "30114")
    @GetMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody BoData searchOlaCasa(
            @RequestParam("value") String value,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo)  {
        logger.info("API call: /bo/cs/customer/olacasa/search/");
        return olaCasaService.searchOlaCasaValue(value, pageNo);
    }

    @BoControllerAudit(eventCode = "30115")
    @GetMapping(path = "/audit/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody BoData listOlaCasa(
            @RequestParam("idNo") String idNo,
            @RequestParam(value = "fromDate", required = false, defaultValue = "") String fromDate,
            @RequestParam(value = "toDate", required = false, defaultValue = "") String toDate,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo)  {
        logger.info("API call: /bo/cs/customer/olacasa/audit/list");
        return olaCasaService.listOlaCasaEvents(idNo, fromDate, toDate, pageNo);
    }

    @BoControllerAudit(eventCode = "30116")
    @GetMapping(path = "/audit/{token}/details", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody BoData getAuditDetails(
            @PathVariable(value = "token") String token) {
        logger.debug("token: " + token);
        return olaCasaService.getAuditDetails(token);
    }
}
