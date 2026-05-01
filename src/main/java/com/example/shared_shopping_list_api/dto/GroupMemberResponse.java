package com.example.shared_shopping_list_api.dto;

import com.example.shared_shopping_list_api.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberResponse {
    private Long id;
    private String name;
    private String email;

    public static GroupMemberResponse from(User user) {
        return new GroupMemberResponse(user.getId(), user.getName(), user.getEmail());
    }
}