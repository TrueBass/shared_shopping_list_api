package com.example.shared_shopping_list_api.controller;

import com.example.shared_shopping_list_api.dto.FriendRequestResponse;
import com.example.shared_shopping_list_api.dto.FriendResponse;
import com.example.shared_shopping_list_api.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    @PostMapping("/requests/{userId}")
    public ResponseEntity<FriendRequestResponse> sendFriendRequest(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(friendshipService.sendFriendRequest(userId, userDetails.getUsername()));
    }

    @GetMapping("/requests/received")
    public ResponseEntity<List<FriendRequestResponse>> getReceivedRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendshipService.getReceivedRequests(userDetails.getUsername()));
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<FriendRequestResponse>> getSentRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendshipService.getSentRequests(userDetails.getUsername()));
    }

    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<FriendResponse> acceptFriendRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendshipService.acceptFriendRequest(requestId, userDetails.getUsername()));
    }

    @DeleteMapping("/requests/{requestId}")
    public ResponseEntity<Void> declineFriendRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        friendshipService.declineFriendRequest(requestId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/requests/by-username")
    public ResponseEntity<FriendRequestResponse> sendFriendRequestByUsername(
            @RequestParam String username,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(friendshipService.sendFriendRequestByUsername(username, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(
            @RequestParam(required = false) String q,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(friendshipService.getFriends(userDetails.getUsername(), q));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable Long friendId,
            @AuthenticationPrincipal UserDetails userDetails) {
        friendshipService.removeFriend(friendId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
