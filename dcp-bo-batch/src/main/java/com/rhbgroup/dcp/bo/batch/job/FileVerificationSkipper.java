package com.rhbgroup.dcp.bo.batch.job;

import org.apache.log4j.Logger;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.file.FlatFileParseException;

import java.io.FileNotFoundException;

public class FileVerificationSkipper implements SkipPolicy {

    private static final Logger logger = Logger.getLogger(FileVerificationSkipper.class);

    @Override
    public boolean shouldSkip(Throwable exception, int skipCount) throws SkipLimitExceededException {
        if (exception instanceof FileNotFoundException) {
            return true;
        } else if (exception instanceof FlatFileParseException && skipCount <= 5) {
            FlatFileParseException ffpe = (FlatFileParseException) exception;
            StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("An error occured while processing the " + ffpe.getLineNumber()
                    + " line of the file. Below was the faulty " + "input.\n");
            errorMessage.append(ffpe.getInput() + "\n");
            logger.error("{}", exception);
            return true;
        } else {
            return true;
        }
    }
}
