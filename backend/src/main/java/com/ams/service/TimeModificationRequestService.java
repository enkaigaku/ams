package com.ams.service;

import com.ams.entity.TimeModificationRequest;
import com.ams.entity.TimeRecord;
import com.ams.entity.User;
import com.ams.entity.enums.RequestStatus;
import com.ams.exception.ResourceNotFoundException;
import com.ams.repository.TimeModificationRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TimeModificationRequestService {

    private static final Logger logger = LoggerFactory.getLogger(TimeModificationRequestService.class);

    @Autowired
    private TimeModificationRequestRepository timeModificationRequestRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TimeRecordService timeRecordService;

    @Autowired
    private com.ams.validation.BusinessRuleValidator businessRuleValidator;

    public TimeModificationRequest createTimeModificationRequest(String employeeId, LocalDate requestDate,
                                                                LocalDateTime requestedClockIn, LocalDateTime requestedClockOut,
                                                                String reason) {
        User user = userService.getUserByEmployeeId(employeeId);
        
        // Apply business rule validation
        businessRuleValidator.validateTimeModificationRequest(user, requestDate, requestedClockIn, requestedClockOut, reason);
        
        // Validate request
        validateTimeModificationRequest(user, requestDate, requestedClockIn, requestedClockOut, reason);
        
        // Check for existing active requests for the same date
        checkForActiveRequests(user.getId(), requestDate);
        
        TimeModificationRequest request = new TimeModificationRequest(user, requestDate, reason);
        
        // Get existing time record for the date to set original values
        Optional<TimeRecord> existingRecord = timeRecordService.getTodayRecord(user.getId(), requestDate);
        if (existingRecord.isPresent()) {
            TimeRecord record = existingRecord.get();
            request.setOriginalClockIn(record.getClockIn());
            request.setOriginalClockOut(record.getClockOut());
        }
        
        request.setRequestedClockIn(requestedClockIn);
        request.setRequestedClockOut(requestedClockOut);
        
        TimeModificationRequest savedRequest = timeModificationRequestRepository.save(request);
        
        logger.info("Created time modification request for user: {} on date: {}", employeeId, requestDate);
        return savedRequest;
    }

    @Transactional(readOnly = true)
    public TimeModificationRequest getTimeModificationRequestById(UUID id) {
        return timeModificationRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Time modification request not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TimeModificationRequest> getTimeModificationRequestsByUser(String employeeId) {
        User user = userService.getUserByEmployeeId(employeeId);
        return timeModificationRequestRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public List<TimeModificationRequest> getTimeModificationRequestsByUserAndStatus(String employeeId, RequestStatus status) {
        User user = userService.getUserByEmployeeId(employeeId);
        return timeModificationRequestRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), status);
    }

    @Transactional(readOnly = true)
    public List<TimeModificationRequest> getPendingTimeModificationRequests() {
        return timeModificationRequestRepository.findByStatusOrderByCreatedAtDesc(RequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<TimeModificationRequest> getTimeModificationRequestsForManager(String managerId) {
        return timeModificationRequestRepository.findByManagerIdOrderByCreatedAtDesc(managerId);
    }

    @Transactional(readOnly = true)
    public List<TimeModificationRequest> getPendingTimeModificationRequestsForManager(String managerId) {
        return timeModificationRequestRepository.findByManagerIdAndStatusOrderByCreatedAtDesc(managerId, RequestStatus.PENDING);
    }

    public TimeModificationRequest approveTimeModificationRequest(UUID requestId, String approverEmployeeId) {
        TimeModificationRequest request = getTimeModificationRequestById(requestId);
        
        // Validate approval authority
        validateApprovalAuthority(request, approverEmployeeId);
        
        // Check if request is still pending
        if (!request.isPending()) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }
        
        // Apply the time modifications
        applyTimeModifications(request);
        
        request.approve(approverEmployeeId);
        TimeModificationRequest savedRequest = timeModificationRequestRepository.save(request);
        
        logger.info("Approved time modification request {} by {}", requestId, approverEmployeeId);
        return savedRequest;
    }

    public TimeModificationRequest rejectTimeModificationRequest(UUID requestId, String approverEmployeeId, String rejectionReason) {
        TimeModificationRequest request = getTimeModificationRequestById(requestId);
        
        // Validate approval authority
        validateApprovalAuthority(request, approverEmployeeId);
        
        // Check if request is still pending
        if (!request.isPending()) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }
        
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("却下理由は必須です");
        }
        
        request.reject(approverEmployeeId, rejectionReason);
        TimeModificationRequest savedRequest = timeModificationRequestRepository.save(request);
        
        logger.info("Rejected time modification request {} by {} with reason: {}", requestId, approverEmployeeId, rejectionReason);
        return savedRequest;
    }

    public TimeModificationRequest updateTimeModificationRequest(UUID requestId, String employeeId,
                                                               TimeModificationRequest updates) {
        TimeModificationRequest existingRequest = getTimeModificationRequestById(requestId);
        
        // Verify ownership
        if (!existingRequest.getUser().getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("この申請を編集する権限がありません");
        }
        
        // Only pending requests can be updated
        if (!existingRequest.isPending()) {
            throw new IllegalStateException("承認済みまたは却下済みの申請は編集できません");
        }
        
        // Update fields
        if (updates.getRequestedClockIn() != null) {
            existingRequest.setRequestedClockIn(updates.getRequestedClockIn());
        }
        
        if (updates.getRequestedClockOut() != null) {
            existingRequest.setRequestedClockOut(updates.getRequestedClockOut());
        }
        
        if (updates.getReason() != null) {
            existingRequest.setReason(updates.getReason());
        }
        
        // Validate updated request
        validateTimeModificationRequest(existingRequest.getUser(), existingRequest.getRequestDate(),
                existingRequest.getRequestedClockIn(), existingRequest.getRequestedClockOut(),
                existingRequest.getReason());
        
        TimeModificationRequest savedRequest = timeModificationRequestRepository.save(existingRequest);
        logger.info("Updated time modification request {} by {}", requestId, employeeId);
        return savedRequest;
    }

    public void cancelTimeModificationRequest(UUID requestId, String employeeId) {
        TimeModificationRequest request = getTimeModificationRequestById(requestId);
        
        // Verify ownership
        if (!request.getUser().getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("この申請をキャンセルする権限がありません");
        }
        
        // Only pending requests can be cancelled
        if (!request.isPending()) {
            throw new IllegalStateException("承認済みまたは却下済みの申請はキャンセルできません");
        }
        
        timeModificationRequestRepository.delete(request);
        logger.info("Cancelled time modification request {} by {}", requestId, employeeId);
    }

    private void validateTimeModificationRequest(User user, LocalDate requestDate,
                                               LocalDateTime requestedClockIn, LocalDateTime requestedClockOut,
                                               String reason) {
        if (requestDate == null) {
            throw new IllegalArgumentException("対象日は必須です");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("修正理由は必須です");
        }
        
        // Cannot modify future dates
        if (requestDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("未来の日付は修正できません");
        }
        
        // Cannot modify dates older than 30 days
        if (requestDate.isBefore(LocalDate.now().minusDays(30))) {
            throw new IllegalArgumentException("30日以上前の日付は修正できません");
        }
        
        // At least one time should be provided
        if (requestedClockIn == null && requestedClockOut == null) {
            throw new IllegalArgumentException("出勤時刻または退勤時刻のいずれかは必須です");
        }
        
        // Validate time logic
        if (requestedClockIn != null && requestedClockOut != null) {
            if (requestedClockIn.isAfter(requestedClockOut)) {
                throw new IllegalArgumentException("出勤時刻は退勤時刻より前である必要があります");
            }
            
            // Check for reasonable working hours (max 16 hours)
            long hoursWorked = java.time.Duration.between(requestedClockIn, requestedClockOut).toHours();
            if (hoursWorked > 16) {
                throw new IllegalArgumentException("勤務時間が16時間を超えています");
            }
        }
        
        // Validate that the requested times are on the correct date
        if (requestedClockIn != null && !requestedClockIn.toLocalDate().equals(requestDate)) {
            throw new IllegalArgumentException("出勤時刻の日付が対象日と一致しません");
        }
        
        if (requestedClockOut != null && !requestedClockOut.toLocalDate().equals(requestDate)) {
            throw new IllegalArgumentException("退勤時刻の日付が対象日と一致しません");
        }
    }

    private void checkForActiveRequests(UUID userId, LocalDate requestDate) {
        List<TimeModificationRequest> activeRequests = timeModificationRequestRepository
                .findActiveRequestsByUserAndDate(userId, requestDate);
        
        if (!activeRequests.isEmpty()) {
            throw new IllegalArgumentException("この日付に対する申請が既に存在します");
        }
    }

    private void validateApprovalAuthority(TimeModificationRequest request, String approverEmployeeId) {
        User approver = userService.getUserByEmployeeId(approverEmployeeId);
        
        // Check if approver is a manager
        if (!approver.isManager()) {
            throw new IllegalArgumentException("承認権限がありません");
        }
        
        // Check if approver manages the department of the requester
        if (request.getUser().getDepartment() == null) {
            throw new IllegalArgumentException("申請者の部署が設定されていません");
        }
        
        String departmentManagerId = request.getUser().getDepartment().getManagerId();
        if (!approverEmployeeId.equals(departmentManagerId)) {
            throw new IllegalArgumentException("この部署の申請を承認する権限がありません");
        }
    }

    private void applyTimeModifications(TimeModificationRequest request) {
        try {
            // Get or create time record for the date
            Optional<TimeRecord> existingRecord = timeRecordService.getTodayRecord(
                    request.getUser().getId(), request.getRequestDate());
            
            TimeRecord timeRecord;
            if (existingRecord.isPresent()) {
                timeRecord = existingRecord.get();
            } else {
                // Create new time record
                timeRecord = new TimeRecord(request.getUser(), request.getRequestDate());
            }
            
            // Apply modifications
            if (request.hasClockInModification()) {
                timeRecord.setClockIn(request.getRequestedClockIn());
            }
            
            if (request.hasClockOutModification()) {
                timeRecord.setClockOut(request.getRequestedClockOut());
            }
            
            // Update the time record
            timeRecordService.updateTimeRecord(timeRecord.getId(), timeRecord);
            
            logger.info("Applied time modifications for user: {} on date: {}", 
                       request.getUser().getEmployeeId(), request.getRequestDate());
                       
        } catch (Exception e) {
            logger.error("Failed to apply time modifications for request: {}", request.getId(), e);
            throw new RuntimeException("打刻修正の適用でエラーが発生しました", e);
        }
    }

    @Transactional(readOnly = true)
    public long countPendingRequests() {
        return timeModificationRequestRepository.countByStatus(RequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public long countPendingRequestsForManager(String managerId) {
        return timeModificationRequestRepository.countByManagerIdAndStatus(managerId, RequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<TimeModificationRequest> getTimeModificationRequestsByDateRange(LocalDate startDate, LocalDate endDate) {
        return timeModificationRequestRepository.findByDateRangeOrderByRequestDate(startDate, endDate);
    }
}