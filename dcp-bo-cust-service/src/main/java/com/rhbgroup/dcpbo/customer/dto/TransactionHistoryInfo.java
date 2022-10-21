package com.rhbgroup.dcpbo.customer.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rhbgroup.dcp.model.Capsule;
import com.rhbgroup.dcp.transformer.ruledriven.util.GSONUtil;
import com.rhbgroup.dcpbo.customer.contract.BoData;
import com.rhbgroup.dcpbo.customer.contract.CapsuleToDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class TransactionHistoryInfo implements BoData, CapsuleToDto {

    private Pagination pagination;
    private List<TransactionHistory> transactionHistory;

    public TransactionHistoryInfo() {
        this.pagination = new Pagination();
        transactionHistory = new ArrayList<>();
    }

    public void addTransactionHistory(TransactionHistory transactionHistoryItem) {
        transactionHistory.add(transactionHistoryItem);
    }

    @Override
    public BoData convert(Capsule capsule) {
        String responseString = capsule.getCurrentMessage();
        JsonObject jsonObject = GSONUtil.convertToGson(responseString);
        JsonObject paginationJsonObj = jsonObject.get("pagination").getAsJsonObject();
        if(paginationJsonObj.get("firstKey") != null) {
        	this.pagination.setFirstKey(paginationJsonObj.get("firstKey").getAsJsonPrimitive().getAsString());
        } else {
        	this.pagination.setFirstKey("");
        }
        if(paginationJsonObj.get("lastKey") != null) {
        	this.pagination.setLastKey(paginationJsonObj.get("lastKey").getAsJsonPrimitive().getAsString());
        } else {
        	this.pagination.setLastKey("");
        }
        this.pagination.setIsLastPage(paginationJsonObj.get("isLastPage").getAsJsonPrimitive().getAsBoolean());
        this.pagination.setPageCounter(paginationJsonObj.get("pageCounter").getAsJsonPrimitive().getAsInt());

        if(jsonObject.has("transactionHistory")){
            JsonArray transactionHistoryArrays = jsonObject.getAsJsonArray("transactionHistory");
            transactionHistoryArrays.forEach(transactionItem -> {
                JsonObject transactionHistoryJsonItem = transactionItem.getAsJsonObject();
                TransactionHistory transactionHistoryItem = new TransactionHistory();
                transactionHistoryItem.setTxnDate(transactionHistoryJsonItem.get("txnDate").getAsJsonPrimitive().getAsString());
                transactionHistoryItem.setDescription(transactionHistoryJsonItem.get("description").getAsJsonPrimitive().getAsString());
                transactionHistoryItem.setAmount(transactionHistoryJsonItem.get("amount").getAsJsonPrimitive().getAsDouble());
                this.transactionHistory.add(transactionHistoryItem);
            });
        }

        return this;
    }

}
