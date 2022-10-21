package com.rhbgroup.dcpbo.customer.controller;


import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.AsnbTransactionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author hassan.malik
 */

@RestController
@RequestMapping(value = "/bo")
public class AsnbController {

    @Autowired
    private AsnbTransactionsService asnbTransactionsService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @BoControllerAudit(eventCode = "30038")
    @GetMapping(value = "/cs/customer/asnb/{accountNo}/transactions", produces = {
            MediaType.APPLICATION_JSON_UTF8_VALUE})
    public BoData getAsnbTransactions(@RequestHeader("customerId") String sCustomerId,
                                      @RequestParam("fundId") String fundId, @RequestParam("identificationNumber") String identificationNumber,
                                      @RequestParam("identificationType") String identificationType,
                                      @RequestParam("membershipNumber") String membershipNumber,
                                      @RequestParam("guardianIdNumber") String guardianIdNumber, @RequestParam("isMinor") boolean isMinor) {

        logger.debug("getAsnbTransactions()");
        int customerId = Integer.parseInt(sCustomerId);

        logger.debug("customerId:{}", customerId);
        logger.debug("fundId:{}", fundId);
        logger.debug("identificationNumber:{}", identificationNumber);
        logger.debug("identificationType:{}", identificationType);
        logger.debug("membershipNumber:{}", membershipNumber);
        logger.debug("isMinor:{}", isMinor);
        logger.debug("guardianIdNumber:{}", guardianIdNumber);
        return asnbTransactionsService.getAsnbTransactions(customerId, fundId, identificationNumber, identificationType,
                membershipNumber, isMinor, guardianIdNumber);
    }

}
