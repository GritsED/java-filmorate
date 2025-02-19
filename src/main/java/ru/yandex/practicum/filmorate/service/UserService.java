package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findUser(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));
    }

    public User addFriend(Long userId, Long user2Id) {
        if (Objects.equals(userId, user2Id)) throw new ValidationException("You can't add yourself as a friend");

        log.info("Attempting to add friend with id {} from user with id {}", user2Id, userId);
        User user = getUserOrThrow(userId);
        User user2 = getUserOrThrow(user2Id);

        user.addFriend(user2Id);
        log.info("User {} added user {} to their friends", user.getLogin(), user2.getLogin());
        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        if (Objects.equals(userId, friendId)) throw new ValidationException("You can't delete yourself from friends");

        log.info("Attempting to remove friend with id {} from user with id {}", friendId, userId);
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        boolean wasFriend = user.removeFriend(friendId);
        boolean wasFriend2 = friend.removeFriend(userId);

        if (!wasFriend) {
            log.info("User with id = {} is not a friend of user with id = {}", friendId, userId);
        }

        if (!wasFriend2) {
            log.info("User with id = {} is not a friend of user with id = {}", userId, friendId);
        }

        if (wasFriend) {
            log.info("User {} remove user {} from their friends", user.getLogin(), friend.getLogin());
        }
        return user;
    }

    public Collection<User> getUserFriends(Long id) {
        User user = getUserOrThrow(id);

        List<User> friends = user.getFriends()
                .stream()
                .flatMap(friendId -> userStorage.findUser(friendId).stream())
                .toList();

        if (friends.isEmpty()) {
            log.info("User with id {} has no friends.", id);
            return Collections.emptyList();
        }

        log.info("User with id {} has {} friends.", id, friends.size());
        return friends;
    }

    public Collection<User> getCommonFriends(Long userId, Long user2Id) {
        log.info("Received request to find common friends: userId={}, user2Id={}", userId, user2Id);
        log.info("Attempting to find common friends for user {} and user {}", userId, user2Id);
        User user = getUserOrThrow(userId);
        User user2 = getUserOrThrow(user2Id);

        if (user.getFriends().isEmpty() || user2.getFriends().isEmpty()) {
            log.info("One or both users have no friends. Returning empty list.");
            return Collections.emptyList();
        }

        List<User> commonFriends = user.getFriends()
                .stream()
                .filter(user2.getFriends()::contains)
                .flatMap(friendId -> userStorage.findUser(friendId).stream())
                .toList();

        if (commonFriends.isEmpty()) {
            log.info("No common friends found between user {} and user {}", userId, user2Id);
            return Collections.emptyList();
        }

        log.info("Found {} common friends between user {} and user {}", commonFriends.size(), userId, user2Id);
        return commonFriends;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findUser(Long id) {
        return getUserOrThrow(id);
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User newUser) {
        return userStorage.updateUser(newUser);
    }
}