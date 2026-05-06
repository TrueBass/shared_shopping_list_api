package com.example.shared_shopping_list_api.service;

import com.example.shared_shopping_list_api.dto.ChangeNameRequest;
import com.example.shared_shopping_list_api.dto.ChangePasswordRequest;
import com.example.shared_shopping_list_api.entity.Group;
import com.example.shared_shopping_list_api.entity.User;
import com.example.shared_shopping_list_api.exception.ApiException;
import com.example.shared_shopping_list_api.repository.FriendshipRepository;
import com.example.shared_shopping_list_api.repository.GroupRepository;
import com.example.shared_shopping_list_api.repository.RefreshTokenRepository;
import com.example.shared_shopping_list_api.repository.ShoppingItemRepository;
import com.example.shared_shopping_list_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FriendshipRepository friendshipRepository;
    private final GroupRepository groupRepository;
    private final ShoppingItemRepository itemRepository;

    @Transactional
    public void changeName(ChangeNameRequest request, String email) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ApiException("NAME_EMPTY", HttpStatus.BAD_REQUEST, "Name cannot be empty");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"));

        String newName = request.getName().trim();
        if (userRepository.existsByNameAndIdNot(newName, user.getId())) {
            throw new ApiException("USERNAME_ALREADY_EXISTS", HttpStatus.BAD_REQUEST, "Username is already taken");
        }

        user.setName(newName);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String email) {
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new ApiException("NEW_PASSWORD_EMPTY", HttpStatus.BAD_REQUEST, "New password cannot be empty");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ApiException("INCORRECT_CURRENT_PASSWORD", HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"));

        // Nullify checkedBy across all groups so checked items become unchecked again
        itemRepository.nullifyCheckedBy(user);

        // Nullify addedBy across all groups so items stay visible to remaining members
        itemRepository.nullifyAddedBy(user);

        // Handle groups
        List<Group> allGroups = groupRepository.findByMembersContaining(user);
        for (Group group : allGroups) {
            if (group.getOwner().getId().equals(user.getId())) {
                Set<User> otherMembers = group.getMembers().stream()
                        .filter(m -> !m.getId().equals(user.getId()))
                        .collect(Collectors.toSet());
                if (otherMembers.isEmpty()) {
                    itemRepository.deleteAllByGroup(group);
                    groupRepository.delete(group);
                } else {
                    group.setOwner(otherMembers.iterator().next());
                    group.getMembers().removeIf(m -> m.getId().equals(user.getId()));
                    groupRepository.save(group);
                }
            } else {
                group.getMembers().removeIf(m -> m.getId().equals(user.getId()));
                groupRepository.save(group);
            }
        }

        refreshTokenRepository.deleteByUser(user);
        friendshipRepository.deleteAllByUser(user);
        userRepository.delete(user);
    }
}