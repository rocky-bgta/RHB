package com.rhbgroup.dcpbo.customer.contract;

import com.rhbgroup.dcp.model.Capsule;

/**
 * All Dto class must implement this if the source data come from capsule.
 * @author faisal
 */
public interface CapsuleToDto {

    public BoData convert(Capsule capsule);
}
