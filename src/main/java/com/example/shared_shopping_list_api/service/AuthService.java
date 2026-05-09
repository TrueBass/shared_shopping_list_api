package com.example.shared_shopping_list_api.service;

import com.example.shared_shopping_list_api.dto.AuthResponse;
import com.example.shared_shopping_list_api.dto.LoginRequest;
import com.example.shared_shopping_list_api.dto.SignupRequest;
import com.example.shared_shopping_list_api.dto.UserResponse;
import com.example.shared_shopping_list_api.entity.RefreshToken;
import com.example.shared_shopping_list_api.entity.User;
import com.example.shared_shopping_list_api.repository.UserRepository;
import com.example.shared_shopping_list_api.security.JwtService;
import com.example.shared_shopping_list_api.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByName(request.getName())) {
            throw new ApiException("USERNAME_ALREADY_EXISTS", HttpStatus.BAD_REQUEST, "Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("EMAIL_ALREADY_EXISTS", HttpStatus.BAD_REQUEST, "Email is already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateToken(buildUserDetails(user));
        RefreshToken refreshToken = refreshTokenService.create(user, request.isRememberMe());
        return new AuthResponse(accessToken, refreshToken.getToken(), toUserResponse(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String accessToken = jwtService.generateToken(buildUserDetails(user));
        RefreshToken refreshToken = refreshTokenService.create(user, request.isRememberMe());
        return new AuthResponse(accessToken, refreshToken.getToken(), toUserResponse(user));
    }

    public AuthResponse refresh(String token) {
        RefreshToken old = refreshTokenService.validate(token);
        RefreshToken newRefreshToken = refreshTokenService.rotate(old);
        User user = newRefreshToken.getUser();
        String accessToken = jwtService.generateToken(buildUserDetails(user));
        return new AuthResponse(accessToken, newRefreshToken.getToken(), toUserResponse(user));
    }

    public void logout(String token) {
        refreshTokenService.deleteByToken(token);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getEmail(), user.getName());
    }

    private UserDetails buildUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
