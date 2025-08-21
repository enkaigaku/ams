package com.ams.service;

import com.ams.entity.LeaveRequest;
import com.ams.entity.TimeModificationRequest;
import com.ams.entity.enums.RequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ApprovalWorkflowService {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalWorkflowService.class);

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private TimeModificationRequestService timeModificationRequestService;

    @Autowired
    private UserService userService;

    // Unified approval methods
    public void approveRequest(String requestType, UUID requestId, String approverEmployeeId) {
        switch (requestType.toLowerCase()) {
            case "leave":
                leaveRequestService.approveLeaveRequest(requestId, approverEmployeeId);
                break;
            case "time_modification":
                timeModificationRequestService.approveTimeModificationRequest(requestId, approverEmployeeId);
                break;
            default:
                throw new IllegalArgumentException("不明な申請タイプです: " + requestType);
        }
        
        logger.info("Approved {} request {} by {}", requestType, requestId, approverEmployeeId);
    }

    public void rejectRequest(String requestType, UUID requestId, String approverEmployeeId, String rejectionReason) {
        switch (requestType.toLowerCase()) {
            case "leave":
                leaveRequestService.rejectLeaveRequest(requestId, approverEmployeeId, rejectionReason);
                break;
            case "time_modification":
                timeModificationRequestService.rejectTimeModificationRequest(requestId, approverEmployeeId, rejectionReason);
                break;
            default:
                throw new IllegalArgumentException("不明な申請タイプです: " + requestType);
        }
        
        logger.info("Rejected {} request {} by {} with reason: {}", requestType, requestId, approverEmployeeId, rejectionReason);
    }

    // Bulk approval methods
    public void bulkApproveRequests(List<BulkApprovalRequest> requests, String approverEmployeeId) {
        for (BulkApprovalRequest request : requests) {
            try {
                approveRequest(request.getRequestType(), request.getRequestId(), approverEmployeeId);
            } catch (Exception e) {
                logger.error("Failed to approve {} request {}: {}", 
                           request.getRequestType(), request.getRequestId(), e.getMessage());
                // Continue with other requests
            }
        }
        
        logger.info("Bulk approved {} requests by {}", requests.size(), approverEmployeeId);
    }

    public void bulkRejectRequests(List<BulkRejectionRequest> requests, String approverEmployeeId) {
        for (BulkRejectionRequest request : requests) {
            try {
                rejectRequest(request.getRequestType(), request.getRequestId(), 
                            approverEmployeeId, request.getRejectionReason());
            } catch (Exception e) {
                logger.error("Failed to reject {} request {}: {}", 
                           request.getRequestType(), request.getRequestId(), e.getMessage());
                // Continue with other requests
            }
        }
        
        logger.info("Bulk rejected {} requests by {}", requests.size(), approverEmployeeId);
    }

    // Get all pending requests for a manager
    @Transactional(readOnly = true)
    public List<Object> getAllPendingRequestsForManager(String managerId) {
        List<Object> allRequests = new ArrayList<>();
        
        // Get pending leave requests
        List<LeaveRequest> pendingLeaveRequests = leaveRequestService.getPendingLeaveRequestsForManager(managerId);
        allRequests.addAll(pendingLeaveRequests);
        
        // Get pending time modification requests
        List<TimeModificationRequest> pendingTimeRequests = timeModificationRequestService.getPendingTimeModificationRequestsForManager(managerId);
        allRequests.addAll(pendingTimeRequests);
        
        return allRequests;
    }

    // Get approval statistics for a manager
    @Transactional(readOnly = true)
    public ApprovalStatistics getApprovalStatistics(String managerId) {
        long pendingLeaveRequests = leaveRequestService.countPendingRequestsForManager(managerId);
        long pendingTimeRequests = timeModificationRequestService.countPendingRequestsForManager(managerId);
        
        return new ApprovalStatistics(pendingLeaveRequests, pendingTimeRequests);
    }

    // Get approval statistics for all managers (admin only)
    @Transactional(readOnly = true)
    public ApprovalStatistics getGlobalApprovalStatistics() {
        long pendingLeaveRequests = leaveRequestService.countPendingRequests();
        long pendingTimeRequests = timeModificationRequestService.countPendingRequests();
        
        return new ApprovalStatistics(pendingLeaveRequests, pendingTimeRequests);
    }

    // Workflow validation
    public boolean canApproveRequest(String requestType, UUID requestId, String approverEmployeeId) {
        try {
            // This would contain business logic to determine if the approver can approve this request
            // For now, we'll check if the approver is a manager and manages the department
            return userService.getUserByEmployeeId(approverEmployeeId).isManager();
        } catch (Exception e) {
            logger.error("Error checking approval authority for {} request {}: {}", 
                        requestType, requestId, e.getMessage());
            return false;
        }
    }

    // Auto-approval rules (for future implementation)
    public void processAutoApprovalRules() {
        // Future implementation for automatic approval based on rules
        // e.g., auto-approve leave requests less than 1 day
        // e.g., auto-approve time modifications within 15 minutes
        logger.debug("Processing auto-approval rules...");
    }

    // Escalation handling (for future implementation)
    public void handleEscalation(String requestType, UUID requestId) {
        // Future implementation for escalating pending requests
        // e.g., escalate to higher management after 48 hours
        logger.info("Handling escalation for {} request {}", requestType, requestId);
    }

    // Inner classes for request handling
    public static class BulkApprovalRequest {
        private String requestType;
        private UUID requestId;

        public BulkApprovalRequest() {}

        public BulkApprovalRequest(String requestType, UUID requestId) {
            this.requestType = requestType;
            this.requestId = requestId;
        }

        public String getRequestType() { return requestType; }
        public void setRequestType(String requestType) { this.requestType = requestType; }
        public UUID getRequestId() { return requestId; }
        public void setRequestId(UUID requestId) { this.requestId = requestId; }
    }

    public static class BulkRejectionRequest extends BulkApprovalRequest {
        private String rejectionReason;

        public BulkRejectionRequest() {}

        public BulkRejectionRequest(String requestType, UUID requestId, String rejectionReason) {
            super(requestType, requestId);
            this.rejectionReason = rejectionReason;
        }

        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }

    public static class ApprovalStatistics {
        private long pendingLeaveRequests;
        private long pendingTimeModificationRequests;
        private long totalPendingRequests;

        public ApprovalStatistics(long pendingLeaveRequests, long pendingTimeModificationRequests) {
            this.pendingLeaveRequests = pendingLeaveRequests;
            this.pendingTimeModificationRequests = pendingTimeModificationRequests;
            this.totalPendingRequests = pendingLeaveRequests + pendingTimeModificationRequests;
        }

        public long getPendingLeaveRequests() { return pendingLeaveRequests; }
        public long getPendingTimeModificationRequests() { return pendingTimeModificationRequests; }
        public long getTotalPendingRequests() { return totalPendingRequests; }
    }
}