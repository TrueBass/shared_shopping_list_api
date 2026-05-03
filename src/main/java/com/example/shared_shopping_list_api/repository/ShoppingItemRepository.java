package com.example.shared_shopping_list_api.repository;

import com.example.shared_shopping_list_api.entity.Group;
import com.example.shared_shopping_list_api.entity.ShoppingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
    List<ShoppingItem> findByGroupOrderByCreatedAtAsc(Group group);
}
