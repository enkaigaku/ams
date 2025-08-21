package com.ams.dto.request;

import com.ams.entity.enums.RequestStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TimeModificationRequestDto {

    private UUID id;
    private UUID userId;
    private String userName;
    private String employeeId;
    private LocalDate requestDate;
    private LocalDateTime originalClockIn;
    private LocalDateTime originalClockOut;
    private LocalDateTime requestedClockIn;
    private LocalDateTime requestedClockOut;
    private String reason;
    private RequestStatus status;
    private String approvedBy;
    private String approverName;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public TimeModificationRequestDto() {
    }

    // Constructor with essential fields
    public TimeModificationRequestDto(UUID id, LocalDate requestDate, RequestStatus status) {
        this.id = id;
        this.requestDate = requestDate;
        this.status = status;
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

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getOriginalClockIn() {
        return originalClockIn;
    }

    public void setOriginalClockIn(LocalDateTime originalClockIn) {
        this.originalClockIn = originalClockIn;
    }

    public LocalDateTime getOriginalClockOut() {
        return originalClockOut;
    }

    public void setOriginalClockOut(LocalDateTime originalClockOut) {
        this.originalClockOut = originalClockOut;
    }

    public LocalDateTime getRequestedClockIn() {
        return requestedClockIn;
    }

    public void setRequestedClockIn(LocalDateTime requestedClockIn) {
        this.requestedClockIn = requestedClockIn;
    }

    public LocalDateTime getRequestedClockOut() {
        return requestedClockOut;
    }

    public void setRequestedClockOut(LocalDateTime requestedClockOut) {
        this.requestedClockOut = requestedClockOut;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
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

    @Override
    public String toString() {
        return "TimeModificationRequestDto{" +
                "id=" + id +
                ", requestDate=" + requestDate +
                ", status=" + status +
                ", requestedClockIn=" + requestedClockIn +
                ", requestedClockOut=" + requestedClockOut +
                '}';
    }
}