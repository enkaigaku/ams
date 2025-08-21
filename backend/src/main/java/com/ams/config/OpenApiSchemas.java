package com.ams.config;

import io.swagger.v3.oas.annotations.media.Schema;

public class OpenApiSchemas {

    @Schema(description = "API共通レスポンス形式")
    public static class ApiResponseSchema<T> {
        
        @Schema(description = "処理成功フラグ", example = "true")
        public boolean success;
        
        @Schema(description = "レスポンスメッセージ", example = "処理が正常に完了しました")
        public String message;
        
        @Schema(description = "レスポンスデータ")
        public T data;
        
        @Schema(description = "タイムスタンプ", example = "2024-01-15T09:00:00")
        public String timestamp;
    }

    @Schema(description = "エラーレスポンス")
    public static class ErrorResponse {
        
        @Schema(description = "処理成功フラグ", example = "false")
        public boolean success;
        
        @Schema(description = "エラーメッセージ", example = "入力値に誤りがあります")
        public String message;
        
        @Schema(description = "タイムスタンプ", example = "2024-01-15T09:00:00")
        public String timestamp;
    }

    @Schema(description = "認証エラーレスポンス")
    public static class AuthErrorResponse {
        
        @Schema(description = "処理成功フラグ", example = "false")
        public boolean success;
        
        @Schema(description = "エラーメッセージ", example = "認証が必要です")
        public String message;
        
        @Schema(description = "タイムスタンプ", example = "2024-01-15T09:00:00")
        public String timestamp;
    }

    @Schema(description = "バリデーションエラーレスポンス")
    public static class ValidationErrorResponse {
        
        @Schema(description = "処理成功フラグ", example = "false")
        public boolean success;
        
        @Schema(description = "エラーメッセージ", example = "入力値に誤りがあります: name=名前は必須です")
        public String message;
        
        @Schema(description = "タイムスタンプ", example = "2024-01-15T09:00:00")
        public String timestamp;
    }

    @Schema(description = "勤怠記録")
    public static class TimeRecordSchema {
        
        @Schema(description = "勤怠記録ID", example = "123e4567-e89b-12d3-a456-426614174000")
        public String id;
        
        @Schema(description = "従業員ID", example = "EMP001")
        public String employeeId;
        
        @Schema(description = "従業員名", example = "田中太郎")
        public String employeeName;
        
        @Schema(description = "対象日", example = "2024-01-15")
        public String date;
        
        @Schema(description = "出勤時刻", example = "2024-01-15T09:00:00")
        public String clockIn;
        
        @Schema(description = "退勤時刻", example = "2024-01-15T18:00:00")
        public String clockOut;
        
        @Schema(description = "休憩開始時刻", example = "2024-01-15T12:00:00")
        public String breakStart;
        
        @Schema(description = "休憩終了時刻", example = "2024-01-15T13:00:00")
        public String breakEnd;
        
        @Schema(description = "総労働時間（時間）", example = "8.0")
        public Double totalHours;
        
        @Schema(description = "出勤状況", example = "PRESENT", allowableValues = {"PRESENT", "LATE", "ABSENT", "EARLY_LEAVE"})
        public String status;
        
        @Schema(description = "備考", example = "定時出勤")
        public String notes;
    }

    @Schema(description = "休暇申請")
    public static class LeaveRequestSchema {
        
        @Schema(description = "申請ID", example = "123e4567-e89b-12d3-a456-426614174000")
        public String id;
        
        @Schema(description = "従業員ID", example = "EMP001")
        public String employeeId;
        
        @Schema(description = "従業員名", example = "田中太郎")
        public String employeeName;
        
        @Schema(description = "休暇種別", example = "ANNUAL", allowableValues = {"ANNUAL", "SICK", "SPECIAL", "MATERNITY", "PATERNITY"})
        public String type;
        
        @Schema(description = "開始日", example = "2024-01-20")
        public String startDate;
        
        @Schema(description = "終了日", example = "2024-01-22")
        public String endDate;
        
        @Schema(description = "理由", example = "有給休暇取得")
        public String reason;
        
        @Schema(description = "申請状況", example = "PENDING", allowableValues = {"PENDING", "APPROVED", "REJECTED"})
        public String status;
        
        @Schema(description = "申請日", example = "2024-01-15T10:00:00")
        public String createdAt;
        
        @Schema(description = "承認者ID", example = "MGR001")
        public String approverEmployeeId;
        
        @Schema(description = "承認日", example = "2024-01-16T14:00:00")
        public String approvedAt;
        
        @Schema(description = "却下理由", example = "業務都合により承認できません")
        public String rejectionReason;
    }

    @Schema(description = "打刻リクエスト")
    public static class ClockRequestSchema {
        
        @Schema(description = "打刻時刻", example = "2024-01-15T09:00:00", required = true)
        public String timestamp;
        
        @Schema(description = "場所", example = "本社オフィス")
        public String location;
        
        @Schema(description = "備考", example = "定時出勤")
        public String notes;
    }

    @Schema(description = "ログインリクエスト")
    public static class LoginRequestSchema {
        
        @Schema(description = "従業員ID", example = "EMP001", required = true)
        public String employeeId;
        
        @Schema(description = "パスワード", example = "password123", required = true)
        public String password;
    }

    @Schema(description = "ログインレスポンス")
    public static class LoginResponseSchema {
        
        @Schema(description = "アクセストークン", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        public String accessToken;
        
        @Schema(description = "リフレッシュトークン", example = "refresh_eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        public String refreshToken;
        
        @Schema(description = "トークンタイプ", example = "Bearer")
        public String tokenType;
        
        @Schema(description = "トークン有効期限（秒）", example = "3600")
        public Long expiresIn;
        
        @Schema(description = "ユーザー情報")
        public UserSchema user;
    }

    @Schema(description = "ユーザー情報")
    public static class UserSchema {
        
        @Schema(description = "ユーザーID", example = "123e4567-e89b-12d3-a456-426614174000")
        public String id;
        
        @Schema(description = "従業員ID", example = "EMP001")
        public String employeeId;
        
        @Schema(description = "従業員名", example = "田中太郎")
        public String name;
        
        @Schema(description = "メールアドレス", example = "tanaka@company.com")
        public String email;
        
        @Schema(description = "役職", example = "EMPLOYEE", allowableValues = {"EMPLOYEE", "MANAGER", "ADMIN"})
        public String role;
        
        @Schema(description = "部署名", example = "開発部")
        public String departmentName;
        
        @Schema(description = "入社日", example = "2023-04-01")
        public String hireDate;
        
        @Schema(description = "アクティブフラグ", example = "true")
        public Boolean active;
    }
}