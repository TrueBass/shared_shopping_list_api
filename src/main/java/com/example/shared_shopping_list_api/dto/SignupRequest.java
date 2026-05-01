package com.example.shared_shopping_list_api.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private boolean rememberMe;
}