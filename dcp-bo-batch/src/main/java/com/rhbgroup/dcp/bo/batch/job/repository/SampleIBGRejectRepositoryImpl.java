package com.rhbgroup.dcp.bo.batch.job.repository;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.rhbgroup.dcp.bo.batch.framework.repository.BaseRepositoryImpl;
import com.rhbgroup.dcp.bo.batch.job.model.SampleIBGReject;

@Component
public class SampleIBGRejectRepositoryImpl extends BaseRepositoryImpl {

    static final Logger logger = Logger.getLogger(SampleIBGRejectRepositoryImpl.class);

    public Boolean addSampleIBGRejectStaging(SampleIBGReject sampleIBGReject) {
        String logMsg="";
        try {
            getJdbcTemplate().update("INSERT INTO TBL_BATCH_SAMPLE_STAGING (date,teller,trace,ref1,name,amount,reject_Code,account_Number,bene_Name,bene_Account, job_execution_id) values (?,?,?,?,?,?,?,?,?,?,?)"
                    , new Object[]{
                    sampleIBGReject.getDate()
                    ,sampleIBGReject.getTeller()
                            ,sampleIBGReject.getTrace()
                            ,sampleIBGReject.getRef1()
                            ,sampleIBGReject.getName()
                            ,sampleIBGReject.getAmount()
                            ,sampleIBGReject.getRejectCode()
                            ,sampleIBGReject.getAccountNumber()
                            ,sampleIBGReject.getBeneName()
                            ,sampleIBGReject.getBeneAccount()
                            ,sampleIBGReject.getJobExecutionId()
            });
            return true;
        } catch (Exception e) {
            logMsg=String.format("exception while add Batch Staged Sample Staging date=%s, teller=%s, trace=%s, ref1=%s, name=%s," +
                            "amount=%s, rejectCode=%s, accountNumber=%s, beneName=%s, beneAccount=%s, jobExecutionId=%s" ,
                    sampleIBGReject.getDate(), sampleIBGReject.getTeller(), sampleIBGReject.getTrace(), sampleIBGReject.getRef1(),
                    sampleIBGReject.getName(), sampleIBGReject.getAmount(), sampleIBGReject.getRejectCode(), sampleIBGReject.getAccountNumber(),
                    sampleIBGReject.getBeneName(), sampleIBGReject.getBeneAccount(), sampleIBGReject.getJobExecutionId()) ;
            logger.info(logMsg);
            logger.error(e);
            return false;
        }
    }
}
