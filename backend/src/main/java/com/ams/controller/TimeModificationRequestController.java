package com.ams.controller;

import com.ams.dto.ApiResponses;
import com.ams.entity.TimeModificationRequest;
import com.ams.entity.enums.RequestStatus;
import com.ams.service.TimeModificationRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.ams.validation.ValidWorkingHours;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/requests/time")
@Validated
@Tag(name = "Time Modification Requests", description = "勤務時間修正申請関連のAPI")
public class TimeModificationRequestController {

    private static final Logger logger = LoggerFactory.getLogger(TimeModificationRequestController.class);

    @Autowired
    private TimeModificationRequestService timeModificationRequestService;

    @PostMapping
    @Operation(summary = "勤務時間修正申請作成", description = "新しい勤務時間修正申請を作成します")
    public ResponseEntity<ApiResponses<TimeModificationRequest>> createTimeModificationRequest(
            @Valid @RequestBody CreateTimeModificationRequestDto request) {
        try {
            String employeeId = getCurrentEmployeeId();
            
            TimeModificationRequest createdRequest = timeModificationRequestService.createTimeModificationRequest(
                    employeeId,
                    request.getRequestDate(),
                    request.getRequestedClockIn(),
                    request.getRequestedClockOut(),
                    request.getReason()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponses.success(createdRequest));
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating time modification request", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponses.error("勤務時間修正申請の作成でエラーが発生しました"));
        }
    }

