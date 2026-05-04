package com.example.shared_shopping_list_api.service;

import com.example.shared_shopping_list_api.dto.CreateItemRequest;
import com.example.shared_shopping_list_api.dto.ItemResponse;
import com.example.shared_shopping_list_api.entity.Group;
import com.example.shared_shopping_list_api.entity.ShoppingItem;
import com.example.shared_shopping_list_api.entity.User;
import com.example.shared_shopping_list_api.exception.ApiException;
import com.example.shared_shopping_list_api.repository.GroupRepository;
import com.example.shared_shopping_list_api.repository.ShoppingItemRepository;
import com.example.shared_shopping_list_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingItemService {

    private final ShoppingItemRepository itemRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Transactional
    public ItemResponse addItem(Long groupId, CreateItemRequest request, String email) {
        User user = findUserByEmail(email);
        Group group = findGroupById(groupId);
        requireMember(group, user);

        if (request.getName() == null || request.getName().isBlank()) {
            throw new ApiException("ITEM_NAME_EMPTY", HttpStatus.BAD_REQUEST, "Item name cannot be empty");
        }

        ShoppingItem item = ShoppingItem.builder()
                .name(request.getName().trim())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .category(request.getCategory())
                .group(group)
                .addedBy(user)
                .build();

        return ItemResponse.from(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getItems(Long groupId, String email) {
        User user = findUserByEmail(email);
        Group group = findGroupById(groupId);
        requireMember(group, user);

        return itemRepository.findByGroupOrderByCreatedAtAsc(group).stream()
                .map(ItemResponse::from)
                .toList();
    }

    @Transactional
    public ItemResponse toggleItem(Long groupId, Long itemId, String email) {
        User user = findUserByEmail(email);
        Group group = findGroupById(groupId);
        requireMember(group, user);

        ShoppingItem item = findItemInGroup(itemId, groupId);

        if (item.getCheckedBy() == null) {
            item.setCheckedBy(user);
            item.setCheckedAt(LocalDateTime.now());
        } else {
            if (!item.getCheckedBy().getId().equals(user.getId())) {
                throw new ApiException("NOT_ITEM_CHECKER", HttpStatus.FORBIDDEN, "Only the user who checked this item can uncheck it");
            }
            item.setCheckedBy(null);
            item.setCheckedAt(null);
        }

        return ItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long groupId, Long itemId, String email) {
        User user = findUserByEmail(email);
        Group group = findGroupById(groupId);
        requireMember(group, user);

        ShoppingItem item = findItemInGroup(itemId, groupId);

        if (!item.getAddedBy().getId().equals(user.getId())) {
            throw new ApiException("NOT_ITEM_OWNER", HttpStatus.FORBIDDEN, "Only the member who added this item can delete it");
        }

        itemRepository.delete(item);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    private Group findGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ApiException("GROUP_NOT_FOUND", HttpStatus.NOT_FOUND, "Group not found"));
    }

    private ShoppingItem findItemInGroup(Long itemId, Long groupId) {
        ShoppingItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException("ITEM_NOT_FOUND", HttpStatus.NOT_FOUND, "Item not found"));
        if (!item.getGroup().getId().equals(groupId)) {
            throw new ApiException("ITEM_NOT_FOUND", HttpStatus.NOT_FOUND, "Item not found");
        }
        return item;
    }

    private void requireMember(Group group, User user) {
        boolean isMember = group.getMembers().stream()
                .anyMatch(m -> m.getId().equals(user.getId()));
        if (!isMember) {
            throw new ApiException("NOT_GROUP_MEMBER", HttpStatus.FORBIDDEN, "You are not a member of this group");
        }
    }
}
