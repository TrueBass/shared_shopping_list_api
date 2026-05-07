package com.example.shared_shopping_list_api.controller;

import com.example.shared_shopping_list_api.dto.ChangeNameRequest;
import com.example.shared_shopping_list_api.dto.ChangePasswordRequest;
import com.example.shared_shopping_list_api.dto.UserSearchResponse;
import com.example.shared_shopping_list_api.service.FriendshipService;
import com.example.shared_shopping_list_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final FriendshipService friendshipService;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<String> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userDetails.getUsername());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
            @RequestParam String q,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendshipService.searchUsers(q, userDetails.getUsername()));
    }

    @PatchMapping("/name")
    public ResponseEntity<Void> changeName(
            @Valid @RequestBody ChangeNameRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.changeName(request, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.changePassword(request, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
