package com.rhbgroup.dcp.bo.batch.job.fieldsetmapper;

import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxn;
import com.rhbgroup.dcp.bo.batch.job.model.BatchStagedIBKPaymentTxnDetail;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BatchStagedDynamicIBKPaymentTxnDetailMapper implements FieldSetMapper<BatchStagedIBKPaymentTxn> {
	
	@Override
    public BatchStagedIBKPaymentTxn mapFieldSet(FieldSet fieldSet) throws BindException {
    	BatchStagedIBKPaymentTxnDetail batchStagedIBKPaymentTxnDetail = new BatchStagedIBKPaymentTxnDetail();
		Supplier<Stream<String>> names= () -> Arrays.stream(fieldSet.getNames());

		batchStagedIBKPaymentTxnDetail.setBillerRefNo1(getNames( fieldSet, names,"biller_ref_no1", ""));
    	batchStagedIBKPaymentTxnDetail.setBillerRefNo2(getNames( fieldSet, names,"biller_ref_no2", ""));
    	batchStagedIBKPaymentTxnDetail.setBillerRefNo3(getNames( fieldSet, names,"biller_ref_no3", "") );
    	batchStagedIBKPaymentTxnDetail.setTxnAmount(getNames( fieldSet, names,"txn_amount", ""));
    	batchStagedIBKPaymentTxnDetail.setTxnDate(getNames( fieldSet, names,"txn_date", ""));
    	batchStagedIBKPaymentTxnDetail.setTxnDescription(getNames( fieldSet, names,"txn_description", ""));
    	batchStagedIBKPaymentTxnDetail.setTxnId(getTxnId(fieldSet,names));
    	batchStagedIBKPaymentTxnDetail.setTxnTime(getNames( fieldSet, names,"txn_time", "") );
    	batchStagedIBKPaymentTxnDetail.setTxnType(getNames( fieldSet, names,"txn_type", ""));

//    	newly added fields
		batchStagedIBKPaymentTxnDetail.setBillerRefNo4(getNames( fieldSet, names,"biller_ref_no4", ""));
		batchStagedIBKPaymentTxnDetail.setIdNo(getNames( fieldSet, names,"id_no", ""));
		batchStagedIBKPaymentTxnDetail.setUserAddress1(getNames( fieldSet, names,"user_address1", ""));
		batchStagedIBKPaymentTxnDetail.setUserAddress2(getNames( fieldSet, names,"user_address2", ""));
		batchStagedIBKPaymentTxnDetail.setUserAddress3(getNames( fieldSet, names,"user_address3", ""));
		batchStagedIBKPaymentTxnDetail.setPolicyNo(getNames( fieldSet, names,"policy_no", ""));

		batchStagedIBKPaymentTxnDetail.setUserAddress4(getNames( fieldSet, names,"user_address4", ""));
		batchStagedIBKPaymentTxnDetail.setUserState( getNames( fieldSet, names,"user_state", ""));
		batchStagedIBKPaymentTxnDetail.setUserCity(getNames( fieldSet, names,"user_city", ""));

		batchStagedIBKPaymentTxnDetail.setUserPostcode(getNames( fieldSet, names,"user_postcode", ""));
		batchStagedIBKPaymentTxnDetail.setUserCountry(getNames( fieldSet, names,"user_country", ""));

		batchStagedIBKPaymentTxnDetail.setBillerAccountNo(getNames( fieldSet, names,"biller_account_no", ""));
		batchStagedIBKPaymentTxnDetail.setBillerAccountName(getNames( fieldSet, names,"biller_account_no", "NoName"));


		return batchStagedIBKPaymentTxnDetail;
    }

	public String getTxnId(FieldSet fieldSet,Supplier<Stream<String>> names){
		if(names.get().anyMatch("txn_id"::equals)){
		return	getNames( fieldSet, names,"txn_id", "");
		}else if(names.get().anyMatch("ref_id"::equals)){
			return	getNames( fieldSet, names,"ref_id", "");
		}
		return "";
	}
    public String getNames(FieldSet fieldSet,Supplier<Stream<String>> names,String name,String defaultName){
		return names.get().anyMatch(name::equals) ?fieldSet.readString(name):defaultName;
	}
}
