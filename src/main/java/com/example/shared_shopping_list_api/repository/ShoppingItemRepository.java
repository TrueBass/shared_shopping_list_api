package com.example.shared_shopping_list_api.repository;

import com.example.shared_shopping_list_api.entity.Group;
import com.example.shared_shopping_list_api.entity.ShoppingItem;
import com.example.shared_shopping_list_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, Long> {
    List<ShoppingItem> findByGroupOrderByCreatedAtAsc(Group group);

    @Modifying
    @Query("DELETE FROM ShoppingItem i WHERE i.group = :group AND i.addedBy = :user AND i.checkedBy IS NULL")
    void deleteUncheckedItemsByGroupAndUser(Group group, User user);

    @Modifying
    @Query("DELETE FROM ShoppingItem i WHERE i.group = :group")
    void deleteAllByGroup(Group group);

    @Modifying
    @Query("UPDATE ShoppingItem i SET i.checkedBy = null, i.checkedAt = null WHERE i.checkedBy = :user")
    void nullifyCheckedBy(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM ShoppingItem i WHERE i.addedBy = :user")
    void deleteAllByAddedBy(@Param("user") User user);

    @Modifying
    @Query("UPDATE ShoppingItem i SET i.addedBy = null WHERE i.addedBy = :user AND i.group = :group")
    void nullifyAddedByInGroup(@Param("user") User user, @Param("group") Group group);
}
