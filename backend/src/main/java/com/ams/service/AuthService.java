package com.ams.service;

import com.ams.dto.auth.LoginRequest;
import com.ams.dto.auth.LoginResponse;
import com.ams.dto.user.UserDto;
import com.ams.entity.RefreshToken;
import com.ams.entity.User;
import com.ams.security.JwtTokenProvider;
import com.ams.util.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmployeeId(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user details
            User user = (User) authentication.getPrincipal();
            
            // Generate tokens
            String accessToken = tokenProvider.createAccessToken(authentication);
            RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user);
            String refreshToken = tokenProvider.createRefreshToken(user.getEmployeeId());

            // Convert user to DTO
            UserDto userDto = userMapper.toDto(user);

            LoginResponse response = new LoginResponse(accessToken, refreshToken, userDto);
            
            logger.info("User {} authenticated successfully", user.getEmployeeId());
            return response;

        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for user: {}", loginRequest.getEmployeeId());
            throw new IllegalArgumentException("社員番号またはパスワードが正しくありません");
        }
    }

    public LoginResponse refreshToken(String refreshToken) {
        try {
            // Validate refresh token
            if (!refreshTokenService.validateRefreshToken(refreshToken)) {
                throw new IllegalArgumentException("無効なリフレッシュトークンです");
            }

            // Get user from refresh token
            String employeeId = tokenProvider.getEmployeeIdFromToken(refreshToken);
            Optional<User> userOpt = userService.findActiveByEmployeeId(employeeId);
            
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("ユーザーが見つかりません");
            }

            User user = userOpt.get();

            // Generate new access token
            String newAccessToken = tokenProvider.createAccessToken(user.getEmployeeId(), user.getRole().name());

            // Optionally generate new refresh token (for token rotation)
            RefreshToken newRefreshTokenEntity = refreshTokenService.createRefreshToken(user);
            String newRefreshToken = tokenProvider.createRefreshToken(user.getEmployeeId());

            // Revoke old refresh token
            refreshTokenService.revokeToken(refreshToken);

            // Convert user to DTO
            UserDto userDto = userMapper.toDto(user);

            LoginResponse response = new LoginResponse(newAccessToken, newRefreshToken, userDto);
            
            logger.debug("Token refreshed for user: {}", user.getEmployeeId());
            return response;

        } catch (Exception e) {
            logger.error("Error refreshing token", e);
            throw new IllegalArgumentException("トークンの更新に失敗しました");
        }
    }

    public void logout(String employeeId) {
        try {
            // Revoke all refresh tokens for the user
            refreshTokenService.revokeAllTokensForUser(employeeId);
            
            // Clear security context
            SecurityContextHolder.clearContext();
            
            logger.info("User {} logged out successfully", employeeId);
            
        } catch (Exception e) {
            logger.error("Error during logout for user: {}", employeeId, e);
            throw new RuntimeException("ログアウト処理でエラーが発生しました");
        }
    }

    public void logoutFromAllDevices(String employeeId) {
        try {
            // Revoke all refresh tokens for the user
            refreshTokenService.revokeAllTokensForUser(employeeId);
            
            logger.info("User {} logged out from all devices", employeeId);
            
        } catch (Exception e) {
            logger.error("Error during logout from all devices for user: {}", employeeId, e);
            throw new RuntimeException("全デバイスからのログアウト処理でエラーが発生しました");
        }
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("認証されていません");
        }

        User user = (User) authentication.getPrincipal();
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserManager() {
        try {
            UserDto currentUser = getCurrentUser();
            return currentUser.getRole().name().equals("MANAGER");
        } catch (Exception e) {
            return false;
        }
    }
}