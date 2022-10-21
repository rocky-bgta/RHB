package com.rhbgroup.dcp.bo.batch.job.flatfilecallback;

import org.springframework.batch.item.file.FlatFileFooterCallback;

import java.io.IOException;
import java.io.Writer;

public class StringFooterWriter implements FlatFileFooterCallback {
    private final String footer;

    public StringFooterWriter(String footer) {
        this.footer = footer;
    }
    @Override
    public void writeFooter(Writer writer) throws IOException {
        writer.write(footer);
    }
}
