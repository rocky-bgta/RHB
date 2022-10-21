package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewOldCustomerProfile {
    private String new_cis_no;
    private String new_race;
    private String new_birth_date;
    private String new_gender;
    private String new_is_staff;
    private String new_customer_type;
    private String old_birth_date;
    private String old_is_staff;
    private String user_id;
    private String newMAddress1;
    private String newMAddress2;
    private String newMAddress3;
    private String newMAddress4;
    private String newPostcode;
    private String newCity;
    private String newState;
    private String newCountry;
    private String oldRAddress1;
    private String oldRAddress2;
    private String oldRAddress3;
    private String oldRAddress4;
    private String oldPostcode;
    private String oldCity;
    private String oldState;
    private String oldCountry;
}
