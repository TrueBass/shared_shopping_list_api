package com.example.shared_shopping_list_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "Username is required")
    @Pattern(
            regexp = "^[a-z0-9_.]{4,20}$",
            message = "Username must be 4-20 characters and contain only lowercase letters, digits, underscores and dots"
    )
    private String name;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
    private String password;

    private boolean rememberMe;
}