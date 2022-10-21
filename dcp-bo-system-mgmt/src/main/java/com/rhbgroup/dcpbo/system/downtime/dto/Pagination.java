
package com.rhbgroup.dcpbo.system.downtime.dto;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Faizal Musa
 */
@Setter
@Getter
public class Pagination {
    
    /**
     * "pagination":{ 
      "recordCount":"2",
      "pageNo":"1",
      "totalPageNo":"1",
      "pageIndicator":"L"
   }
     * 
     */
    private String pageIndicator;
    private int recordCount;
    private int pageNo;
    private int totalPageNo;

    private Pagination(Builder b) {
       
        pageIndicator = b.pageIndicator;
        recordCount = b.recordCount;
        pageNo = b.pageNo;
        totalPageNo = b.totalPageNo;
    }   
    
    public static class Builder{
    
        private String pageIndicator;
        private int recordCount;
        private int pageNo;
        private int totalPageNo;
    
        public Builder(int totalPageNo){
            this.totalPageNo = totalPageNo;
        }
        
        public Builder pageNo(int pageNo){
            this.pageNo = pageNo;
            return this;
        }
        
        public Builder pageIndicator(String pageIndicator){
            this.pageIndicator = pageIndicator;
            return this;
        }
        
        public Builder recordCount(int recordCount){
            
            this.recordCount = recordCount;
            return this;
        }
        
        public Pagination build(){
            
            return new Pagination(this);
        }
    }

    public static Builder empty(){
        return new Builder(0).pageIndicator("L").pageNo(0).recordCount(0);
    }
    
    public static Builder of(int totalPageNo){
        return new Builder(totalPageNo);
    }
}
