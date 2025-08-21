package com.ams.controller;

import com.ams.dto.ApiResponses;
import com.ams.dto.auth.LoginRequest;
import com.ams.dto.auth.LoginResponse;
import com.ams.dto.auth.RefreshTokenRequest;
import com.ams.dto.user.UserDto;
import com.ams.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "認証関連のAPI")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "ユーザーログイン", description = "社員番号とパスワードでログインしてJWTトークンを取得します")
    public ResponseEntity<ApiResponses<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse loginResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(ApiResponses.success(loginResponse, "ログインに成功しました"));
        } catch (IllegalArgumentException e) {
            logger.warn("Login failed for user: {}", loginRequest.getEmployeeId());
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("ログイン処理でエラーが発生しました"));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "トークン更新", description = "リフレッシュトークンを使用してアクセストークンを更新します")
    public ResponseEntity<ApiResponses<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            LoginResponse loginResponse = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ApiResponses.success(loginResponse, "トークンを更新しました"));
        } catch (IllegalArgumentException e) {
            logger.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during token refresh", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("トークン更新でエラーが発生しました"));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "ログアウト", description = "現在のユーザーをログアウトしてリフレッシュトークンを無効化します")
    public ResponseEntity<ApiResponses<Void>> logout() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String employeeId = authentication.getName();
                authService.logout(employeeId);
            }
            return ResponseEntity.ok(ApiResponses.successMessage("ログアウトしました"));
        } catch (Exception e) {
            logger.error("Error during logout", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("ログアウト処理でエラーが発生しました"));
        }
    }

    @PostMapping("/logout-all")
    @Operation(summary = "全デバイスからログアウト", description = "全てのデバイスからログアウトして全てのリフレッシュトークンを無効化します")
    public ResponseEntity<ApiResponses<Void>> logoutFromAllDevices() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String employeeId = authentication.getName();
                authService.logoutFromAllDevices(employeeId);
            }
            return ResponseEntity.ok(ApiResponses.successMessage("全デバイスからログアウトしました"));
        } catch (Exception e) {
            logger.error("Error during logout from all devices", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("全デバイスログアウト処理でエラーが発生しました"));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "現在のユーザー情報取得", description = "認証されたユーザーの情報を取得します")
    public ResponseEntity<ApiResponses<UserDto>> getCurrentUser() {
        try {
            UserDto currentUser = authService.getCurrentUser();
            return ResponseEntity.ok(ApiResponses.success(currentUser));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponses.error("認証が必要です"));
        } catch (Exception e) {
            logger.error("Error getting current user", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("ユーザー情報の取得でエラーが発生しました"));
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "トークン検証", description = "現在のアクセストークンが有効かどうかを確認します")
    public ResponseEntity<ApiResponses<Boolean>> validateToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isValid = authentication != null && authentication.isAuthenticated() && 
                            !authentication.getPrincipal().equals("anonymousUser");
            
            if (isValid) {
                return ResponseEntity.ok(ApiResponses.success(true, "トークンは有効です"));
            } else {
                return ResponseEntity.ok(ApiResponses.success(false, "トークンが無効です"));
            }
        } catch (Exception e) {
            logger.error("Error validating token", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("トークン検証でエラーが発生しました"));
        }
    }
}