package com.ams.service;

import com.ams.entity.RefreshToken;
import com.ams.entity.User;
import com.ams.exception.ResourceNotFoundException;
import com.ams.repository.RefreshTokenRepository;
import com.ams.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    public RefreshToken createRefreshToken(User user) {
        // Revoke existing tokens for the user to maintain single session
        revokeAllTokensForUser(user.getEmployeeId());

        // Generate new refresh token
        String tokenValue = jwtTokenProvider.createRefreshToken(user.getEmployeeId());
        String tokenHash = hashToken(tokenValue);
        LocalDateTime expiresAt = jwtTokenProvider.getExpirationFromToken(tokenValue).toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();

        RefreshToken refreshToken = new RefreshToken(user, tokenHash, expiresAt);
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        
        logger.debug("Created refresh token for user: {}", user.getEmployeeId());
        return savedToken;
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        String tokenHash = hashToken(token);
        return refreshTokenRepository.findByTokenHash(tokenHash);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    public boolean validateRefreshToken(String token) {
        try {
            // First validate JWT structure and signature
            if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isRefreshToken(token)) {
                return false;
            }

            // Then check if token exists in database and is not revoked
            Optional<RefreshToken> refreshToken = findByToken(token);
            if (refreshToken.isEmpty()) {
                return false;
            }

            // Verify expiration
            RefreshToken dbToken = refreshToken.get();
            return dbToken.isValid();

        } catch (Exception e) {
            logger.error("Error validating refresh token", e);
            return false;
        }
    }

    public void revokeToken(String token) {
        Optional<RefreshToken> refreshToken = findByToken(token);
        if (refreshToken.isPresent()) {
            RefreshToken dbToken = refreshToken.get();
            dbToken.revoke();
            refreshTokenRepository.save(dbToken);
            logger.debug("Revoked refresh token for user: {}", dbToken.getUser().getEmployeeId());
        }
    }

    public void revokeAllTokensForUser(String employeeId) {
        refreshTokenRepository.revokeAllByEmployeeId(employeeId);
        logger.debug("Revoked all refresh tokens for user: {}", employeeId);
    }

    @Transactional(readOnly = true)
    public List<RefreshToken> getValidTokensForUser(String employeeId) {
        return refreshTokenRepository.findValidTokensByEmployeeId(employeeId, LocalDateTime.now());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }

    // Scheduled task to clean up expired tokens (runs daily at 2 AM)
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        logger.info("Starting cleanup of expired refresh tokens");
        refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
        logger.info("Completed cleanup of expired refresh tokens");
    }
}