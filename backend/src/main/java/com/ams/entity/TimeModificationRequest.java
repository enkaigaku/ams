package com.ams.entity;

import com.ams.entity.enums.RequestStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_modification_requests", indexes = {
    @Index(name = "idx_time_mod_request_user", columnList = "user_id"),
    @Index(name = "idx_time_mod_request_status", columnList = "status"),
    @Index(name = "idx_time_mod_request_date", columnList = "request_date"),
    @Index(name = "idx_time_mod_request_approved_by", columnList = "approved_by")
})
public class TimeModificationRequest extends BaseEntity {

    @NotNull(message = "ユーザーは必須です")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @NotNull(message = "対象日は必須です")
    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "original_clock_in")
    private LocalDateTime originalClockIn;

    @Column(name = "original_clock_out")
    private LocalDateTime originalClockOut;

    @Column(name = "requested_clock_in")
    private LocalDateTime requestedClockIn;

    @Column(name = "requested_clock_out")
    private LocalDateTime requestedClockOut;

    @NotBlank(message = "修正理由は必須です")
    @Size(max = 1000, message = "修正理由は1000文字以内で入力してください")
    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "approved_by")
    private String approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", referencedColumnName = "employee_id", insertable = false, updatable = false)
    private User approver;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    // Default constructor
    public TimeModificationRequest() {
    }

    // Constructor with essential fields
    public TimeModificationRequest(User user, LocalDate requestDate, String reason) {
        this.user = user;
        this.requestDate = requestDate;
        this.reason = reason;
    }

    // Business methods
    public void approve(String approverEmployeeId) {
        this.status = RequestStatus.APPROVED;
        this.approvedBy = approverEmployeeId;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = null;
    }

    public void reject(String approverEmployeeId, String rejectionReason) {
        this.status = RequestStatus.REJECTED;
        this.approvedBy = approverEmployeeId;
        this.approvedAt = LocalDateTime.now();
        this.rejectionReason = rejectionReason;
    }

    public boolean isPending() {
        return RequestStatus.PENDING.equals(status);
    }

    public boolean isApproved() {
        return RequestStatus.APPROVED.equals(status);
    }

    public boolean isRejected() {
        return RequestStatus.REJECTED.equals(status);
    }

    public boolean hasClockInModification() {
        return requestedClockIn != null;
    }

    public boolean hasClockOutModification() {
        return requestedClockOut != null;
    }

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
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

    @Override
    public String toString() {
        return "TimeModificationRequest{" +
                "requestDate=" + requestDate +
                ", requestedClockIn=" + requestedClockIn +
                ", requestedClockOut=" + requestedClockOut +
                ", status=" + status +
                ", id=" + getId() +
                '}';
    }
}