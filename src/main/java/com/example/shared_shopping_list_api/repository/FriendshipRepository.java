package com.example.shared_shopping_list_api.repository;

import com.example.shared_shopping_list_api.entity.Friendship;
import com.example.shared_shopping_list_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT f FROM Friendship f WHERE (f.requester = :a AND f.addressee = :b) OR (f.requester = :b AND f.addressee = :a)")
    Optional<Friendship> findBetweenUsers(@Param("a") User a, @Param("b") User b);

    @Query("SELECT f FROM Friendship f WHERE (f.requester = :user AND f.addressee.id IN :userIds) OR (f.addressee = :user AND f.requester.id IN :userIds)")
    List<Friendship> findAllBetweenUserAndIds(@Param("user") User user, @Param("userIds") List<Long> userIds);

    @Query("SELECT f FROM Friendship f WHERE f.addressee = :user AND f.status = 'PENDING'")
    List<Friendship> findPendingReceivedRequests(@Param("user") User user);

    @Query("SELECT f FROM Friendship f WHERE f.requester = :user AND f.status = 'PENDING'")
    List<Friendship> findPendingSentRequests(@Param("user") User user);

    @Query("SELECT f FROM Friendship f WHERE (f.requester = :user OR f.addressee = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("user") User user);

    @Query("SELECT f FROM Friendship f WHERE ((f.requester = :user AND f.addressee.id = :friendId) OR (f.addressee = :user AND f.requester.id = :friendId)) AND f.status = 'ACCEPTED'")
    Optional<Friendship> findAcceptedFriendshipBetween(@Param("user") User user, @Param("friendId") Long friendId);

    @Modifying
    @Query("DELETE FROM Friendship f WHERE f.requester = :user OR f.addressee = :user")
    void deleteAllByUser(@Param("user") User user);
}