package com.ams.dto.time;

import com.ams.entity.enums.AttendanceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TimeRecordDto {

    private UUID id;
    private UUID userId;
    private String userName;
    private String employeeId;
    private LocalDate recordDate;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private LocalDateTime breakStart;
    private LocalDateTime breakEnd;
    private BigDecimal totalHours;
    private AttendanceStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Calculated fields
    private boolean isClockingIn;
    private boolean isOnBreak;
    private boolean isCompleted;

    // Default constructor
    public TimeRecordDto() {
    }

    // Constructor with essential fields
    public TimeRecordDto(UUID id, LocalDate recordDate, AttendanceStatus status) {
        this.id = id;
        this.recordDate = recordDate;
        this.status = status;
    }

    // Business methods
    public boolean canClockIn() {
        return clockIn == null;
    }

    public boolean canClockOut() {
        return clockIn != null && clockOut == null;
    }

    public boolean canStartBreak() {
        return clockIn != null && clockOut == null && !isOnBreak;
    }

    public boolean canEndBreak() {
        return isOnBreak;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public LocalDateTime getClockIn() {
        return clockIn;
    }

    public void setClockIn(LocalDateTime clockIn) {
        this.clockIn = clockIn;
        updateCalculatedFields();
    }

    public LocalDateTime getClockOut() {
        return clockOut;
    }

    public void setClockOut(LocalDateTime clockOut) {
        this.clockOut = clockOut;
        updateCalculatedFields();
    }

    public LocalDateTime getBreakStart() {
        return breakStart;
    }

    public void setBreakStart(LocalDateTime breakStart) {
        this.breakStart = breakStart;
        updateCalculatedFields();
    }

    public LocalDateTime getBreakEnd() {
        return breakEnd;
    }

    public void setBreakEnd(LocalDateTime breakEnd) {
        this.breakEnd = breakEnd;
        updateCalculatedFields();
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(BigDecimal totalHours) {
        this.totalHours = totalHours;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isClockingIn() {
        return isClockingIn;
    }

    public void setClockingIn(boolean clockingIn) {
        isClockingIn = clockingIn;
    }

    public boolean isOnBreak() {
        return isOnBreak;
    }

    public void setOnBreak(boolean onBreak) {
        isOnBreak = onBreak;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    private void updateCalculatedFields() {
        this.isClockingIn = clockIn != null && clockOut == null;
        this.isOnBreak = breakStart != null && breakEnd == null;
        this.isCompleted = clockIn != null && clockOut != null;
    }

    @Override
    public String toString() {
        return "TimeRecordDto{" +
                "id=" + id +
                ", recordDate=" + recordDate +
                ", status=" + status +
                ", totalHours=" + totalHours +
                ", isClockingIn=" + isClockingIn +
                ", isOnBreak=" + isOnBreak +
                '}';
    }
}