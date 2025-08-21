package com.ams.controller;

import com.ams.dto.ApiResponses;
import com.ams.dto.time.AttendanceStatistics;
import com.ams.dto.time.AttendanceStatus;
import com.ams.dto.time.ClockRequest;
import com.ams.dto.time.TimeRecordDto;
import com.ams.entity.TimeRecord;
import com.ams.service.TimeRecordService;
import com.ams.util.TimeRecordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/time")
@Tag(name = "Time Tracking", description = "打刻関連のAPI")
public class TimeController {

    private static final Logger logger = LoggerFactory.getLogger(TimeController.class);

    @Autowired
    private TimeRecordService timeRecordService;

    @Autowired
    private TimeRecordMapper timeRecordMapper;

    @PostMapping("/clock-in")
    @Operation(
        summary = "出勤打刻",
        description = "従業員の出勤時刻を記録します。\n\n" +
                     "### 制限事項\n" +
                     "- 既に出勤打刻済みの場合はエラー\n" +
                     "- 休日（土日）は打刻不可\n" +
                     "- 承認済み休暇中は打刻不可\n" +
                     "- 打刻時間は6時〜23時の間のみ\n" +
                     "- 未来の日付での打刻は不可",
        tags = {"打刻"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "出勤打刻成功",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = com.ams.dto.ApiResponses.class),
            examples = @ExampleObject(
                name = "成功例",
                value = "{\n" +
                       "  \"success\": true,\n" +
                       "  \"message\": \"出勤打刻が完了しました\",\n" +
                       "  \"data\": {\n" +
                       "    \"id\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
                       "    \"employeeId\": \"EMP001\",\n" +
                       "    \"date\": \"2024-01-15\",\n" +
                       "    \"clockIn\": \"2024-01-15T09:00:00\",\n" +
                       "    \"status\": \"PRESENT\"\n" +
                       "  }\n" +
                       "}"
            )
        )
    )
    @ApiResponse(
        responseCode = "400",
        description = "不正なリクエスト",
        content = @Content(
            mediaType = "application/json",
            examples = {
                @ExampleObject(
                    name = "既に打刻済み",
                    value = "{\n" +
                           "  \"success\": false,\n" +
                           "  \"message\": \"既に出勤打刻済みです\"\n" +
                           "}"
                ),
                @ExampleObject(
                    name = "休日打刻エラー",
                    value = "{\n" +
                           "  \"success\": false,\n" +
                           "  \"message\": \"休日は出勤打刻できません\"\n" +
                           "}"
                )
            }
        )
    )
    @ApiResponse(
        responseCode = "401",
        description = "認証が必要です",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = "{\n" +
                       "  \"success\": false,\n" +
                       "  \"message\": \"認証が必要です\"\n" +
                       "}"
            )
        )
    )
    @SecurityRequirement(name = "bearer-jwt")
    public ResponseEntity<com.ams.dto.ApiResponses<TimeRecordDto>> clockIn(
        @Parameter(
            description = "打刻リクエスト情報",
            required = true,
            content = @Content(
                examples = @ExampleObject(
                    name = "打刻リクエスト例",
                    value = "{\n" +
                           "  \"timestamp\": \"2024-01-15T09:00:00\",\n" +
                           "  \"location\": \"本社オフィス\",\n" +
                           "  \"notes\": \"定時出勤\"\n" +
                           "}"
                )
            )
        )
        @Valid @RequestBody ClockRequest request) {
        try {
            String employeeId = getCurrentEmployeeId();
            
            LocalDateTime clockInTime = request.getTimestamp() != null ? 
                request.getTimestamp() : LocalDateTime.now();
            
            TimeRecord timeRecord = timeRecordService.clockIn(employeeId, clockInTime);
            TimeRecordDto dto = timeRecordMapper.toDto(timeRecord);
            
            return ResponseEntity.ok(ApiResponses.success(dto, "出勤打刻が完了しました"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during clock-in", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("出勤打刻でエラーが発生しました"));
        }
    }

    @PostMapping("/clock-out")
    @Operation(summary = "退勤打刻", description = "退勤時刻を記録します")
    public ResponseEntity<ApiResponses<TimeRecordDto>> clockOut(@Valid @RequestBody ClockRequest request) {
        try {
            String employeeId = getCurrentEmployeeId();
            
            LocalDateTime clockOutTime = request.getTimestamp() != null ? 
                request.getTimestamp() : LocalDateTime.now();
            
            TimeRecord timeRecord = timeRecordService.clockOut(employeeId, clockOutTime);
            TimeRecordDto dto = timeRecordMapper.toDto(timeRecord);
            
            return ResponseEntity.ok(ApiResponses.success(dto, "退勤打刻が完了しました"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during clock-out", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("退勤打刻でエラーが発生しました"));
        }
    }

    @PostMapping("/break-start")
    @Operation(summary = "休憩開始", description = "休憩開始時刻を記録します")
    public ResponseEntity<ApiResponses<TimeRecordDto>> startBreak(@Valid @RequestBody ClockRequest request) {
        try {
            String employeeId = getCurrentEmployeeId();
            
            LocalDateTime breakStartTime = request.getTimestamp() != null ? 
                request.getTimestamp() : LocalDateTime.now();
            
            TimeRecord timeRecord = timeRecordService.startBreak(employeeId, breakStartTime);
            TimeRecordDto dto = timeRecordMapper.toDto(timeRecord);
            
            return ResponseEntity.ok(ApiResponses.success(dto, "休憩開始が記録されました"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during break start", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("休憩開始でエラーが発生しました"));
        }
    }

    @PostMapping("/break-end")
    @Operation(summary = "休憩終了", description = "休憩終了時刻を記録します")
    public ResponseEntity<ApiResponses<TimeRecordDto>> endBreak(@Valid @RequestBody ClockRequest request) {
        try {
            String employeeId = getCurrentEmployeeId();
            
            LocalDateTime breakEndTime = request.getTimestamp() != null ? 
                request.getTimestamp() : LocalDateTime.now();
            
            TimeRecord timeRecord = timeRecordService.endBreak(employeeId, breakEndTime);
            TimeRecordDto dto = timeRecordMapper.toDto(timeRecord);
            
            return ResponseEntity.ok(ApiResponses.success(dto, "休憩終了が記録されました"));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during break end", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("休憩終了でエラーが発生しました"));
        }
    }

    @GetMapping("/today")
    @Operation(summary = "本日の勤怠取得", description = "本日の勤怠記録を取得します")
    public ResponseEntity<ApiResponses<TimeRecordDto>> getTodayRecord() {
        try {
            String employeeId = getCurrentEmployeeId();
            Optional<TimeRecord> timeRecord = timeRecordService.getTodayRecord(employeeId);
            
            if (timeRecord.isPresent()) {
                TimeRecordDto dto = timeRecordMapper.toDto(timeRecord.get());
                return ResponseEntity.ok(ApiResponses.success(dto));
            } else {
                return ResponseEntity.ok(ApiResponses.success(null, "本日の勤怠記録がありません"));
            }
        } catch (Exception e) {
            logger.error("Error getting today's record", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("勤怠記録の取得でエラーが発生しました"));
        }
    }

    @GetMapping("/history")
    @Operation(summary = "勤怠履歴取得", description = "指定期間の勤怠履歴を取得します")
    public ResponseEntity<ApiResponses<List<TimeRecordDto>>> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            String employeeId = getCurrentEmployeeId();
            
            // Validate date range
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(ApiResponses.error("開始日は終了日より前である必要があります"));
            }
            
            // Limit to maximum 31 days
            if (startDate.plusDays(31).isBefore(endDate)) {
                return ResponseEntity.badRequest().body(ApiResponses.error("検索期間は31日以内で指定してください"));
            }
            
            List<TimeRecord> timeRecords = timeRecordService.getTimeRecords(employeeId, startDate, endDate);
            List<TimeRecordDto> dtos = timeRecords.stream()
                    .map(timeRecordMapper::toDto)
                    .toList();
            
            return ResponseEntity.ok(ApiResponses.success(dtos));
        } catch (Exception e) {
            logger.error("Error getting time records", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("勤怠履歴の取得でエラーが発生しました"));
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "勤怠統計取得", description = "指定期間の勤怠統計を取得します")
    public ResponseEntity<ApiResponses<AttendanceStatistics>> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            String employeeId = getCurrentEmployeeId();
            
            // Validate date range
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body(ApiResponses.error("開始日は終了日より前である必要があります"));
            }
            
            Double averageHours = timeRecordService.getAverageWorkingHours(employeeId, startDate, endDate);
            Double totalHours = timeRecordService.getTotalWorkingHours(employeeId, startDate, endDate);
            
            // Calculate working days (weekdays only)
            long workingDays = startDate.datesUntil(endDate.plusDays(1))
                    .filter(date -> date.getDayOfWeek().getValue() <= 5)
                    .count();
            
            AttendanceStatistics statistics = new AttendanceStatistics(
                    startDate, endDate, averageHours, totalHours, workingDays);
            
            return ResponseEntity.ok(ApiResponses.success(statistics));
        } catch (Exception e) {
            logger.error("Error getting statistics", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("統計データの取得でエラーが発生しました"));
        }
    }

    @GetMapping("/status")
    @Operation(summary = "現在の状態取得", description = "現在の打刻状態を取得します")
    public ResponseEntity<ApiResponses<AttendanceStatus>> getCurrentStatus() {
        try {
            String employeeId = getCurrentEmployeeId();
            Optional<TimeRecord> todayRecord = timeRecordService.getTodayRecord(employeeId);
            
            if (todayRecord.isEmpty()) {
                AttendanceStatus status = new AttendanceStatus(
                        "not_clocked_in", "未出勤", true, false, false, false, null);
                return ResponseEntity.ok(ApiResponses.success(status));
            }
            
            TimeRecord record = todayRecord.get();
            String statusCode;
            String message;
            
            if (record.isOnBreak()) {
                statusCode = "on_break";
                message = "休憩中";
            } else if (record.isClockingIn()) {
                statusCode = "clocked_in";
                message = "勤務中";
            } else if (record.isCompleted()) {
                statusCode = "clocked_out";
                message = "退勤済み";
            } else {
                statusCode = "not_clocked_in";
                message = "未出勤";
            }
            
            AttendanceStatus status = new AttendanceStatus(
                    statusCode,
                    message,
                    record.getClockIn() == null,
                    record.getClockIn() != null && record.getClockOut() == null,
                    record.getClockIn() != null && record.getClockOut() == null && !record.isOnBreak(),
                    record.isOnBreak(),
                    timeRecordMapper.toDto(record)
            );
            
            return ResponseEntity.ok(ApiResponses.success(status));
        } catch (Exception e) {
            logger.error("Error getting current status", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("状態の取得でエラーが発生しました"));
        }
    }

    private String getCurrentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("認証が必要です");
        }
        return authentication.getName();
    }
}