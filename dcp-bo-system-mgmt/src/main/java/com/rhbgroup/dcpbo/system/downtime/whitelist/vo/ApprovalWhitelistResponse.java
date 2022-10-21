/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rhbgroup.dcpbo.system.downtime.whitelist.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.system.common.BoData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author faizal.musa
 */

@Setter
@Getter
@ToString
@JsonInclude
public class ApprovalWhitelistResponse implements BoData {
    
    private int approvalId;
    private String actionType;
    private String reason;
    private String createdTime;
    private String createdBy;
    private String updatedTime;
    private String updatedBy;
    private String creatorName;
    private String isCreator;
    
    private String name;
    private int userId;
    private String mobileNo;
    private String username;
    private String idNo;
    private String idType;
    private String type;
    private String cisNo;
    private String approvalStatus;
    
    private ApprovalWhitelistResponse(Builder b) {
        
        this.approvalId = b.approvalId;
        this.userId = b.userId;
        this.name = b.name;
        this.mobileNo = b.mobileNo;
        this.username = b.username;
        this.idNo = b.idNo;
        this.idType = b.idType;
        this.type = b.type;
        this.cisNo = b.cisNo;
        this.creatorName = b.creatorName;
        this.isCreator = b.isCreator;
        this.actionType = b.actionType;
        this.reason = b.reason;
        this.createdTime = b.createdTime;
        this.createdBy = b.createdBy;
        this.updatedTime = b.updatedTime;
        this.updatedBy = b.updatedBy;
        this.approvalStatus = b.approvalStatus;
        
    }
    
    public static class Builder{
        
        //BO_APPROVAL
        private final int approvalId;
        private String actionType;
        private String reason;
        private String createdTime;
        private String createdBy;
        private String updatedTime;
        private String updatedBy;
        private String isCreator;
        
        //BO_USER
        private String creatorName;
        
        //BO_SM_DOWNTIME_WHITELIST
        private int userId;
        private String name;
        private String mobileNo;
        private String username;
        private String idNo;
        private String idType;
        private String type;
        private String cisNo;
        private String approvalStatus;
        
        public Builder(int approvalId){
            this.approvalId = approvalId;
        }
        
        public Builder userId(int userId){
            this.userId = userId;
            return this;
        }
        
        public Builder name(String name){
            this.name = name;
            return this;
        }
        
        public Builder mobileNo(String mobileNo){
            this.mobileNo = mobileNo;
            return this;
        }
        
        public Builder username(String username){
            this.username = username;
            return this;
        }
        
        public Builder idNo(String idNo){
            this.idNo = idNo;
            return this;
        }
        
        public Builder idType(String idType){
            this.idType = idType;
            return this;
        }
        
        public Builder type(String type){
            this.type = type;
            return this;
        }
        
        public Builder cisNo(String cisNo){
            this.cisNo = cisNo;
            return this;
        }        
                
        public Builder actionType(String actionType){
            this.actionType = actionType;
            return this;
        }
        
        public Builder reason(String reason){
            this.reason = reason;
            return this;
        }
        
        public Builder createdTime(String createdTime){
            this.createdTime = createdTime;
            return this;
        }
        
        public Builder createdBy(String createdBy){
            this.createdBy = createdBy;
            return this;
        }
        
        public Builder updatedTime(String updatedTime){
            this.updatedTime = updatedTime;
            return this;
        }
        
        public Builder updatedBy(String updatedBy){
            this.updatedBy = updatedBy;
            return this;
        }
        
        public Builder isCreator(String isCreator){
            this.isCreator = isCreator;
            return this;
        }
        
        public Builder creatorName(String creatorName){
            
            this.creatorName = creatorName;
            return this;
        }
        
       public Builder approvalStatus(String approvalStatus){
            
            this.approvalStatus = approvalStatus;
            return this;
        }
       
        public ApprovalWhitelistResponse build(){
        
            return new ApprovalWhitelistResponse(this);
        }
    }

    public static Builder of(int approvalId){
        return new Builder(approvalId);
    }
}
