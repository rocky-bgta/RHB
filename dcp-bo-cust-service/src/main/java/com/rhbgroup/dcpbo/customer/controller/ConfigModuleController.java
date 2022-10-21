package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.ConfigModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bo")
public class ConfigModuleController {

    @Autowired
    ConfigModuleService configModuleService;

    @GetMapping(path = "/config/module/list", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE })
    public BoData getModuleList() {
        return configModuleService.getConfigModules();
    }
}
