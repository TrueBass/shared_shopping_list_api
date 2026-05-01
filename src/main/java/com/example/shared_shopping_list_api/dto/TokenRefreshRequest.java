package com.example.shared_shopping_list_api.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
