package com.ams.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ams.entity.LeaveRequest;
import com.ams.entity.User;
import com.ams.entity.enums.LeaveType;
import com.ams.entity.enums.RequestStatus;
import com.ams.exception.ResourceNotFoundException;
import com.ams.repository.LeaveRequestRepository;

@Service
@Transactional
public class LeaveRequestService {

    private static final Logger logger = LoggerFactory.getLogger(LeaveRequestService.class);

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private com.ams.validation.BusinessRuleValidator businessRuleValidator;

    public LeaveRequest createLeaveRequest(String employeeId, LeaveType type, LocalDate startDate, 
                                         LocalDate endDate, String reason) {
        User user = userService.getUserByEmployeeId(employeeId);
        
        // Apply business rule validation
        businessRuleValidator.validateLeaveRequest(user, type, startDate, endDate, reason);
        
        // Validate dates
        validateLeaveDates(startDate, endDate);
        
        // Check for overlapping requests
        checkForOverlappingRequests(user.getId(), startDate, endDate);
        
        // Business rules validation
        validateLeaveRequest(user, type, startDate, endDate);
        
        LeaveRequest leaveRequest = new LeaveRequest(user, type, startDate, endDate, reason);
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        logger.info("Created leave request for user: {} from {} to {}", employeeId, startDate, endDate);
        return savedRequest;
    }

    @Transactional(readOnly = true)
    public LeaveRequest getLeaveRequestById(UUID id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByUser(String employeeId) {
        User user = userService.getUserByEmployeeId(employeeId);
        return leaveRequestRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByUserAndStatus(String employeeId, RequestStatus status) {
        User user = userService.getUserByEmployeeId(employeeId);
        return leaveRequestRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), status);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatusOrderByCreatedAtDesc(RequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsForManager(String managerId) {
        return leaveRequestRepository.findByManagerIdOrderByCreatedAtDesc(managerId);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getPendingLeaveRequestsForManager(String managerId) {
        return leaveRequestRepository.findByManagerIdAndStatusOrderByCreatedAtDesc(managerId, RequestStatus.PENDING);
    }

    public LeaveRequest approveLeaveRequest(UUID requestId, String approverEmployeeId) {
        LeaveRequest leaveRequest = getLeaveRequestById(requestId);
        
        // Validate approval authority
        validateApprovalAuthority(leaveRequest, approverEmployeeId);
        
        // Check if request is still pending
        if (!leaveRequest.isPending()) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }
        
        // Final validation before approval
        validateLeaveApproval(leaveRequest);
        
        leaveRequest.approve(approverEmployeeId);
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        logger.info("Approved leave request {} by {}", requestId, approverEmployeeId);
        return savedRequest;
    }

    public LeaveRequest rejectLeaveRequest(UUID requestId, String approverEmployeeId, String rejectionReason) {
        LeaveRequest leaveRequest = getLeaveRequestById(requestId);
        
        // Validate approval authority
        validateApprovalAuthority(leaveRequest, approverEmployeeId);
        
        // Check if request is still pending
        if (!leaveRequest.isPending()) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }
        
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("却下理由は必須です");
        }
        
        leaveRequest.reject(approverEmployeeId, rejectionReason);
        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        
        logger.info("Rejected leave request {} by {} with reason: {}", requestId, approverEmployeeId, rejectionReason);
        return savedRequest;
    }

    public LeaveRequest updateLeaveRequest(UUID requestId, String employeeId, LeaveRequest updates) {
        LeaveRequest existingRequest = getLeaveRequestById(requestId);
        
        // Verify ownership
        if (!existingRequest.getUser().getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("この申請を編集する権限がありません");
        }
        
        // Only pending requests can be updated
        if (!existingRequest.isPending()) {
            throw new IllegalStateException("承認済みまたは却下済みの申請は編集できません");
        }
        
        // Update fields
        if (updates.getStartDate() != null && updates.getEndDate() != null) {
            validateLeaveDates(updates.getStartDate(), updates.getEndDate());
            checkForOverlappingRequests(existingRequest.getUser().getId(), 
                                      updates.getStartDate(), updates.getEndDate(), requestId);
            existingRequest.setStartDate(updates.getStartDate());
            existingRequest.setEndDate(updates.getEndDate());
        }
        
        if (updates.getType() != null) {
            existingRequest.setType(updates.getType());
        }
        
        if (updates.getReason() != null) {
            existingRequest.setReason(updates.getReason());
        }
        
        LeaveRequest savedRequest = leaveRequestRepository.save(existingRequest);
        logger.info("Updated leave request {} by {}", requestId, employeeId);
        return savedRequest;
    }

