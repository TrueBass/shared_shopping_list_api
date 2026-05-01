package com.example.shared_shopping_list_api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AddMembersRequest {
    private List<Long> memberIds;
}