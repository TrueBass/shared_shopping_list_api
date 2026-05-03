package com.example.shared_shopping_list_api.controller;

import com.example.shared_shopping_list_api.dto.CreateItemRequest;
import com.example.shared_shopping_list_api.dto.ItemResponse;
import com.example.shared_shopping_list_api.service.ShoppingItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/items")
@RequiredArgsConstructor
public class ShoppingItemController {

    private final ShoppingItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponse> addItem(
            @PathVariable Long groupId,
            @RequestBody CreateItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemService.addItem(groupId, request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<ItemResponse>> getItems(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(itemService.getItems(groupId, userDetails.getUsername()));
    }

    @PatchMapping("/{itemId}/toggle")
    public ResponseEntity<ItemResponse> toggleItem(
            @PathVariable Long groupId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(itemService.toggleItem(groupId, itemId, userDetails.getUsername()));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long groupId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        itemService.deleteItem(groupId, itemId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
