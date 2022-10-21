package com.rhbgroup.dcp.bo.batch.job.repository;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.PrepaidReloadFileFromIBK;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class PrepaidReloadFileFromIBKRepositoryImpl extends BaseRepositoryImpl {
    static final Logger logger = Logger.getLogger(PrepaidReloadFileFromIBKRepositoryImpl.class);
    public Boolean addToStaging(PrepaidReloadFileFromIBK prepaidReloadFileFromIBK) {
        try {

            getJdbcTemplate().update("INSERT INTO TBL_BATCH_STAGED_IBK_PREPAID_RELOAD_EXTRACTION (job_execution_id,file_name,payment_type,txn_time,ref_no,host_ref_no,mobile_no,prepaid_product_code,amount,txn_status,created_time) values (?,?,?,?,?,?,?,?,?,?,?)"
                    , new Object[]{
                            prepaidReloadFileFromIBK.getJobExecutionId()
                            , prepaidReloadFileFromIBK.getFileName()
                            , prepaidReloadFileFromIBK.getPaymentType()
                            , prepaidReloadFileFromIBK.getTxnTime()
                            , prepaidReloadFileFromIBK.getRefNo()
                            , prepaidReloadFileFromIBK.getHostRefNo()
                            , prepaidReloadFileFromIBK.getMobileNo()
                            , prepaidReloadFileFromIBK.getPrepaidProductCode()
                            , prepaidReloadFileFromIBK.getAmount()
                            , prepaidReloadFileFromIBK.getTxnStatus()
                            , prepaidReloadFileFromIBK.getCreatedTime()

                    });
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }
}
