package com.rhbgroup.dcp.bo.batch.framework.core.mapping;

import lombok.Setter;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.util.StringUtils;

public class SkipFooterLineMapper<T> extends PatternMatchingCompositeLineMapper<T> {

    @Setter
    private int totalItemsToRead;

    @Override
    public T mapLine(String line, int lineNumber) throws Exception {
        if (StringUtils.isEmpty(line)){
            return null;
        }
        if(lineNumber > totalItemsToRead){
            return null;
        }
        return super.mapLine(line, lineNumber);
    }
}