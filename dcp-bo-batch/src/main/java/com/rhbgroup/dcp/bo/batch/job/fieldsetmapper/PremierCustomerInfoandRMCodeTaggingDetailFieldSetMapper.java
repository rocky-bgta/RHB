package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import com.rhbgroup.dcp.bo.batch.job.model.PremierCustomerInfoandRMCodeTaggingDetail;
import org.apache.log4j.Logger;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class PremierCustomerInfoandRMCodeTaggingDetailFieldSetMapper implements FieldSetMapper<PremierCustomerInfoandRMCodeTaggingDetail> {

    private static final String INDICATOR = "indicator";

    @Override
    @StepScope
    public PremierCustomerInfoandRMCodeTaggingDetail mapFieldSet(FieldSet fieldSet) throws BindException {

        PremierCustomerInfoandRMCodeTaggingDetail premierCustomerInfoandRMCodeTaggingDetail = new PremierCustomerInfoandRMCodeTaggingDetail();

        // Header
        if(fieldSet.readString(INDICATOR).equalsIgnoreCase("00")) {
            return null;
        }
        // Footer
        else if(fieldSet.readString(INDICATOR).equalsIgnoreCase("99")) {
            return null;
        }else {
            premierCustomerInfoandRMCodeTaggingDetail.setIndicator(fieldSet.readString(INDICATOR));
            premierCustomerInfoandRMCodeTaggingDetail.setCifNo(fieldSet.readString("cifNo"));
            premierCustomerInfoandRMCodeTaggingDetail.setRmCode(fieldSet.readString("rmCode"));
            premierCustomerInfoandRMCodeTaggingDetail.setCisNo2(fieldSet.readString("cisNo2"));
            premierCustomerInfoandRMCodeTaggingDetail.setFullNm(fieldSet.readString("fullNm"));
            premierCustomerInfoandRMCodeTaggingDetail.setCisNo3(fieldSet.readString("cisNo3"));
            premierCustomerInfoandRMCodeTaggingDetail.setIdNo(fieldSet.readString("idNo"));
            premierCustomerInfoandRMCodeTaggingDetail.setCisNo4(fieldSet.readString("cisNo4"));
            premierCustomerInfoandRMCodeTaggingDetail.setStaffInd(fieldSet.readString("staffInd"));
            premierCustomerInfoandRMCodeTaggingDetail.setFiller(fieldSet.readString("filler"));
            premierCustomerInfoandRMCodeTaggingDetail.setEnd(fieldSet.readString("end"));

            return premierCustomerInfoandRMCodeTaggingDetail;
        }
    }
}
