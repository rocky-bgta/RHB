package com.rhbgroup.dcp.bo.batch.job.fieldextractor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rhbgroup.dcp.bo.batch.framework.constants.BatchErrorCode;
import com.rhbgroup.dcp.bo.batch.framework.exception.BatchException;
import com.rhbgroup.dcp.bo.batch.framework.utils.JsonUtils;
import com.rhbgroup.dcp.bo.batch.job.model.SampleIBGReject;

import org.apache.log4j.Logger;
import org.springframework.batch.item.file.transform.FieldExtractor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SampleIBGRejectFieldExtractor  implements FieldExtractor<SampleIBGReject> {
    final static Logger logger = Logger.getLogger(SampleIBGRejectFieldExtractor.class);

    @Override
    public Object[] extract(SampleIBGReject sampleIBGReject) {
        List<String> outputBuilder = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        outputBuilder.add(simpleDateFormat.format(sampleIBGReject.getDate()));
        outputBuilder.add(sampleIBGReject.getTeller());
        outputBuilder.add(sampleIBGReject.getTrace());
        outputBuilder.add(sampleIBGReject.getRef1());
        outputBuilder.add(sampleIBGReject.getName());
        outputBuilder.add(sampleIBGReject.getAmount().toString());
        outputBuilder.add(sampleIBGReject.getRejectCode());
        outputBuilder.add(sampleIBGReject.getAccountNumber());
        outputBuilder.add(sampleIBGReject.getBeneName());
        outputBuilder.add(sampleIBGReject.getBeneAccount());
        return outputBuilder.isEmpty() ? null : outputBuilder.toArray();
    }
}
