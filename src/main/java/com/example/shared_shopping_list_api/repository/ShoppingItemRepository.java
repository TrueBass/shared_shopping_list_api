package com.example.shared_shopping_list_api.repository;

import com.example.shared_shopping_list_api.entity.Group;
import com.example.shared_shopping_list_api.entity.ShoppingItem;
import com.example.shared_shopping_list_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
    List<ShoppingItem> findByGroupOrderByCreatedAtAsc(Group group);

    @Modifying
    @Query("DELETE FROM ShoppingItem i WHERE i.group = :group AND i.addedBy = :user AND i.checkedBy IS NULL")
    void deleteUncheckedItemsByGroupAndUser(Group group, User user);

    @Modifying
    @Query("DELETE FROM ShoppingItem i WHERE i.group = :group")
    void deleteAllByGroup(Group group);
}
