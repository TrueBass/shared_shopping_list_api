package com.example.shared_shopping_list_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {
    private Long id;
    private String name;
    private String emoji;
    private GroupMemberResponse owner;
    private List<GroupMemberResponse> members;
    private LocalDateTime createdAt;
}