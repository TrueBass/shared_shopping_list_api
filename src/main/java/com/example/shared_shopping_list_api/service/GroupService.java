package com.example.shared_shopping_list_api.service;

import com.example.shared_shopping_list_api.dto.*;
import com.example.shared_shopping_list_api.entity.Group;
import com.example.shared_shopping_list_api.entity.User;
import com.example.shared_shopping_list_api.exception.ApiException;
import com.example.shared_shopping_list_api.repository.GroupRepository;
import com.example.shared_shopping_list_api.repository.ShoppingItemRepository;
import com.example.shared_shopping_list_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ShoppingItemRepository itemRepository;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, String email) {
        User owner = findUserByEmail(email);

        Set<User> members = new HashSet<>();
        members.add(owner);

        if (request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
                if (!memberId.equals(owner.getId())) {
                    User member = userRepository.findById(memberId)
                            .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found: " + memberId));
                    members.add(member);
                }
            }
        }

        Group group = Group.builder()
                .name(request.getName())
                .emoji(request.getEmoji())
                .owner(owner)
                .members(members)
                .build();

        return toResponse(groupRepository.save(group));
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getGroups(String email) {
        User user = findUserByEmail(email);
        return groupRepository.findByMembersContaining(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(Long id, String email) {
        User user = findUserByEmail(email);
        Group group = findGroupById(id);

        boolean isMember = group.getMembers().stream()
                .anyMatch(m -> m.getId().equals(user.getId()));
        if (!isMember) {
            throw new ApiException("NOT_GROUP_MEMBER", HttpStatus.FORBIDDEN, "You are not a member of this group");
        }

        return toResponse(group);
    }

    @Transactional
    public GroupResponse addMembers(Long id, AddMembersRequest request, String email) {
        User currentUser = findUserByEmail(email);
        Group group = findGroupById(id);
        requireOwner(group, currentUser);

        for (Long memberId : request.getMemberIds()) {
            User newMember = userRepository.findById(memberId)
                    .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found: " + memberId));

            boolean alreadyMember = group.getMembers().stream()
                    .anyMatch(m -> m.getId().equals(newMember.getId()));
            if (alreadyMember) {
                throw new ApiException("ALREADY_GROUP_MEMBER", HttpStatus.BAD_REQUEST, "User " + memberId + " is already a member of this group");
            }

            group.getMembers().add(newMember);
        }

        return toResponse(groupRepository.save(group));
    }

    @Transactional
    public void removeMember(Long groupId, Long userId, String email) {
        User currentUser = findUserByEmail(email);
        Group group = findGroupById(groupId);
        requireOwner(group, currentUser);

        if (userId.equals(group.getOwner().getId())) {
            throw new ApiException("CANNOT_REMOVE_OWNER", HttpStatus.BAD_REQUEST, "The group owner cannot be removed");
        }

        User memberToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found"));

        boolean isMember = group.getMembers().stream()
                .anyMatch(m -> m.getId().equals(memberToRemove.getId()));
        if (!isMember) {
            throw new ApiException("NOT_GROUP_MEMBER", HttpStatus.BAD_REQUEST, "User is not a member of this group");
        }

        itemRepository.deleteUncheckedItemsByGroupAndUser(group, memberToRemove);
        group.getMembers().removeIf(m -> m.getId().equals(memberToRemove.getId()));
        groupRepository.save(group);
    }

    @Transactional
    public void deleteGroup(Long id, String email) {
        User currentUser = findUserByEmail(email);
        Group group = findGroupById(id);
        requireOwner(group, currentUser);
        groupRepository.delete(group);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    private Group findGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ApiException("GROUP_NOT_FOUND", HttpStatus.NOT_FOUND, "Group not found"));
    }

    private void requireOwner(Group group, User user) {
        if (!group.getOwner().getId().equals(user.getId())) {
            throw new ApiException("NOT_GROUP_OWNER", HttpStatus.FORBIDDEN, "Only the group owner can perform this action");
        }
    }

    private GroupResponse toResponse(Group group) {
        List<GroupMemberResponse> memberResponses = group.getMembers().stream()
                .map(GroupMemberResponse::from)
                .toList();

        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getEmoji(),
                GroupMemberResponse.from(group.getOwner()),
                memberResponses,
                group.getCreatedAt()
        );
    }
}
