package com.example.shared_shopping_list_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeNameRequest {

    @NotBlank(message = "Username is required")
    @Pattern(
            regexp = "^[a-z0-9_.]{4,20}$",
            message = "Username must be 4-20 characters and contain only lowercase letters, digits, underscores and dots"
    )
    private String name;
}
