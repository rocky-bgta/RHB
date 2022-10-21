package com.rhbgroup.dcp.bo.batch.framework.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Lazy
@Getter
@Setter
public class DcpBatchApplicationContext {
    private Map<String,String> initialJobArguments;
}