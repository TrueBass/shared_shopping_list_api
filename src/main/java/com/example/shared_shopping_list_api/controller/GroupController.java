package com.example.shared_shopping_list_api.controller;

import com.example.shared_shopping_list_api.dto.*;
import com.example.shared_shopping_list_api.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getGroups(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(groupService.getGroups(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(groupService.getGroup(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<GroupResponse> addMembers(
            @PathVariable Long id,
            @RequestBody AddMembersRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(groupService.addMembers(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        groupService.removeMember(id, userId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        groupService.deleteGroup(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}