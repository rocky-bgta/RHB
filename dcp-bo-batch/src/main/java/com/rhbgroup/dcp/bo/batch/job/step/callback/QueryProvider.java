package com.rhbgroup.dcp.bo.batch.job.step.callback;

import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.stereotype.Component;

@Component
public interface QueryProvider {
    public PagingQueryProvider call();
}
