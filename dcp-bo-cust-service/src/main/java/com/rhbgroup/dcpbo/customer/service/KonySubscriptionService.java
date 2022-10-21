package com.rhbgroup.dcpbo.customer.service;

import com.rhbgroup.dcp.kpns.connector.PushNotificationAdaptor;
import com.rhbgroup.dcp.kpns.model.DcpDeactivateAudienceRequest;
import com.rhbgroup.dcp.kpns.model.DcpDeleteAudienceRequest;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.model.Constants;
import com.rhbgroup.dcp.model.SourceChannel;
import com.rhbgroup.dcpbo.customer.exception.CommonException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KonySubscriptionService {

    private static Logger logger = LogManager.getLogger(KonySubscriptionService.class);

    private static final String DELETE_DEVICE_OPERATION_NAME = "BODeleteDevice";

    PushNotificationAdaptor pnAdaptor = new PushNotificationAdaptor();

    public void deleteSubscriber(String subscriberId, Integer userId, String cisNo) {
        logger.info("Proceeding to call KES subscriber service, Subscriber ID : {}", subscriberId);
        try {
            Capsule capsule = new Capsule();
            capsule.setProperty(Constants.SOURCE_CHANNEL, SourceChannel.DCP_BACKOFFICE);
            capsule.setMessageId(UUID.randomUUID().toString());
            capsule.setUserId(userId);
            capsule.setCisNumber(cisNo);
            capsule.setProperty(Constants.OPERATION_NAME, DELETE_DEVICE_OPERATION_NAME);
            DcpDeactivateAudienceRequest dcpDeactivateAudienceRequest = new DcpDeactivateAudienceRequest();
            dcpDeactivateAudienceRequest.setKsid(subscriberId);
            if (pnAdaptor.deactivateAudience(dcpDeactivateAudienceRequest, capsule)) {
                DcpDeleteAudienceRequest deleteAudienceRequest = new DcpDeleteAudienceRequest();
                List<String> ksids = new ArrayList<>(1);
                ksids.add(subscriberId);
                deleteAudienceRequest.setKsids(ksids);
                if (!pnAdaptor.deleteAudience(deleteAudienceRequest, capsule)) {
                    throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Unsuccesful KES Delete Subscriber call for subscriberID : " + subscriberId);
                }
            } else {
                throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Unsuccesful KES Inactivate Subscriber call for subscriberID : " + subscriberId);
            }
        } catch (CommonException ex) {
            logger.error(ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            throw new CommonException(CommonException.GENERIC_ERROR_CODE, "Unsuccesful KES Subscriber service call for subscriberID : " + subscriberId);
        }
    }
}
