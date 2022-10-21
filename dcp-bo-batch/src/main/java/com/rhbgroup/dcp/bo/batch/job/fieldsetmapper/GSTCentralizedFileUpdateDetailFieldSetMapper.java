package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import com.rhbgroup.dcp.bo.batch.job.model.GSTCentralizedFileUpdateDetail;
import org.apache.log4j.Logger;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class GSTCentralizedFileUpdateDetailFieldSetMapper implements FieldSetMapper<GSTCentralizedFileUpdateDetail> {
    final static Logger logger = Logger.getLogger(GSTCentralizedFileUpdateDetailFieldSetMapper.class);

    @Override
    @StepScope
    public GSTCentralizedFileUpdateDetail mapFieldSet(FieldSet fieldSet) throws BindException {

        GSTCentralizedFileUpdateDetail gstCentralizedFileUpdateDetail=new GSTCentralizedFileUpdateDetail();

        // Ignore footer
        if(fieldSet.readString("recordIndicator").equalsIgnoreCase("99"))
        {
            return null;
        }else {
            gstCentralizedFileUpdateDetail.setRecordIndicator(fieldSet.readString("recordIndicator"));
            gstCentralizedFileUpdateDetail.setEntityCode(fieldSet.readString("entityCode"));
            gstCentralizedFileUpdateDetail.setEntityIndicator(fieldSet.readString("entityIndicator"));
            gstCentralizedFileUpdateDetail.setUniqueId(fieldSet.readString("uniqueId"));
            gstCentralizedFileUpdateDetail.setSourceSystemId(fieldSet.readString("sourceSystemId"));
            gstCentralizedFileUpdateDetail.setTransactionIdentifier(fieldSet.readString("transactionIdentifier"));
            gstCentralizedFileUpdateDetail.setTransactionDescription(fieldSet.readString("transactionDescription"));
            gstCentralizedFileUpdateDetail.setTreatmentType(fieldSet.readString("treatmentType"));
            gstCentralizedFileUpdateDetail.setTaxCode(fieldSet.readString("taxCode"));
            gstCentralizedFileUpdateDetail.setCalculationMethod(fieldSet.readString("calculationMethod"));
            gstCentralizedFileUpdateDetail.setGlAccountCodeCharges(fieldSet.readString("glAccountCodeCharges"));
            gstCentralizedFileUpdateDetail.setLastUpdateBy(fieldSet.readString("lastUpdateBy"));
            gstCentralizedFileUpdateDetail.setHostTranOrGSTGLCode(fieldSet.readString("hostTranOrGSTGLCode"));
            gstCentralizedFileUpdateDetail.setFiller(fieldSet.readString("filler"));

            try {

                gstCentralizedFileUpdateDetail.setFileName("");
                gstCentralizedFileUpdateDetail.setHdDate("");
                gstCentralizedFileUpdateDetail.setHdTime("");

                gstCentralizedFileUpdateDetail.setStartDate(fieldSet.readString("startDate"));
                gstCentralizedFileUpdateDetail.setEndDate(fieldSet.readString("endDate"));
                gstCentralizedFileUpdateDetail.setLastUpdateDate(fieldSet.readString("lastUpdateDate"));
                gstCentralizedFileUpdateDetail.setLastUpdateTime(fieldSet.readString("lastUpdateTime"));
                gstCentralizedFileUpdateDetail.setGstRate(fieldSet.readString("gstRate"));

            } catch (Exception ex) {
                logger.error(ex);
            }

            return gstCentralizedFileUpdateDetail;
        }
    }
}
