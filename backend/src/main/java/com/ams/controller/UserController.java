package com.ams.controller;

import com.ams.dto.ApiResponses;
import com.ams.dto.user.UserDto;
import com.ams.entity.User;
import com.ams.service.AuthService;
import com.ams.service.UserService;
import com.ams.util.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "ユーザー管理関連のAPI")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/profile")
    @Operation(summary = "プロフィール取得", description = "現在のユーザーのプロフィール情報を取得します")
    public ResponseEntity<ApiResponses<UserDto>> getProfile() {
        try {
            UserDto currentUser = authService.getCurrentUser();
            return ResponseEntity.ok(ApiResponses.success(currentUser));
        } catch (Exception e) {
            logger.error("Error getting user profile", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("プロフィールの取得でエラーが発生しました"));
        }
    }

    @PutMapping("/profile")
    @Operation(summary = "プロフィール更新", description = "現在のユーザーのプロフィール情報を更新します")
    public ResponseEntity<ApiResponses<UserDto>> updateProfile(@Valid @RequestBody UserDto userDto) {
        try {
            String currentEmployeeId = getCurrentEmployeeId();
            User currentUser = userService.getUserByEmployeeId(currentEmployeeId);
            
            // Only allow updating certain fields for security
            User updates = new User();
            updates.setName(userDto.getName());
            updates.setEmail(userDto.getEmail());
            
            User updatedUser = userService.updateUser(currentUser.getId(), updates);
            UserDto updatedDto = userMapper.toDto(updatedUser);
            
            return ResponseEntity.ok(ApiResponses.success(updatedDto, "プロフィールを更新しました"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating user profile", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("プロフィールの更新でエラーが発生しました"));
        }
    }

    @PostMapping("/change-password")
    @Operation(summary = "パスワード変更", description = "現在のユーザーのパスワードを変更します")
    public ResponseEntity<ApiResponses<Void>> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            String currentEmployeeId = getCurrentEmployeeId();
            
            // Validate new password
            if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest().body(ApiResponses.error("新しいパスワードは6文字以上である必要があります"));
            }
            
            userService.changePassword(currentEmployeeId, request.getNewPassword());
            
            return ResponseEntity.ok(ApiResponses.successMessage("パスワードを変更しました"));
        } catch (Exception e) {
            logger.error("Error changing password", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("パスワードの変更でエラーが発生しました"));
        }
    }

    // Manager-only endpoints
    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "ユーザー一覧取得", description = "全てのアクティブユーザーを取得します（管理者のみ）")
    public ResponseEntity<ApiResponses<List<UserDto>>> getAllUsers() {
        try {
            List<User> users = userService.getAllActiveUsers();
            List<UserDto> userDtos = users.stream()
                    .map(userMapper::toDto)
                    .toList();
            
            return ResponseEntity.ok(ApiResponses.success(userDtos));
        } catch (Exception e) {
            logger.error("Error getting all users", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("ユーザー一覧の取得でエラーが発生しました"));
        }
    }

    @GetMapping("/team")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "チームメンバー取得", description = "管理している部署のメンバーを取得します（管理者のみ）")
    public ResponseEntity<ApiResponses<List<UserDto>>> getTeamMembers() {
        try {
            String managerId = getCurrentEmployeeId();
            List<User> teamMembers = userService.getTeamMembersByManagerId(managerId);
            List<UserDto> teamMemberDtos = teamMembers.stream()
                    .map(userMapper::toDto)
                    .toList();
            
            return ResponseEntity.ok(ApiResponses.success(teamMemberDtos));
        } catch (Exception e) {
            logger.error("Error getting team members", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("チームメンバーの取得でエラーが発生しました"));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "ユーザー作成", description = "新しいユーザーを作成します（管理者のみ）")
    public ResponseEntity<ApiResponses<UserDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User newUser = new User();
            newUser.setEmployeeId(request.getEmployeeId());
            newUser.setName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setPasswordHash(request.getPassword()); // Will be encoded by service
            newUser.setRole(request.getRole());
            
            User createdUser = userService.createUser(newUser);
            UserDto createdDto = userMapper.toDto(createdUser);
            
            return ResponseEntity.ok(ApiResponses.success(createdDto, "ユーザーを作成しました"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponses.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating user", e);
            return ResponseEntity.internalServerError().body(ApiResponses.error("ユーザーの作成でエラーが発生しました"));
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
    public static class ChangePasswordRequest {
        private String newPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    public static class CreateUserRequest {
        @jakarta.validation.constraints.NotBlank
        private String employeeId;
        
        @jakarta.validation.constraints.NotBlank
        private String name;
        
        private String email;
        
        @jakarta.validation.constraints.NotBlank
        private String password;
        
        @jakarta.validation.constraints.NotNull
        private com.ams.entity.enums.UserRole role;

        public String getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(String employeeId) {
            this.employeeId = employeeId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public com.ams.entity.enums.UserRole getRole() {
            return role;
        }

        public void setRole(com.ams.entity.enums.UserRole role) {
            this.role = role;
        }
    }
}