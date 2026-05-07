package com.example.shared_shopping_list_api.repository;

import com.example.shared_shopping_list_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);

    @Query("SELECT u FROM User u WHERE u.id != :excludeId AND (LOWER(u.name) LIKE :pattern OR LOWER(u.email) LIKE :pattern)")
    List<User> searchUsers(@Param("pattern") String pattern, @Param("excludeId") Long excludeId);
}