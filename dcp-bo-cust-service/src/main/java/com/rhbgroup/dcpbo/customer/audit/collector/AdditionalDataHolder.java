package com.rhbgroup.dcpbo.customer.audit.collector;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.context.annotation.RequestScope;

import java.util.HashMap;
import java.util.Map;

/**
 * Use ONLY to put additional data. We don't care how they going to retrieve it.
 * @author faisal
 */
@RequestScope
@Setter
@Getter
public class AdditionalDataHolder {

    private Map<String, String> map;

    public AdditionalDataHolder() {
        map = new HashMap<>();
    }
}
