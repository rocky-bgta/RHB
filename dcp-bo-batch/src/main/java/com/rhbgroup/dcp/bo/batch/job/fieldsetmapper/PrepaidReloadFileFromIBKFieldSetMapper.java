package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import com.rhbgroup.dcp.bo.batch.job.model.PrepaidReloadFileFromIBK;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class PrepaidReloadFileFromIBKFieldSetMapper implements FieldSetMapper<PrepaidReloadFileFromIBK> {
    @Override
    public PrepaidReloadFileFromIBK mapFieldSet(FieldSet fieldSet) throws BindException {
        PrepaidReloadFileFromIBK prepaidReloadFileFromIBK=new PrepaidReloadFileFromIBK();
        prepaidReloadFileFromIBK.setTxnTime(fieldSet.readString("txnTime"));
        prepaidReloadFileFromIBK.setRefNo(fieldSet.readString("refNo"));
        prepaidReloadFileFromIBK.setHostRefNo(fieldSet.readString("hostRefNo"));
        prepaidReloadFileFromIBK.setMobileNo(fieldSet.readString("mobileNo"));
        prepaidReloadFileFromIBK.setPrepaidProductCode(fieldSet.readString("prepaidProductCode"));
        prepaidReloadFileFromIBK.setAmount(fieldSet.readString("amount"));
        prepaidReloadFileFromIBK.setTxnStatus(fieldSet.readString("txnStatus"));

        return prepaidReloadFileFromIBK;
    }
}
