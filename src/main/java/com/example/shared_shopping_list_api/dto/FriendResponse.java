package com.example.shared_shopping_list_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {
    private Long friendshipId;
    private Long userId;
    private String name;
    private String email;
    private LocalDateTime friendsSince;
}