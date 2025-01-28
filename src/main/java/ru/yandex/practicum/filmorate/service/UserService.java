package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Optional<User> findUser(Long userId) {
        return userStorage.findAll()
                .stream()
                .filter(user -> Objects.equals(user.getId(), userId))
                .findFirst();
    }

    public User addFriend(Long userId, Long user2Id) {
        if (userId == null || user2Id == null) throw new ValidationException("User IDs must not be null");

        if (Objects.equals(userId, user2Id)) throw new ValidationException("You can't add yourself as a friend");

        log.info("Attempting to add friend with id {} from user with id {}", user2Id, userId);
        User user = findUser(userId)
                .orElseThrow(() -> new ValidationException("User with id = " + userId + " not found"));
        User user2 = findUser(user2Id)
                .orElseThrow(() -> new ValidationException("User with id = " + user2Id + " not found"));

        user.getFriendsId().add(user2Id);
        user2.getFriendsId().add(userId);
        log.info("User {} added user {} to their friends", user.getLogin(), user2.getLogin());
        return user;
    }

    public User deleteFriend(Long userId, Long friendId) {
        if (userId == null || friendId == null) throw new ValidationException("User IDs must not be null");

        if (Objects.equals(userId, friendId)) throw new ValidationException("You can't delete yourself from friends");

        log.info("Attempting to remove friend with id {} from user with id {}", friendId, userId);
        User user = findUser(userId)
                .orElseThrow(() -> new ValidationException("User with id = " + userId + " not found"));
        User friend = findUser(friendId)
                .orElseThrow(() -> new ValidationException("User with id = " + friendId + " not found"));

        if (!user.getFriendsId().contains(friendId))
            throw new ValidationException("User with id = " + friendId +
                    " is not a friend of user with id = " + userId);

        if (!friend.getFriendsId().contains(userId))
            throw new ValidationException("User with id = " + userId +
                    " is not a friend of user with id = " + friendId);

        user.getFriendsId().remove(friendId);
        friend.getFriendsId().remove(userId);
        log.info("User {} remove user {} from their friends", user.getLogin(), friend.getLogin());
        return user;
    }

    public Collection<User> getCommonFriends(Long userId, Long user2Id) {
        if (userId == null || user2Id == null) throw new ValidationException("User IDs must not be null");

        log.info("Attempting to find common friends for user {} and user {}", userId, user2Id);
        User user = findUser(userId)
                .orElseThrow(() -> new ValidationException("User with id = " + userId + " not found"));
        User user2 = findUser(user2Id)
                .orElseThrow(() -> new ValidationException("User with id = " + user2Id + " not found"));

        if (user.getFriendsId().isEmpty() || user2.getFriendsId().isEmpty()) {
            log.info("One or both users have no friends. Returning empty list.");
            return Collections.emptyList();
        }

        List<User> commonFriends = user.getFriendsId()
                .stream()
                .filter(user2.getFriendsId()::contains)
                .flatMap(friendId -> findUser(friendId).stream()) //Извлекаем User из Optional
                .toList();

        if (commonFriends.isEmpty()) {
            log.info("No common friends found between user {} and user {}", userId, user2Id);
            return Collections.emptyList();
        }

        log.info("Found {} common friends between user {} and user {}", commonFriends.size(), userId, user2Id);
        return commonFriends;
    }
}