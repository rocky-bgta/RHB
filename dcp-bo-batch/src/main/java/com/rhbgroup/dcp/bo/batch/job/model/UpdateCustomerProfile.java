package com.rhbgroup.dcp.bo.batch.job.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.batch.item.ItemCountAware;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UpdateCustomerProfile implements ItemCountAware {
    private int count;
    int jobExecutionId;
    boolean isProcessed;
    String fileName;

    @Size(max = 14)
    String cisNo;
    @Size(max = 3)
    String race;
    @Size(max = 10)
    String birthDate;
    @Size(max = 2)
    String gender;
    @Size(max = 1)
    String staffIndicator;
    @Size(max = 2)
    String hostCustomerType;
    @Size(max = 40)
    String maddress1;
    @Size(max = 40)
    String maddress2;
    @Size(max = 40)
    String maddress3;
    @Size(max = 40)
    String maddress4;
    @Size(max = 10)
    String postcode;
    @Size(max = 20)
    String city;
    @Size(max = 20)
    String state;
    @Size(max = 2)
    String country;

    @Override
    public void setItemCount(int count) {
        this.count = count;
    }
}
