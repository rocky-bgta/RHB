package com.rhbgroup.dcpbo.customer.controller;

import com.rhbgroup.dcpbo.customer.aspect.ApiContext;
import com.rhbgroup.dcpbo.customer.audit.collector.BoControllerAudit;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.contract.SearchCustomer;
import com.rhbgroup.dcpbo.customer.dto.*;
import com.rhbgroup.dcpbo.customer.model.DelApprovalRequest;
import com.rhbgroup.dcpbo.customer.service.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="/bo/cs") // This means URL's start with /bo/cs (after Application path)
public class CustController {

    Logger logger= LogManager.getLogger(CustController.class);
    @Autowired
    SearchCustomer searchCustomer;

    @Autowired
    ApiContext apiContext;

    @Autowired
    GetTransactionTransferService getTransactionTransferService;

    @Autowired
    GetTransactionTopupService getTransactionTopupService;

    @Autowired
    DeleteDeviceService deleteDeviceService;

    @Autowired
    GetTransactionSearchService getTransactionSearchService;

    @Autowired
    GetTransactionPaymentService getTransactionPaymentService;

    @Autowired
    GetFavouritesTransferService getFavouritesTransferService;

    @Autowired
    GetDevicesService getDevicesService;

    @Autowired
    PutProfileStatusService putProfileStatusService;

    @Autowired
    DeleteDeviceRequestService deleteDeviceRequestService;
    
    @Autowired
    PutUnlockFacilityService putUnlockFacilityService;

    //DCPBL-8455 : Retrieve transfer details by refId on Activity tab
    @BoControllerAudit(eventCode = "30014")
    @GetMapping(value = "/transaction/transfer/details/{refId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    BoData getTransactionTransfer(@PathVariable("refId") String refId) {
        BoData responseBody = getTransactionTransferService.retrieveTransactionTransfer (refId);
        return responseBody;
    }

    //DCPBL-8456 : Retrieve topup details by refId on Activity tab
    @BoControllerAudit(eventCode = "30013")
    @GetMapping(value = "/transaction/topup/details/{refId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    BoData getTransactionTopup(@PathVariable("refId") String refId) {
        BoData responseBody = getTransactionTopupService.retrieveTransactionTopup (refId);
        return responseBody;
    }

    @BoControllerAudit(eventCode = "30009")
    @GetMapping(value = "/customer/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public BoData getCustomerSearch(
            @RequestParam("searchtype") String searchtype,
            @RequestParam("value") String value,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo){
        return this.searchCustomer.getCustomerTypeValue(searchtype, value, pageNo);
    }
    
    @BoControllerAudit(eventCode = "30009")
    @GetMapping(value = "/customer/registration/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public BoData getCustomerRegistrationSearch(
            @RequestParam("value") String value,
            @RequestParam(value = "pageNo", required = false, defaultValue = "1") Integer pageNo){
        return this.searchCustomer.getCustomerValue(value, pageNo);
    }
    
    @BoControllerAudit(eventCode = "30007", value = "deleteDeviceAdditionalDataRetriever")
    @DeleteMapping("/customer/device/{deviceId}")
    public void deleteCustomerDevice(@PathVariable("deviceId") Integer deviceId){
        deleteDeviceService.deleteDevice(deviceId);
    }

    //DCPBL-7182 : search transaction and customer details by refId
    @BoControllerAudit(eventCode = "30012")
    @GetMapping(value = "/transaction/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<TransactionSearch> getTransactionSearch(@RequestParam("value") String value) {
        List<TransactionSearch> responseBody = getTransactionSearchService.retrieveTransactionSearch(value);
        return responseBody;
    }

    //DCPBL-7185 : Retrieve payment details by refId on Activity tab
    @BoControllerAudit(eventCode = "30011")
    @GetMapping(value = "/transaction/payment/details/{refId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    BoData getTransactionPayment(@PathVariable("refId") String refId) {
        BoData responseBody = getTransactionPaymentService.retrieveTransactionPayment(refId);
        return responseBody;
    }

    //DCPBL-8411 : Retrieve customer's
    @BoControllerAudit(eventCode = "30010")
    @GetMapping(value = "/favourites/{favouritesId}/transfer", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    public @ResponseBody
    BoData getFavouritesTransfer(@PathVariable("favouritesId") Integer favouritesId) {
        BoData responseBody = getFavouritesTransferService.retrieveFavouritesTransfer(favouritesId);
        return responseBody;
    }

    //DCPBL-7183 : Retrieve list of devices for user
    @BoControllerAudit(eventCode = "30008")
    @GetMapping(value = "/customer/devices", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<Device> getDevices(@RequestHeader("customerId") String customerId) {
        List<Device> result = getDevicesService.retrieveDevice (customerId);
        return result;
    }

    //DCPBL-7821 : Unlock customer account from CQ lockout
    @BoControllerAudit(eventCode = "30028", value = "putProfileStatusAdditionalDataRetriever")
    @PutMapping(value = "/customer/reset/failed-challenge-count", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    public @ResponseBody
    BoData putProfileStatus(@RequestHeader("customerId") String customerId) {
        BoData responseBody = putProfileStatusService.writeProfileStatus(customerId);
        return responseBody;
    }

    @BoControllerAudit(eventCode = "30037")
    @PutMapping(value = "/customer/device/{deviceId}/delete", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    public @ResponseBody
    BoData putDeleteDeviceRequest(@PathVariable("deviceId") Integer deviceId, @RequestBody DelApprovalRequest delApprovalRequest, @RequestHeader(value = "userid") Integer userId){
        return deleteDeviceRequestService.deleteDevice(deviceId, userId, delApprovalRequest);
    }
    
    @BoControllerAudit(eventCode = "30040", value = "putUnlockFacilityAdditionalDataRetriever")
    @PutMapping(value = "/customer/{acctNumber}/unlock", produces = { MediaType.APPLICATION_JSON_UTF8_VALUE })
    public @ResponseBody
    BoData putUnlockFacility(@PathVariable("acctNumber") String acctNumber) {
    	BoData responseBody = putUnlockFacilityService.writeUnlockFacility(acctNumber);
    	return responseBody;
    }
}