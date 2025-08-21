package com.ams.controller;

import com.ams.service.CsvExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/export")
@Tag(name = "CSV Export", description = "CSV形式でのデータエクスポート機能")
public class CsvExportController {

    private static final Logger logger = LoggerFactory.getLogger(CsvExportController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private CsvExportService csvExportService;

    @GetMapping("/attendance")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "出勤記録CSV出力", description = "指定期間のチーム出勤記録をCSV形式で出力します")
    public StreamingResponseBody exportAttendanceRecords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) {
        
        try {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("開始日は終了日より前である必要があります");
            }

            String managerId = getCurrentEmployeeId();
            String filename = String.format("attendance_%s_%s.csv", 
                                           startDate.format(DATE_FORMATTER), 
                                           endDate.format(DATE_FORMATTER));
            
            setupCsvResponse(response, filename);
            
            return outputStream -> {
                try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                    // Write BOM for Excel compatibility
                    writer.write('\ufeff');
                    csvExportService.exportAttendanceRecords(writer, startDate, endDate, managerId);
                } catch (IOException e) {
                    logger.error("Error exporting attendance records", e);
                    throw new RuntimeException("CSV出力でエラーが発生しました", e);
                }
            };
            
        } catch (Exception e) {
            logger.error("Error setting up attendance export", e);
            throw new RuntimeException("CSV出力の準備でエラーが発生しました", e);
        }
    }

    @GetMapping("/attendance/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "全出勤記録CSV出力", description = "指定期間の全社員出勤記録をCSV形式で出力します（管理者専用）")
    public StreamingResponseBody exportAllAttendanceRecords(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) {
        
        try {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("開始日は終了日より前である必要があります");
            }

            String filename = String.format("all_attendance_%s_%s.csv", 
                                           startDate.format(DATE_FORMATTER), 
                                           endDate.format(DATE_FORMATTER));
            
            setupCsvResponse(response, filename);
            
            return outputStream -> {
                try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                    // Write BOM for Excel compatibility
                    writer.write('\ufeff');
                    csvExportService.exportAllAttendanceRecords(writer, startDate, endDate);
                } catch (IOException e) {
                    logger.error("Error exporting all attendance records", e);
                    throw new RuntimeException("CSV出力でエラーが発生しました", e);
                }
            };
            
        } catch (Exception e) {
            logger.error("Error setting up all attendance export", e);
            throw new RuntimeException("CSV出力の準備でエラーが発生しました", e);
        }
    }

    @GetMapping("/leave-requests")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "休暇申請CSV出力", description = "指定期間のチーム休暇申請をCSV形式で出力します")
    public StreamingResponseBody exportLeaveRequests(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) {
        
        try {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("開始日は終了日より前である必要があります");
            }

            String managerId = getCurrentEmployeeId();
            String filename = String.format("leave_requests_%s_%s.csv", 
                                           startDate.format(DATE_FORMATTER), 
                                           endDate.format(DATE_FORMATTER));
            
            setupCsvResponse(response, filename);
            
            return outputStream -> {
                try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                    // Write BOM for Excel compatibility
                    writer.write('\ufeff');
                    csvExportService.exportLeaveRequests(writer, startDate, endDate, managerId);
                } catch (IOException e) {
                    logger.error("Error exporting leave requests", e);
                    throw new RuntimeException("CSV出力でエラーが発生しました", e);
                }
            };
            
        } catch (Exception e) {
            logger.error("Error setting up leave requests export", e);
            throw new RuntimeException("CSV出力の準備でエラーが発生しました", e);
        }
    }

    @GetMapping("/time-modifications")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "勤務時間修正申請CSV出力", description = "指定期間のチーム勤務時間修正申請をCSV形式で出力します")
    public StreamingResponseBody exportTimeModificationRequests(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) {
        
        try {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("開始日は終了日より前である必要があります");
            }

            String managerId = getCurrentEmployeeId();
            String filename = String.format("time_modifications_%s_%s.csv", 
                                           startDate.format(DATE_FORMATTER), 
                                           endDate.format(DATE_FORMATTER));
            
            setupCsvResponse(response, filename);
            
            return outputStream -> {
                try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                    // Write BOM for Excel compatibility
                    writer.write('\ufeff');
                    csvExportService.exportTimeModificationRequests(writer, startDate, endDate, managerId);
                } catch (IOException e) {
                    logger.error("Error exporting time modification requests", e);
                    throw new RuntimeException("CSV出力でエラーが発生しました", e);
                }
            };
            
        } catch (Exception e) {
            logger.error("Error setting up time modification requests export", e);
            throw new RuntimeException("CSV出力の準備でエラーが発生しました", e);
        }
    }

    @GetMapping("/team-summary")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "チームサマリーCSV出力", description = "指定期間のチームメンバーのサマリー情報をCSV形式で出力します")
    public StreamingResponseBody exportTeamSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) {
        
        try {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("開始日は終了日より前である必要があります");
            }

            String managerId = getCurrentEmployeeId();
            String filename = String.format("team_summary_%s_%s.csv", 
                                           startDate.format(DATE_FORMATTER), 
                                           endDate.format(DATE_FORMATTER));
            
            setupCsvResponse(response, filename);
            
            return outputStream -> {
                try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                    // Write BOM for Excel compatibility
                    writer.write('\ufeff');
                    csvExportService.exportTeamSummary(writer, startDate, endDate, managerId);
                } catch (IOException e) {
                    logger.error("Error exporting team summary", e);
                    throw new RuntimeException("CSV出力でエラーが発生しました", e);
                }
            };
            
        } catch (Exception e) {
            logger.error("Error setting up team summary export", e);
            throw new RuntimeException("CSV出力の準備でエラーが発生しました", e);
        }
    }

    private void setupCsvResponse(HttpServletResponse response, String filename) {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                          "attachment; filename=\"" + filename + "\"");
        
        // Set cache control headers
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setDateHeader(HttpHeaders.EXPIRES, 0);
    }

    private String getCurrentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("認証が必要です");
        }
        return authentication.getName();
    }
}