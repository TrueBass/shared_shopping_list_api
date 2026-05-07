package com.example.shared_shopping_list_api.service;

import com.example.shared_shopping_list_api.dto.FriendRequestResponse;
import com.example.shared_shopping_list_api.dto.FriendResponse;
import com.example.shared_shopping_list_api.dto.UserSearchResponse;
import com.example.shared_shopping_list_api.entity.Friendship;
import com.example.shared_shopping_list_api.entity.Friendship.FriendshipStatus;
import com.example.shared_shopping_list_api.entity.User;
import com.example.shared_shopping_list_api.exception.ApiException;
import com.example.shared_shopping_list_api.repository.FriendshipRepository;
import com.example.shared_shopping_list_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Transactional
    public FriendRequestResponse sendFriendRequest(Long addresseeId, String requesterEmail) {
        User requester = findUserByEmail(requesterEmail);

        if (requester.getId().equals(addresseeId)) {
            throw new ApiException("CANNOT_FRIEND_SELF", HttpStatus.BAD_REQUEST, "You cannot send a friend request to yourself");
        }

        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found"));

        friendshipRepository.findBetweenUsers(requester, addressee).ifPresent(f -> {
            throw new ApiException("FRIENDSHIP_ALREADY_EXISTS", HttpStatus.BAD_REQUEST, "A friendship or pending request already exists with this user");
        });

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status(FriendshipStatus.PENDING)
                .build();

        friendship = friendshipRepository.save(friendship);
        return toRequestResponse(friendship, addressee);
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getReceivedRequests(String email) {
        User user = findUserByEmail(email);
        return friendshipRepository.findPendingReceivedRequests(user).stream()
                .map(f -> toRequestResponse(f, f.getRequester()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getSentRequests(String email) {
        User user = findUserByEmail(email);
        return friendshipRepository.findPendingSentRequests(user).stream()
                .map(f -> toRequestResponse(f, f.getAddressee()))
                .toList();
    }

    @Transactional
    public FriendResponse acceptFriendRequest(Long requestId, String email) {
        User user = findUserByEmail(email);
        Friendship friendship = findFriendshipById(requestId);

        if (!friendship.getAddressee().getId().equals(user.getId())) {
            throw new ApiException("NOT_REQUEST_ADDRESSEE", HttpStatus.FORBIDDEN, "You cannot accept a request that was not sent to you");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ApiException("REQUEST_NOT_PENDING", HttpStatus.BAD_REQUEST, "This friend request is no longer pending");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);

        User friend = friendship.getRequester();
        return new FriendResponse(friendship.getId(), friend.getId(), friend.getName(), friend.getEmail(), friendship.getCreatedAt());
    }

    @Transactional
    public void declineFriendRequest(Long requestId, String email) {
        User user = findUserByEmail(email);
        Friendship friendship = findFriendshipById(requestId);

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ApiException("REQUEST_NOT_PENDING", HttpStatus.BAD_REQUEST, "This friend request is no longer pending");
        }

        boolean isAddressee = friendship.getAddressee().getId().equals(user.getId());
        boolean isRequester = friendship.getRequester().getId().equals(user.getId());
        if (!isAddressee && !isRequester) {
            throw new ApiException("NOT_REQUEST_PARTICIPANT", HttpStatus.FORBIDDEN, "You are not a participant in this friend request");
        }

        friendshipRepository.delete(friendship);
    }

    @Transactional(readOnly = true)
    public List<FriendResponse> getFriends(String email, String query) {
        User user = findUserByEmail(email);
        return friendshipRepository.findAcceptedFriendships(user).stream()
                .map(f -> {
                    User friend = f.getRequester().getId().equals(user.getId())
                            ? f.getAddressee()
                            : f.getRequester();
                    return new FriendResponse(f.getId(), friend.getId(), friend.getName(), friend.getEmail(), f.getCreatedAt());
                })
                .filter(f -> query == null || f.getName().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    @Transactional
    public FriendRequestResponse sendFriendRequestByUsername(String username, String requesterEmail) {
        User requester = findUserByEmail(requesterEmail);

        User addressee = userRepository.findByName(username)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "No user found with username \"" + username + "\""));

        if (requester.getId().equals(addressee.getId())) {
            throw new ApiException("CANNOT_FRIEND_SELF", HttpStatus.BAD_REQUEST, "You cannot send a friend request to yourself");
        }

        friendshipRepository.findBetweenUsers(requester, addressee).ifPresent(f -> {
            throw new ApiException("FRIENDSHIP_ALREADY_EXISTS", HttpStatus.BAD_REQUEST, "A friendship or pending request already exists with this user");
        });

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .addressee(addressee)
                .status(FriendshipStatus.PENDING)
                .build();

        friendship = friendshipRepository.save(friendship);
        return toRequestResponse(friendship, addressee);
    }

    @Transactional
    public void removeFriend(Long friendId, String email) {
        User user = findUserByEmail(email);
        Friendship friendship = friendshipRepository.findAcceptedFriendshipBetween(user, friendId)
                .orElseThrow(() -> new ApiException("FRIENDSHIP_NOT_FOUND", HttpStatus.NOT_FOUND, "Friendship not found"));
        friendshipRepository.delete(friendship);
    }

    @Transactional(readOnly = true)
    public List<UserSearchResponse> searchUsers(String query, String email) {
        User currentUser = findUserByEmail(email);
        String pattern = "%" + query.trim().toLowerCase() + "%";
        List<User> users = userRepository.searchUsers(pattern, currentUser.getId());

        if (users.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = users.stream().map(User::getId).toList();
        List<Friendship> friendships = friendshipRepository.findAllBetweenUserAndIds(currentUser, userIds);

        Map<Long, Friendship> friendshipByUserId = new HashMap<>();
        for (Friendship f : friendships) {
            Long otherId = f.getRequester().getId().equals(currentUser.getId())
                    ? f.getAddressee().getId()
                    : f.getRequester().getId();
            friendshipByUserId.put(otherId, f);
        }

        return users.stream().map(u -> {
            Friendship f = friendshipByUserId.get(u.getId());
            String status = null;
            Long friendshipId = null;
            if (f != null) {
                friendshipId = f.getId();
                if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                    status = "ACCEPTED";
                } else if (f.getRequester().getId().equals(currentUser.getId())) {
                    status = "PENDING_SENT";
                } else {
                    status = "PENDING_RECEIVED";
                }
            }
            return new UserSearchResponse(u.getId(), u.getName(), u.getEmail(), status, friendshipId);
        }).toList();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    private Friendship findFriendshipById(Long id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new ApiException("FRIEND_REQUEST_NOT_FOUND", HttpStatus.NOT_FOUND, "Friend request not found"));
    }

    private FriendRequestResponse toRequestResponse(Friendship friendship, User otherUser) {
        return new FriendRequestResponse(
                friendship.getId(),
                otherUser.getId(),
                otherUser.getName(),
                otherUser.getEmail(),
                friendship.getCreatedAt()
        );
    }
}