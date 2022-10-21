package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.contract.ViewTransaction;
import com.rhbgroup.dcpbo.customer.dto.MortgageTransactions;
import com.rhbgroup.dcpbo.customer.service.GetMortgageTransactionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/bo/cs/customer/loan")
public class LoanController {

    private  Logger logger = LogManager.getLogger(LoanController.class);

    @Autowired @Qualifier(value = "viewTransactionAsb")
    ViewTransaction viewTransaction;

    @Autowired
    GetMortgageTransactionService getMortgageTransactionService;

    @Autowired @Qualifier(value = "viewTransactionPersonalLoan")
    ViewTransaction viewTransactionPersonalLoan;

    @Autowired @Qualifier(value = "viewHirePurchaseTransactionLoan")
    ViewTransaction viewHirePurchaseTransactionLoan;

    //TODO tell AD accountId is in int and not string

    @BoControllerAudit(eventCode = "30021")
    @GetMapping(path = "/asb/{accountNo}/transactions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BoData getAsbTransactionsHistory(
            @RequestHeader("customerId") int customerId,
            @PathVariable("accountNo") String accountNo,
            @RequestParam(value = "pageCounter", required = false, defaultValue = "1") Integer pageCounter,
            @RequestParam(value = "firstKey", required = false, defaultValue = "") String firstKey,
            @RequestParam(value = "lastKey", required = false, defaultValue = "") String lastKey) {
        logger.debug("getAsbTransactionsHistory customerId {} ,accountNo {} ",customerId,accountNo);
        return viewTransaction.listing(customerId, accountNo, pageCounter, firstKey, lastKey);
    }

    @BoControllerAudit(eventCode = "30023")
    @GetMapping(value = "/mortgage/{accountNo}/transactions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BoData getMortgageTransactions(@RequestHeader("customerId") int customerId,
                                                        @PathVariable("accountNo") String accountNo,
                                                        @RequestParam(value = "pageCounter", required = false, defaultValue = "1") Integer pageCounter,
                                                        @RequestParam(name = "firstKey", required = false, defaultValue = "") String firstKey,
                                                        @RequestParam(name = "lastKey", required = false, defaultValue = "") String lastKey){

        return getMortgageTransactionService.getMortgageTransactions(customerId, accountNo, pageCounter, firstKey, lastKey);

    }

    @BoControllerAudit(eventCode = "30024")
    @GetMapping(path = "/personal/{accountNo}/transactions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BoData getPersonalTransactions(@RequestHeader("customerId") int customerId,
                                          @PathVariable("accountNo") String accountNo,
                                          @RequestParam(value =  "pageCounter", required = false, defaultValue = "1") Integer pageCounter,
                                          @RequestParam(value = "firstKey", required = false, defaultValue = "") String firstKey,
                                          @RequestParam(value = "lastKey", required = false, defaultValue = "") String lastKey) {
        return viewTransactionPersonalLoan.listing(customerId, accountNo, pageCounter, firstKey, lastKey);
    }

    @BoControllerAudit(eventCode = "30022")
    @GetMapping(path = "/hp/{accountNo}/transactions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BoData getHirePurchaseTransactions(@RequestHeader("customerId") int customerId,
                                              @PathVariable("accountNo") String accountNo,
                                              @RequestParam(value =  "pageCounter", required = false, defaultValue = "1") Integer pageCounter,
                                              @RequestParam(value = "firstKey", required = false, defaultValue = "") String firstKey,
                                              @RequestParam(value = "lastKey", required = false, defaultValue = "") String lastKey) {
        return viewHirePurchaseTransactionLoan.listing(customerId, accountNo, pageCounter, firstKey, lastKey);
    }
}
