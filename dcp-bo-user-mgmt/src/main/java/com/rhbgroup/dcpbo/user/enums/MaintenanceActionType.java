package com.rhbgroup.dcpbo.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum MaintenanceActionType {
	ADD("ADD"),
    EDIT("UPDATE"),
    DELETE("DELETE"),
    STATUS_PENDING_APPROVAL("P"),
    STATUS_APPROVED("A"),
    STATUS_REJECTED("R"),
    YES("Y"),
    NO("N"),
    USER_STATUS_ID_ACTIVE("A"),
    USER_STATUS_ID_INACTIVE("I"),
    USER_STATUS_ID_DELETED("D"),
    USER_STATUS_ID_LOCKED("L"),
	USER_USER_GROUP_STATUS_ACTIVE("A"),
	USER_USER_GROUP_STATUS_DELETED("D"),
    USER_APPROVAL_STATE_A("A"),
    USER_APPROVAL_STATE_B("B"),
    USERGROUP_APPROVAL_STATE_A("A"),
    USERGROUP_APPROVAL_STATE_B("B"),
	ACCESS_TYPE_MAKER("M"),
	ACCESS_TYPE_CHECKER("C"),
	ACCESS_TYPE_INQUIRER("I"),
    USER_GROUP_STATUS_ACTIVE("A"),
    USER_GROUP_STATUS_DELETED("D");
    @Getter private final String value;
}
