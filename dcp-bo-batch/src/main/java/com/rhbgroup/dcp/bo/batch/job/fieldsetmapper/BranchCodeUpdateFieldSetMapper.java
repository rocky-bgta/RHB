package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import com.rhbgroup.dcp.bo.batch.job.model.BranchCodeUpdate;
import org.apache.log4j.Logger;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class BranchCodeUpdateFieldSetMapper implements FieldSetMapper<BranchCodeUpdate> {
    static final Logger logger = Logger.getLogger(BranchCodeUpdateFieldSetMapper.class);

    @Override
    public BranchCodeUpdate mapFieldSet(FieldSet fieldSet) throws BindException {
        BranchCodeUpdate branchCodeUpdate=new BranchCodeUpdate();
        branchCodeUpdate.setRecordType(fieldSet.readString("recordType"));
        branchCodeUpdate.setBnmBranchCode(fieldSet.readString("bnmBranchCode"));
        branchCodeUpdate.setRhbBranchCode(fieldSet.readString("rhbBranchCode"));
        branchCodeUpdate.setRhbBranchName(fieldSet.readString("rhbBranchName"));
        branchCodeUpdate.setRhbBranchAdd1(fieldSet.readString("rhbBranchAdd1"));
        branchCodeUpdate.setRhbBranchAdd2(fieldSet.readString("rhbBranchAdd2"));
        branchCodeUpdate.setRhbBranchAdd3(fieldSet.readString("rhbBranchAdd3"));
        branchCodeUpdate.setExtra1(fieldSet.readString("extra1"));
        branchCodeUpdate.setExtra2(fieldSet.readString("extra2"));
        branchCodeUpdate.setExtra3(fieldSet.readString("extra3"));
        return branchCodeUpdate;
    }
}
