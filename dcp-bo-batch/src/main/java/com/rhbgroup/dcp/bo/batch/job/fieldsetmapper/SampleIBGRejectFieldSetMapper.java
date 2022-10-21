package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.rhbgroup.dcp.bo.batch.framework.utils.DateUtils;
import com.rhbgroup.dcp.bo.batch.job.model.SampleIBGReject;

import java.text.ParseException;

public class SampleIBGRejectFieldSetMapper implements FieldSetMapper<SampleIBGReject> {
    final static Logger logger = Logger.getLogger(SampleIBGRejectFieldSetMapper.class);

    @Override
    public SampleIBGReject mapFieldSet(FieldSet fieldSet) throws BindException {
        SampleIBGReject sampleIBGReject=new SampleIBGReject();
        sampleIBGReject.setTeller(fieldSet.readString("teller"));
        sampleIBGReject.setTrace(fieldSet.readString("trace"));
        sampleIBGReject.setRef1(fieldSet.readString("ref1"));
        sampleIBGReject.setName(fieldSet.readString("name"));
        sampleIBGReject.setRejectCode(fieldSet.readString("rejectCode"));
        sampleIBGReject.setAccountNumber(fieldSet.readString("accountNumber"));
        sampleIBGReject.setBeneName(fieldSet.readString("beneName"));
        sampleIBGReject.setBeneAccount(fieldSet.readString("beneAccount"));

        try {
            String dateString = fieldSet.readString("date");
            String amountString= fieldSet.readString("amount");
            sampleIBGReject.setDate(DateUtils.getDateFromString(dateString,"yyyyMMdd"));
            sampleIBGReject.setAmount(Double.parseDouble(StringUtils.stripStart(amountString,"0"))/100);
        } catch (ParseException ex) {
            logger.error(ex);
        }

        return sampleIBGReject;
    }
}
