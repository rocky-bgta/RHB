package com.rhbgroup.dcpbo.system.epullenrollment.service;

import com.rhbgroup.dcp.estatement.bizlogic.EPullAutoEnrollmentLogic;
import com.rhbgroup.dcp.estatement.model.EPullAutoEnrollmentRequest;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcp.util.JsonUtil;
import com.rhbgroup.dcpbo.system.exception.EpullEnrollmentMissingException;
import com.rhbgroup.dcpbo.system.exception.EpullEnrollmentSDKException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class EpullEnrollmentServiceImpl implements EpullEnrollmentService {

    private static final Logger logger = LogManager.getLogger(EpullEnrollmentServiceImpl.class);

    @Autowired
    EPullAutoEnrollmentLogic ePullAutoEnrollmentLogic;

    public EpullEnrollmentServiceImpl(EPullAutoEnrollmentLogic ePullAutoEnrollmentLogic) {
        this.ePullAutoEnrollmentLogic = ePullAutoEnrollmentLogic;
    }

    @Override
    public ResponseEntity<String> executeEpullEnrollmentProcess(String userId, EPullAutoEnrollmentRequest request) {

        logger.debug("userId: " + userId);

        if(userId.isEmpty()) {
            logger.error("Did not provide user id");
            throw new EpullEnrollmentMissingException();
        }

        if(validateRequestPayload(request)) {
            logger.error("Request payload is empty");
            throw new EpullEnrollmentMissingException("Request payload is empty");
        }

        String jsonStr = JsonUtil.objectToJson(request);
        logger.debug("jsonStr: {}", jsonStr);
        Capsule capsule = new Capsule();
        capsule.setUserId(Integer.parseInt(userId));
        capsule.updateCurrentMessage(jsonStr);
        capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_MBK);
        capsule.setMessageId(UUID.randomUUID().toString());
        capsule.setProperty(Constants.OPERATION_NAME, "EPullAutoEnrollmentLogic");

        Capsule response;
        try {
            logger.debug("capsule before: {}", capsule);
            response = ePullAutoEnrollmentLogic.executeBusinessLogic(capsule);
            logger.debug("Response from ePullAutoEnrollmentLogic: {}" + response);
        } catch (Exception ex) {
            logger.error("Error when calling to EPullAutoEnrollmentLogic SDK.", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (response.isOperationSuccessful() == null || !response.isOperationSuccessful()) {
            logger.error("Error executing epull auto enrollment logic.");
            throw new EpullEnrollmentSDKException();
        }

        logger.debug("EPullAutoEnrollmentLogic process completed.");
        return new ResponseEntity<>("Success", HttpStatus.OK);
    }

    private boolean validateRequestPayload(EPullAutoEnrollmentRequest request) {
        // validate list is not null
        if(Objects.isNull(request.getSavings()) &&
                Objects.isNull(request.getCurrents()) &&
                Objects.isNull(request.getMca()) &&
                Objects.isNull(request.getTermDeposits()) &&
                Objects.isNull(request.getMortgages()) &&
                Objects.isNull(request.getHirePurchases()) &&
                Objects.isNull(request.getPersonalFinances()) &&
                Objects.isNull(request.getAsb()) &&
                Objects.isNull(request.getCreditCards()) &&
                Objects.isNull(request.getPrepaidCards())) {
            return true;
        }

        // validate list is not empty
        if(request.getSavings().isEmpty() &&
                request.getCurrents().isEmpty() &&
                request.getMca().isEmpty() &&
                request.getTermDeposits().isEmpty() &&
                request.getMortgages().isEmpty() &&
                request.getHirePurchases().isEmpty() &&
                request.getPersonalFinances().isEmpty() &&
                request.getAsb().isEmpty() &&
                request.getCreditCards().isEmpty() &&
                request.getPrepaidCards().isEmpty()) {
            return true;
        }

        return false;
    }
}
