package com.ams.entity.enums;

public enum UserRole {
    EMPLOYEE("従業員"),
    MANAGER("管理者");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}