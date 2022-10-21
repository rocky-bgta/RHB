package com.rhbgroup.dcpbo.customer.repository;

import com.rhbgroup.dcp.data.entity.loans.LoanProduct;
import com.rhbgroup.dcp.data.entity.loans.LoanProfile;
import com.rhbgroup.dcp.data.repository.LoanRepository;
import io.ebean.Ebean;

public class BoLoanProfileRepository extends LoanRepository {

    public LoanProfile findByAccountId(int id) {
        return Ebean.find ( LoanProfile.class )
                .where ()
                .eq ( "id" , id )
                .findOne();
    }

    public LoanProduct findByLoanId(int id) {
        return Ebean.find ( LoanProduct.class )
                .where ()
                .eq ( "id" , id )
                .findOne();
    }
}
