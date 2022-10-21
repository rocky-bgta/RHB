
package com.rhbgroup.dcpbo.system.downtime.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author faizal.musa
 */
@Getter
@Setter
@ToString
@JsonInclude
public class UpdateDowntimeAdhocApprovalRequestVo {
 
    private String reason;
}
