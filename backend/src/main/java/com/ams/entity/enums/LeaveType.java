package com.ams.entity.enums;

public enum LeaveType {
    ANNUAL("年次休暇"),
    SPECIAL("特別休暇"),
    MATERNITY("産休"),
    PATERNITY("育休"),
    PAID("有給休暇"),
    SICK("病気休暇"),
    PERSONAL("私用休暇");

    private final String displayName;

    LeaveType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}