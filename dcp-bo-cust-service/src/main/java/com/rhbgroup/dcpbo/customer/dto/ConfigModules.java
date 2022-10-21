package com.rhbgroup.dcpbo.customer.dto;


import com.rhbgroup.dcpbo.customer.contract.BoData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ConfigModules implements BoData {
    List<Module> modules;

    public ConfigModules() {
        modules = new ArrayList<>();
    }
}
