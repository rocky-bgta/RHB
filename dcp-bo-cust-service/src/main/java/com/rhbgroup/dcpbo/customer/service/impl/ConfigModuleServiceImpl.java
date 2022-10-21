package com.rhbgroup.dcpbo.customer.service.impl;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.dto.ConfigModules;
import com.rhbgroup.dcpbo.customer.dto.Module;
import com.rhbgroup.dcpbo.customer.dcpbo.ConfigModule;
import com.rhbgroup.dcpbo.customer.repository.ConfigModuleRepository;
import com.rhbgroup.dcpbo.customer.service.ConfigModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfigModuleServiceImpl implements ConfigModuleService {

    @Autowired
    ConfigModuleRepository configModuleRepository;

    @Override
    public BoData getConfigModules() {

        List<ConfigModule> configModuleList = configModuleRepository.findAll();
        ConfigModules configModules = new ConfigModules();
        configModules.getModules().addAll(configModuleList.stream().map(cm -> {
            Module module = new Module();
            module.setModuleId(cm.getId());
            module.setModuleName(cm.getModuleName());
            return module;
        }).collect(Collectors.toList()));

        return configModules;
    }
}
