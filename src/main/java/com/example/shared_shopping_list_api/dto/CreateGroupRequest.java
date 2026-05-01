package com.example.shared_shopping_list_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateGroupRequest {
    private String name;
    private String emoji;
    private List<Long> memberIds;
}