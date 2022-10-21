    package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcp.data.entity.deposits.DepositProfile;
import com.rhbgroup.dcp.data.repository.DepositRepository;
import io.ebean.Ebean;

public class BoDepositRepository extends DepositRepository {

    public DepositProfile getDepositProfileById(int id) {
        return Ebean.find(DepositProfile.class).where().eq("id",id).findOne();
    }
}