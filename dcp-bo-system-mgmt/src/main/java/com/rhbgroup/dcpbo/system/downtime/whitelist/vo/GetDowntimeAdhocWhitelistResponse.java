
package com.rhbgroup.dcpbo.system.downtime.whitelist.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rhbgroup.dcpbo.system.common.BoData;
import com.rhbgroup.dcpbo.system.downtime.dto.AuditPagination;
import com.rhbgroup.dcpbo.system.downtime.dto.Whitelist;
import com.rhbgroup.dcpbo.system.downtime.dto.Pagination;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author faizal.musa
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@JsonInclude
public class GetDowntimeAdhocWhitelistResponse implements BoData{
    
    /*
    
    { 
   "whitelist":[ 
      { 
         "id":1,
         "userId": 111,
         "name":"OG HESWOH NO@ G HTI EI WOHGN",
         "mobileNo":"6018661600911",
         "username":"Robin09",
         "idNo":"460214035004",
         "idType":"MK",
         "cisNo" : "00000000407533"
      },
       { 
         "id":2,
         "userId": 112,
         "name":"Azizu Fify",
         "mobileNo":"6018661600123",
         "username":"Batman01",
         "idNo":"800214035004",
         "idType":"MK",
         "cisNo" : "00000000407123"
      }
   ],
   "pagination":{ 
      "recordCount":"2",
      "pageNo":"1",
      "totalPageNo":"1",
      "pageIndicator":"L"
   }
}
    
    */
    private List<Whitelist> whitelist;
    
    private Pagination pagination;
    
}
