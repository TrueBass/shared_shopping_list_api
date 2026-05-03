package com.example.shared_shopping_list_api.dto;

import com.example.shared_shopping_list_api.entity.ShoppingItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {
    private Long id;
    private String name;
    private Integer quantity;
    private String unit;
    private String category;
    private GroupMemberResponse addedBy;
    private GroupMemberResponse checkedBy;
    private LocalDateTime checkedAt;
    private LocalDateTime createdAt;

    public static ItemResponse from(ShoppingItem item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getQuantity(),
                item.getUnit(),
                item.getCategory(),
                GroupMemberResponse.from(item.getAddedBy()),
                item.getCheckedBy() != null ? GroupMemberResponse.from(item.getCheckedBy()) : null,
                item.getCheckedAt(),
                item.getCreatedAt()
        );
    }
}
