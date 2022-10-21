package com.rhbgroup.dcpbo.customer.dto;

import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.model.OlaTokenUser;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class OlaCasaResponse implements BoData {

    private List<OlaTokenUser> customer;
    private OlaCasaPagination pagination;

    public OlaCasaResponse() {
        customer = new ArrayList<>();
    }

}