    public void cancelLeaveRequest(UUID requestId, String employeeId) {
        LeaveRequest leaveRequest = getLeaveRequestById(requestId);
        
        // Verify ownership
        if (!leaveRequest.getUser().getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("この申請をキャンセルする権限がありません");
        }
        
        // Only pending requests can be cancelled
        if (!leaveRequest.isPending()) {
            throw new IllegalStateException("承認済みまたは却下済みの申請はキャンセルできません");
        }
        
        leaveRequestRepository.delete(leaveRequest);
        logger.info("Cancelled leave request {} by {}", requestId, employeeId);
    }

    private void validateLeaveDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("開始日と終了日は必須です");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("開始日は終了日より前である必要があります");
        }
        
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("過去の日付は指定できません");
        }
        
        // Limit to 30 days in advance
        if (startDate.isAfter(LocalDate.now().plusDays(30))) {
            throw new IllegalArgumentException("30日以上先の日付は指定できません");
        }
    }

    private void checkForOverlappingRequests(UUID userId, LocalDate startDate, LocalDate endDate) {
        checkForOverlappingRequests(userId, startDate, endDate, null);
    }

    private void checkForOverlappingRequests(UUID userId, LocalDate startDate, LocalDate endDate, UUID excludeRequestId) {
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findUserRequestsOverlapping(
                userId, startDate, endDate);
        
        // Filter out the current request if updating
        if (excludeRequestId != null) {
            overlappingRequests = overlappingRequests.stream()
                    .filter(request -> !request.getId().equals(excludeRequestId))
                    .toList();
        }
        
        if (!overlappingRequests.isEmpty()) {
            throw new IllegalArgumentException("指定期間に既存の申請があります");
        }
    }

    private void validateLeaveRequest(User user, LeaveType type, LocalDate startDate, LocalDate endDate) {
        // Additional business rules can be implemented here
        // For example: checking annual leave balance, blackout dates, etc.
        
        if (type == LeaveType.PAID) {
            // Check if user has sufficient paid leave balance
            // This would require a separate table to track leave balances
            long usedPaidLeave = leaveRequestRepository.countApprovedLeavesByUserAndTypeAndYear(
                    user.getId(), LeaveType.PAID, startDate.getYear());
            
            // Assume 20 days annual leave limit
            long requestDays = startDate.datesUntil(endDate.plusDays(1)).count();
            if (usedPaidLeave + requestDays > 20) {
                throw new IllegalArgumentException("有給休暇の残日数が不足しています");
            }
        }
    }

    private void validateApprovalAuthority(LeaveRequest leaveRequest, String approverEmployeeId) {
        User approver = userService.getUserByEmployeeId(approverEmployeeId);
        
        // Check if approver is a manager
        if (!approver.isManager()) {
            throw new IllegalArgumentException("承認権限がありません");
        }
        
        // Check if approver manages the department of the requester
        if (leaveRequest.getUser().getDepartment() == null) {
            throw new IllegalArgumentException("申請者の部署が設定されていません");
        }
        
        String departmentManagerId = leaveRequest.getUser().getDepartment().getManagerId();
        if (!approverEmployeeId.equals(departmentManagerId)) {
            throw new IllegalArgumentException("この部署の申請を承認する権限がありません");
        }
    }

    private void validateLeaveApproval(LeaveRequest leaveRequest) {
        // Additional validation before approval
        // Check for scheduling conflicts, resource availability, etc.
        
        // For now, just validate that the dates are still in the future
        if (leaveRequest.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("過去の日付の申請は承認できません");
        }
    }

    @Transactional(readOnly = true)
    public long countPendingRequests() {
        return leaveRequestRepository.countByStatus(RequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public long countPendingRequestsForManager(String managerId) {
        return leaveRequestRepository.countByManagerIdAndStatus(managerId, RequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByDateRange(LocalDate startDate, LocalDate endDate) {
        return leaveRequestRepository.findByDateRangeOrderByStartDate(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsForManager(String managerId, LocalDate startDate, LocalDate endDate) {
        return leaveRequestRepository.findByManagerIdAndDateRangeOrderByStartDate(managerId, startDate, endDate);
    }
}