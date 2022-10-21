package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import com.rhbgroup.dcp.bo.batch.job.model.UpdateCustomerProfile;
import org.apache.log4j.Logger;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class UpdateCustomerProfileFieldSetMapper implements FieldSetMapper<UpdateCustomerProfile> {
    final static Logger logger = Logger.getLogger(UpdateCustomerProfileFieldSetMapper.class);

    @Override
    public UpdateCustomerProfile mapFieldSet(FieldSet fieldSet) throws BindException {
        UpdateCustomerProfile updateCustomerProfile=new UpdateCustomerProfile();
        updateCustomerProfile.setCisNo(fieldSet.readString("cisNo"));
        updateCustomerProfile.setRace(fieldSet.readString("race"));
        updateCustomerProfile.setBirthDate(fieldSet.readString("birthDate"));
        updateCustomerProfile.setGender(fieldSet.readString("gender"));
        updateCustomerProfile.setStaffIndicator(fieldSet.readString("staffIndicator"));
        updateCustomerProfile.setHostCustomerType(fieldSet.readString("hostCustomerType"));
        updateCustomerProfile.setMaddress1(fieldSet.readString("maddress1"));
        updateCustomerProfile.setMaddress2(fieldSet.readString("maddress2"));
        updateCustomerProfile.setMaddress3(fieldSet.readString("maddress3"));
        updateCustomerProfile.setMaddress4(fieldSet.readString("maddress4"));
        updateCustomerProfile.setPostcode(fieldSet.readString("postcode"));
        updateCustomerProfile.setCity(fieldSet.readString("city"));
        updateCustomerProfile.setState(fieldSet.readString("state"));
        updateCustomerProfile.setCountry(fieldSet.readString("country"));

        return updateCustomerProfile;
    }
}
