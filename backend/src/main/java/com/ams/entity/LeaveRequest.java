package com.ams.entity;

import com.ams.entity.enums.LeaveType;
import com.ams.entity.enums.RequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests", indexes = {
    @Index(name = "idx_leave_request_user", columnList = "user_id"),
    @Index(name = "idx_leave_request_status", columnList = "status"),
    @Index(name = "idx_leave_request_dates", columnList = "start_date, end_date"),
    @Index(name = "idx_leave_request_approved_by", columnList = "approved_by")
})
public class LeaveRequest extends BaseEntity {

    @NotNull(message = "ユーザーは必須です")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "休暇種別は必須です")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LeaveType type;

    @NotNull(message = "開始日は必須です")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "終了日は必須です")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotBlank(message = "理由は必須です")
    @Size(max = 1000, message = "理由は1000文字以内で入力してください")
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
    public LeaveRequest() {
    }

    // Constructor with essential fields
    public LeaveRequest(User user, LeaveType type, LocalDate startDate, LocalDate endDate, String reason) {
        this.user = user;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public long getDurationDays() {
        return startDate.datesUntil(endDate.plusDays(1)).count();
    }

    // Getters and Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LeaveType getType() {
        return type;
    }

    public void setType(LeaveType type) {
        this.type = type;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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
        return "LeaveRequest{" +
                "type=" + type +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                ", id=" + getId() +
                '}';
    }
}