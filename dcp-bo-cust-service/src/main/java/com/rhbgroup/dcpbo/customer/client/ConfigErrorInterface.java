package com.rhbgroup.dcpbo.customer.client;

import com.rhbgroup.dcpbo.customer.model.response.BoExceptionResponse;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "admin-service")
public interface ConfigErrorInterface {

    @GetMapping(value = "/admin-service/bo/error/{errorCode}")
    BoExceptionResponse getConfigError(@PathVariable("errorCode") String errorCode);
}
