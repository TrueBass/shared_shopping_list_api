package com.example.shared_shopping_list_api.repository;

import com.example.shared_shopping_list_api.entity.RefreshToken;
import com.example.shared_shopping_list_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
