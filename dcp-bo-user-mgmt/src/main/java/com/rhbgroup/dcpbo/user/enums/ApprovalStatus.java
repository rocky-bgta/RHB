package com.rhbgroup.dcpbo.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ApprovalStatus {
    REJECTED("R"),
    APPROVED("A"),
    PENDING_APPROVAL("P"),
    WRITTEN_Y("Y"),
    WRITTEN_N("N");

    @Getter
    private final String value;
}
