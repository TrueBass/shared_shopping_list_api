package com.example.shared_shopping_list_api.repository;

import com.example.shared_shopping_list_api.entity.Group;
import com.example.shared_shopping_list_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByMembersContaining(User user);
}