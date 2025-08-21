package com.ams.controller;

import com.ams.dto.ApiResponses;
import com.ams.service.ApprovalWorkflowService;
import com.ams.service.ManagerDashboardService;
import com.ams.service.AlertService;
import com.ams.entity.Alert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/manager")
@PreAuthorize("hasRole('MANAGER')")
@Tag(name = "Manager Dashboard", description = "管理者ダッシュボード関連のAPI")
public class ManagerController {

    private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);

    @Autowired
    private ManagerDashboardService managerDashboardService;

    @Autowired
    private ApprovalWorkflowService approvalWorkflowService;

    @Autowired
    private AlertService alertService;

    @GetMapping("/dashboard")
    @Operation(summary = "ダッシュボード概要取得", description = "管理者ダッシュボードの概要情報を取得します")
    public ResponseEntity<ApiResponses<ManagerDashboardService.DashboardOverview>> getDashboardOverview() {
        try {
            String managerId = getCurrentEmployeeId();
            ManagerDashboardService.DashboardOverview overview = managerDashboardService.getDashboardOverview(managerId);
            return ResponseEntity.ok(ApiResponses.success(overview));
        } catch (Exception e) {
            logger.error("Error getting dashboard overview", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("ダッシュボード情報の取得でエラーが発生しました"));
        }
    }

    @GetMapping("/team/statistics")
    @Operation(summary = "チーム統計取得", description = "指定期間のチーム統計を取得します")
    public ResponseEntity<ApiResponses<ManagerDashboardService.TeamStatistics>> getTeamStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            String managerId = getCurrentEmployeeId();
            
            // Validate date range
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(ApiResponses.error("開始日は終了日より前である必要があります"));
            }
            
            ManagerDashboardService.TeamStatistics statistics = 
                    managerDashboardService.getTeamStatistics(managerId, startDate, endDate);
            
            return ResponseEntity.ok(ApiResponses.success(statistics));
        } catch (Exception e) {
            logger.error("Error getting team statistics", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("チーム統計の取得でエラーが発生しました"));
        }
    }

    @GetMapping("/attendance/summary")
    @Operation(summary = "出勤状況サマリー取得", description = "指定日の出勤状況サマリーを取得します")
    public ResponseEntity<ApiResponses<Map<String, Object>>> getAttendanceSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            String managerId = getCurrentEmployeeId();
            LocalDate targetDate = date != null ? date : LocalDate.now();
            
            Map<String, Object> summary = managerDashboardService.getAttendanceSummary(managerId, targetDate);
            return ResponseEntity.ok(ApiResponses.success(summary));
        } catch (Exception e) {
            logger.error("Error getting attendance summary", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("出勤状況サマリーの取得でエラーが発生しました"));
        }
    }

    @GetMapping("/team/performance")
    @Operation(summary = "チームパフォーマンス取得", description = "指定期間のチームメンバーのパフォーマンス指標を取得します")
    public ResponseEntity<ApiResponses<List<ManagerDashboardService.PerformanceMetric>>> getTeamPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            String managerId = getCurrentEmployeeId();
            
            // Validate date range
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(ApiResponses.error("開始日は終了日より前である必要があります"));
            }
            
            List<ManagerDashboardService.PerformanceMetric> metrics = 
                    managerDashboardService.getTeamPerformanceMetrics(managerId, startDate, endDate);
            
            return ResponseEntity.ok(ApiResponses.success(metrics));
        } catch (Exception e) {
            logger.error("Error getting team performance", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("チームパフォーマンスの取得でエラーが発生しました"));
        }
    }

    @GetMapping("/approvals/statistics")
    @Operation(summary = "承認統計取得", description = "承認待ち申請の統計を取得します")
    public ResponseEntity<ApiResponses<ApprovalWorkflowService.ApprovalStatistics>> getApprovalStatistics() {
        try {
            String managerId = getCurrentEmployeeId();
            ApprovalWorkflowService.ApprovalStatistics statistics = 
                    approvalWorkflowService.getApprovalStatistics(managerId);
            
            return ResponseEntity.ok(ApiResponses.success(statistics));
        } catch (Exception e) {
            logger.error("Error getting approval statistics", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("承認統計の取得でエラーが発生しました"));
        }
    }

    @GetMapping("/approvals/pending")
    @Operation(summary = "承認待ち申請一覧取得", description = "すべての承認待ち申請を取得します")
    public ResponseEntity<ApiResponses<List<Object>>> getPendingApprovals() {
        try {
            String managerId = getCurrentEmployeeId();
            List<Object> pendingRequests = approvalWorkflowService.getAllPendingRequestsForManager(managerId);
            
            return ResponseEntity.ok(ApiResponses.success(pendingRequests));
        } catch (Exception e) {
            logger.error("Error getting pending approvals", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("承認待ち申請の取得でエラーが発生しました"));
        }
    }

    @PostMapping("/approvals/{requestType}/{requestId}/approve")
    @Operation(summary = "申請承認", description = "指定された申請を承認します")
    public ResponseEntity<ApiResponses<Void>> approveRequest(
            @PathVariable String requestType,
            @PathVariable UUID requestId) {
        try {
            String managerId = getCurrentEmployeeId();
            approvalWorkflowService.approveRequest(requestType, requestId, managerId);
            
            return ResponseEntity.ok(ApiResponses.successMessage("申請を承認しました"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error approving request", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("申請の承認でエラーが発生しました"));
        }
    }

    @PostMapping("/approvals/{requestType}/{requestId}/reject")
    @Operation(summary = "申請却下", description = "指定された申請を却下します")
    public ResponseEntity<ApiResponses<Void>> rejectRequest(
            @PathVariable String requestType,
            @PathVariable UUID requestId,
            @RequestBody RejectRequestDto rejectDto) {
        try {
            String managerId = getCurrentEmployeeId();
            String rejectionReason = rejectDto.getRejectionReason();
            
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponses.error("却下理由は必須です"));
            }
            
            approvalWorkflowService.rejectRequest(requestType, requestId, managerId, rejectionReason);
            
            return ResponseEntity.ok(ApiResponses.successMessage("申請を却下しました"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error rejecting request", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("申請の却下でエラーが発生しました"));
        }
    }

    @PostMapping("/approvals/bulk/approve")
    @Operation(summary = "一括承認", description = "複数の申請を一括で承認します")
    public ResponseEntity<ApiResponses<Void>> bulkApproveRequests(
            @RequestBody List<ApprovalWorkflowService.BulkApprovalRequest> requests) {
        try {
            String managerId = getCurrentEmployeeId();
            approvalWorkflowService.bulkApproveRequests(requests, managerId);
            
            return ResponseEntity.ok(ApiResponses.successMessage(requests.size() + "件の申請を一括承認しました"));
        } catch (Exception e) {
            logger.error("Error bulk approving requests", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("一括承認でエラーが発生しました"));
        }
    }

    @PostMapping("/approvals/bulk/reject")
    @Operation(summary = "一括却下", description = "複数の申請を一括で却下します")
    public ResponseEntity<ApiResponses<Void>> bulkRejectRequests(
            @RequestBody List<ApprovalWorkflowService.BulkRejectionRequest> requests) {
        try {
            String managerId = getCurrentEmployeeId();
            approvalWorkflowService.bulkRejectRequests(requests, managerId);
            
            return ResponseEntity.ok(ApiResponses.successMessage(requests.size() + "件の申請を一括却下しました"));
        } catch (Exception e) {
            logger.error("Error bulk rejecting requests", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("一括却下でエラーが発生しました"));
        }
    }

    @GetMapping("/alerts")
    @Operation(summary = "アラート一覧取得", description = "管理者のアラート一覧を取得します")
    public ResponseEntity<ApiResponses<List<Alert>>> getAlerts() {
        try {
            String managerId = getCurrentEmployeeId();
            List<Alert> alerts = alertService.getAlertsForManager(managerId);
            
            return ResponseEntity.ok(ApiResponses.success(alerts));
        } catch (Exception e) {
            logger.error("Error getting alerts", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("アラートの取得でエラーが発生しました"));
        }
    }

    @GetMapping("/alerts/unread")
    @Operation(summary = "未読アラート取得", description = "未読のアラート一覧を取得します")
    public ResponseEntity<ApiResponses<List<Alert>>> getUnreadAlerts() {
        try {
            String managerId = getCurrentEmployeeId();
            List<Alert> unreadAlerts = alertService.getUnreadAlertsForManager(managerId);
            
            return ResponseEntity.ok(ApiResponses.success(unreadAlerts));
        } catch (Exception e) {
            logger.error("Error getting unread alerts", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("未読アラートの取得でエラーが発生しました"));
        }
    }

    @PostMapping("/alerts/{alertId}/read")
    @Operation(summary = "アラート既読", description = "指定されたアラートを既読にします")
    public ResponseEntity<ApiResponses<Void>> markAlertAsRead(@PathVariable UUID alertId) {
        try {
            alertService.markAlertAsRead(alertId);
            return ResponseEntity.ok(ApiResponses.successMessage("アラートを既読にしました"));
        } catch (Exception e) {
            logger.error("Error marking alert as read", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("アラートの既読処理でエラーが発生しました"));
        }
    }

    @PostMapping("/alerts/mark-all-read")
    @Operation(summary = "全アラート既読", description = "すべてのアラートを既読にします")
    public ResponseEntity<ApiResponses<Void>> markAllAlertsAsRead() {
        try {
            String managerId = getCurrentEmployeeId();
            alertService.markAllAlertsAsReadForManager(managerId);
            
            return ResponseEntity.ok(ApiResponses.successMessage("すべてのアラートを既読にしました"));
        } catch (Exception e) {
            logger.error("Error marking all alerts as read", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("アラートの既読処理でエラーが発生しました"));
        }
    }

    private String getCurrentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("認証が必要です");
        }
        return authentication.getName();
    }

    // Inner classes for request DTOs
    public static class RejectRequestDto {
        private String rejectionReason;

        public String getRejectionReason() {
            return rejectionReason;
        }

        public void setRejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
        }
    }
}