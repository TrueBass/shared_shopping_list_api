package com.example.shared_shopping_list_api.service;

import com.example.shared_shopping_list_api.entity.RefreshToken;
import com.example.shared_shopping_list_api.entity.User;
import com.example.shared_shopping_list_api.repository.RefreshTokenRepository;
import com.example.shared_shopping_list_api.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration-days}")
    private int refreshExpirationDays;

    @Value("${jwt.refresh-expiration-days-remember-me}")
    private int refreshExpirationDaysRememberMe;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken create(User user, boolean rememberMe) {
        int days = rememberMe ? refreshExpirationDaysRememberMe : refreshExpirationDays;
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(days, ChronoUnit.DAYS))
                .rememberMe(rememberMe)
                .build();
        return refreshTokenRepository.save(token);
    }

    public RefreshToken validate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("INVALID_REFRESH_TOKEN", HttpStatus.UNAUTHORIZED, "Refresh token is invalid"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new ApiException("REFRESH_TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED, "Refresh token has expired");
        }

        return refreshToken;
    }

    @Transactional
    public RefreshToken rotate(RefreshToken old) {
        refreshTokenRepository.delete(old);
        return create(old.getUser(), old.isRememberMe());
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }
}
