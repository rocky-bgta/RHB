package com.rhbgroup.dcp.bo.batch.framework.core.mapping;

import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.util.StringUtils;

public class SkipEmptyLineMapper<T> extends DefaultLineMapper<T> {

    @Override
    public T mapLine(String line, int lineNumber) throws Exception {
        if (StringUtils.isEmpty(line)){
            return null;
        }
        return super.mapLine(line, lineNumber);
    }
}