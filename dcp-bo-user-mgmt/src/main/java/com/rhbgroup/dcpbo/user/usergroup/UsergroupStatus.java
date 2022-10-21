package com.rhbgroup.dcpbo.user.usergroup;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum UsergroupStatus {
    ACTIVE("A"),
    DELETED("D");

    @Getter private final String value;
}