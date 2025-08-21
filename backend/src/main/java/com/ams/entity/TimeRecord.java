package com.ams.entity;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.ams.entity.enums.AttendanceStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "time_records", indexes = {
    @Index(name = "idx_time_record_user_date", columnList = "user_id, record_date", unique = true),
    @Index(name = "idx_time_record_date", columnList = "record_date"),
    @Index(name = "idx_time_record_status", columnList = "status")
})
public class TimeRecord extends BaseEntity {

    @NotNull(message = "ユーザーは必須です")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "勤務日は必須です")
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "clock_in")
    private LocalDateTime clockIn;

    @Column(name = "clock_out")
    private LocalDateTime clockOut;

    @Column(name = "break_start")
    private LocalDateTime breakStart;

    @Column(name = "break_end")
    private LocalDateTime breakEnd;

    @Column(name = "total_hours", precision = 4, scale = 2)
    private BigDecimal totalHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status = AttendanceStatus.ABSENT;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Default constructor
    public TimeRecord() {
    }

    // Constructor with essential fields
    public TimeRecord(User user, LocalDate recordDate) {
        this.user = user;
        this.recordDate = recordDate;
    }

    // Business methods
    public void clockIn(LocalDateTime clockInTime) {
        this.clockIn = clockInTime;
        this.status = AttendanceStatus.PRESENT;
        calculateTotalHours();
    }

    public void clockOut(LocalDateTime clockOutTime) {
        this.clockOut = clockOutTime;
        calculateTotalHours();
    }

    public void startBreak(LocalDateTime breakStartTime) {
        this.breakStart = breakStartTime;
        calculateTotalHours();
    }

    public void endBreak(LocalDateTime breakEndTime) {
        this.breakEnd = breakEndTime;
        calculateTotalHours();
    }

    public void calculateTotalHours() {
        if (clockIn == null) {
            totalHours = BigDecimal.ZERO;
            return;
        }

        LocalDateTime endTime = clockOut != null ? clockOut : LocalDateTime.now();
        Duration workDuration = Duration.between(clockIn, endTime);

        // Subtract break time if both start and end are present
        if (breakStart != null && breakEnd != null) {
            Duration breakDuration = Duration.between(breakStart, breakEnd);
            workDuration = workDuration.minus(breakDuration);
        }

        // Convert to hours with 2 decimal places
        long totalMinutes = workDuration.toMinutes();
        if (totalMinutes < 0) {
            totalMinutes = 0;
        }
        
        totalHours = BigDecimal.valueOf(totalMinutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP);
    }

    public boolean isClockingIn() {
        return clockIn != null && clockOut == null;
    }

    public boolean isOnBreak() {
        return breakStart != null && breakEnd == null;
    }

    public boolean isCompleted() {
        return clockIn != null && clockOut != null;
    }

    public Duration getBreakDuration() {
        if (breakStart != null && breakEnd != null) {
            return Duration.between(breakStart, breakEnd);
        }
        return Duration.ZERO;
    }

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        calculateTotalHours();
    }

    public LocalDateTime getClockOut() {
        return clockOut;
    }

    public void setClockOut(LocalDateTime clockOut) {
        this.clockOut = clockOut;
        calculateTotalHours();
    }

    public LocalDateTime getBreakStart() {
        return breakStart;
    }

    public void setBreakStart(LocalDateTime breakStart) {
        this.breakStart = breakStart;
        calculateTotalHours();
    }

    public LocalDateTime getBreakEnd() {
        return breakEnd;
    }

    public void setBreakEnd(LocalDateTime breakEnd) {
        this.breakEnd = breakEnd;
        calculateTotalHours();
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

    @Override
    public String toString() {
        return "TimeRecord{" +
                "recordDate=" + recordDate +
                ", clockIn=" + clockIn +
                ", clockOut=" + clockOut +
                ", totalHours=" + totalHours +
                ", status=" + status +
                ", id=" + getId() +
                '}';
    }
}