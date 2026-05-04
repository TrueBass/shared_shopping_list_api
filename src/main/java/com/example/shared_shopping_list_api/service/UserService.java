package com.example.shared_shopping_list_api.service;

import com.example.shared_shopping_list_api.dto.ChangeNameRequest;
import com.example.shared_shopping_list_api.dto.ChangePasswordRequest;
import com.example.shared_shopping_list_api.entity.User;
import com.example.shared_shopping_list_api.exception.ApiException;
import com.example.shared_shopping_list_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changeName(ChangeNameRequest request, String email) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ApiException("NAME_EMPTY", HttpStatus.BAD_REQUEST, "Name cannot be empty");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"));

        user.setName(request.getName().trim());
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String email) {
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new ApiException("NEW_PASSWORD_EMPTY", HttpStatus.BAD_REQUEST, "New password cannot be empty");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ApiException("INCORRECT_CURRENT_PASSWORD", HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}