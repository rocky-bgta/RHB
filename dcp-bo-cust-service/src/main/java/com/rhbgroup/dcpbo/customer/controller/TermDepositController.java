package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.service.ViewDepositService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/bo/cs/customer/termdeposit")
public class TermDepositController {

    private ViewDepositService viewTermViewDepositService;

    public TermDepositController(ViewDepositService viewTermViewDepositService) {
        this.viewTermViewDepositService = viewTermViewDepositService;
    }


    @BoControllerAudit(eventCode = "30027")
    @GetMapping(path = "/{accountNo}/details", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BoData getTermDepositDetails(@RequestHeader("customerId") int customerId,
                                        @PathVariable("accountNo") String accountNo) {
        return viewTermViewDepositService.detail(customerId, accountNo);
    }
}