    @GetMapping
    @Operation(summary = "勤務時間修正申請一覧取得", description = "ログインユーザーの勤務時間修正申請一覧を取得します")
    public ResponseEntity<ApiResponses<List<TimeModificationRequest>>> getMyTimeModificationRequests(
            @RequestParam(required = false) RequestStatus status) {
        try {
            String employeeId = getCurrentEmployeeId();
            
            List<TimeModificationRequest> requests;
            if (status != null) {
                requests = timeModificationRequestService.getTimeModificationRequestsByUserAndStatus(employeeId, status);
            } else {
                requests = timeModificationRequestService.getTimeModificationRequestsByUser(employeeId);
            }
            
            return ResponseEntity.ok(ApiResponses.success(requests));
            
        } catch (Exception e) {
            logger.error("Error getting time modification requests", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponses.error("勤務時間修正申請の取得でエラーが発生しました"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "勤務時間修正申請詳細取得", description = "指定された勤務時間修正申請の詳細を取得します")
    public ResponseEntity<ApiResponses<TimeModificationRequest>> getTimeModificationRequest(@PathVariable UUID id) {
        try {
            TimeModificationRequest request = timeModificationRequestService.getTimeModificationRequestById(id);
            
            // Check ownership or manager authority
            String currentEmployeeId = getCurrentEmployeeId();
            if (!request.getUser().getEmployeeId().equals(currentEmployeeId) && 
                !isManagerOfRequest(currentEmployeeId, request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponses.error("この申請を閲覧する権限がありません"));
            }
            
            return ResponseEntity.ok(ApiResponses.success(request));
            
        } catch (Exception e) {
            logger.error("Error getting time modification request", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponses.error("勤務時間修正申請の取得でエラーが発生しました"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "勤務時間修正申請更新", description = "指定された勤務時間修正申請を更新します")
    public ResponseEntity<ApiResponses<TimeModificationRequest>> updateTimeModificationRequest(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTimeModificationRequestDto updateDto) {
        try {
            String employeeId = getCurrentEmployeeId();
            
            // Create update object
            TimeModificationRequest updates = new TimeModificationRequest();
            updates.setRequestedClockIn(updateDto.getRequestedClockIn());
            updates.setRequestedClockOut(updateDto.getRequestedClockOut());
            updates.setReason(updateDto.getReason());
            
            TimeModificationRequest updatedRequest = timeModificationRequestService.updateTimeModificationRequest(
                    id, employeeId, updates);
            
            return ResponseEntity.ok(ApiResponses.success(updatedRequest));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating time modification request", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponses.error("勤務時間修正申請の更新でエラーが発生しました"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "勤務時間修正申請キャンセル", description = "指定された勤務時間修正申請をキャンセルします")
    public ResponseEntity<ApiResponses<Void>> cancelTimeModificationRequest(@PathVariable UUID id) {
        try {
            String employeeId = getCurrentEmployeeId();
            timeModificationRequestService.cancelTimeModificationRequest(id, employeeId);
            
            return ResponseEntity.ok(ApiResponses.successMessage("勤務時間修正申請をキャンセルしました"));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error cancelling time modification request", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponses.error("勤務時間修正申請のキャンセルでエラーが発生しました"));
        }
    }

    @GetMapping("/history")
    @Operation(summary = "勤務時間修正申請履歴取得", description = "指定期間の勤務時間修正申請履歴を取得します")
    public ResponseEntity<ApiResponses<List<TimeModificationRequest>>> getTimeModificationHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponses.error("開始日は終了日より前である必要があります"));
            }
            
            List<TimeModificationRequest> requests = timeModificationRequestService
                    .getTimeModificationRequestsByDateRange(startDate, endDate);
            
            return ResponseEntity.ok(ApiResponses.success(requests));
            
        } catch (Exception e) {
            logger.error("Error getting time modification history", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponses.error("勤務時間修正申請履歴の取得でエラーが発生しました"));
        }
    }

    @GetMapping("/pending")
    @Operation(summary = "承認待ち申請取得", description = "承認待ちの勤務時間修正申請一覧を取得します（管理者用）")
    public ResponseEntity<ApiResponses<List<TimeModificationRequest>>> getPendingTimeModificationRequests() {
        try {
            String managerId = getCurrentEmployeeId();
            List<TimeModificationRequest> pendingRequests = timeModificationRequestService
                    .getPendingTimeModificationRequestsForManager(managerId);
            
            return ResponseEntity.ok(ApiResponses.success(pendingRequests));
            
        } catch (Exception e) {
            logger.error("Error getting pending time modification requests", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponses.error("承認待ち申請の取得でエラーが発生しました"));
        }
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "勤務時間修正申請承認", description = "指定された勤務時間修正申請を承認します（管理者用）")
    public ResponseEntity<ApiResponses<TimeModificationRequest>> approveTimeModificationRequest(@PathVariable UUID id) {
        try {
            String approverEmployeeId = getCurrentEmployeeId();
            TimeModificationRequest approvedRequest = timeModificationRequestService
                    .approveTimeModificationRequest(id, approverEmployeeId);
            
            return ResponseEntity.ok(ApiResponses.success(approvedRequest));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error approving time modification request", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponses.error("勤務時間修正申請の承認でエラーが発生しました"));
        }
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "勤務時間修正申請却下", description = "指定された勤務時間修正申請を却下します（管理者用）")
    public ResponseEntity<ApiResponses<TimeModificationRequest>> rejectTimeModificationRequest(
            @PathVariable UUID id,
            @Valid @RequestBody RejectRequestDto rejectDto) {
        try {
            String approverEmployeeId = getCurrentEmployeeId();
            
            if (rejectDto.getRejectionReason() == null || rejectDto.getRejectionReason().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponses.error("却下理由は必須です"));
            }
            
            TimeModificationRequest rejectedRequest = timeModificationRequestService
                    .rejectTimeModificationRequest(id, approverEmployeeId, rejectDto.getRejectionReason());
            
            return ResponseEntity.ok(ApiResponses.success(rejectedRequest));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error rejecting time modification request", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponses.error("勤務時間修正申請の却下でエラーが発生しました"));
        }
    }

    private String getCurrentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("認証が必要です");
        }
        return authentication.getName();
    }

    private boolean isManagerOfRequest(String employeeId, TimeModificationRequest request) {
        // Check if the current user is a manager of the department that owns this request
        try {
            if (request.getUser().getDepartment() == null) {
                return false;
            }
            return employeeId.equals(request.getUser().getDepartment().getManagerId());
        } catch (Exception e) {
            logger.warn("Error checking manager authority for request {}: {}", request.getId(), e.getMessage());
            return false;
        }
    }

    // DTO classes for request/response
    @ValidWorkingHours(maxWorkingHours = 16, minWorkingMinutes = 30, 
                      message = "勤務時間が正しくありません")
    public static class CreateTimeModificationRequestDto {
        @NotNull(message = "対象日は必須です")
        private LocalDate requestDate;
        
        private LocalDateTime requestedClockIn;
        
        private LocalDateTime requestedClockOut;
        
        @NotBlank(message = "修正理由は必須です")
        @Size(max = 1000, message = "修正理由は1000文字以内で入力してください")
        private String reason;

        // Getters and setters
        public LocalDate getRequestDate() { return requestDate; }
        public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
        public LocalDateTime getRequestedClockIn() { return requestedClockIn; }
        public void setRequestedClockIn(LocalDateTime requestedClockIn) { this.requestedClockIn = requestedClockIn; }
        public LocalDateTime getRequestedClockOut() { return requestedClockOut; }
        public void setRequestedClockOut(LocalDateTime requestedClockOut) { this.requestedClockOut = requestedClockOut; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    @ValidWorkingHours(maxWorkingHours = 16, minWorkingMinutes = 30, 
                      message = "勤務時間が正しくありません")
    public static class UpdateTimeModificationRequestDto {
        private LocalDateTime requestedClockIn;
        
        private LocalDateTime requestedClockOut;
        
        @Size(max = 1000, message = "修正理由は1000文字以内で入力してください")
        private String reason;

        // Getters and setters
        public LocalDateTime getRequestedClockIn() { return requestedClockIn; }
        public void setRequestedClockIn(LocalDateTime requestedClockIn) { this.requestedClockIn = requestedClockIn; }
        public LocalDateTime getRequestedClockOut() { return requestedClockOut; }
        public void setRequestedClockOut(LocalDateTime requestedClockOut) { this.requestedClockOut = requestedClockOut; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class RejectRequestDto {
        @NotBlank(message = "却下理由は必須です")
        @Size(max = 1000, message = "却下理由は1000文字以内で入力してください")
        private String rejectionReason;

        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }
}