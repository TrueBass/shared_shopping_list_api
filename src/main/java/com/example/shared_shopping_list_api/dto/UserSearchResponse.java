package com.example.shared_shopping_list_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponse {
    private Long id;
    private String name;
    private String email;
    private String friendshipStatus; // null, "PENDING_SENT", "PENDING_RECEIVED", "ACCEPTED"
    private Long friendshipId;
}