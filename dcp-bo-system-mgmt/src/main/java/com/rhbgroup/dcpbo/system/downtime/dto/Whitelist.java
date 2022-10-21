
package com.rhbgroup.dcpbo.system.downtime.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Faizal Musas
 */
@Setter
@Getter
@JsonInclude
public class Whitelist {
    
    private int id;
    private int userId;
    private String name;
    private String mobileNo;
    private String username;
    private String idNo;
    private String idType;
    private String cisNo;

    private Whitelist(Builder b) {
        id = b.id;
        userId = b.userId;
        name = b.name;
        mobileNo = b.mobileNo;
        username = b.username;
        idNo = b.idNo;
        idType = b.idType;
        cisNo = b.cisNo;
    }
    
    public static class Builder{
    
        private int id;
        private int userId;
        private String name;
        private String mobileNo;
        private String username;
        private String idNo;
        private String idType;
        private String cisNo;
    
        /**
         * Downtime ID
         * @param id Downtime ID
         */
        public Builder(int id){
            this.id = id;
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
        
        public Builder cisNo(String cisNo){
            this.cisNo = cisNo;
            return this;
        }
        
        public Whitelist build(){
            
            return new Whitelist(this);
        }
    }
}
