package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
@RequiredArgsConstructor
@Qualifier("friendDbStorage")
public class FriendDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;
    private final UserStorage userStorage;

    private static final String CHECK_USER_QUERY = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT friend_id FROM friends WHERE user_id = ?";

    private User getUserOrThrow(Long userId) {
        return userStorage.findUser(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " not found"));
    }

    @Override
    public void addFriend(Long userId, Long user2Id) {
        final String INSERT_QUERY = "INSERT INTO friends(user_id, friend_id) VALUES (?, ?)";
        if (Objects.equals(userId, user2Id)) throw new ValidationException("You can't add yourself as a friend");

        log.info("Attempting to add friend with id {} from user with id {}", user2Id, userId);
        User user = getUserOrThrow(userId);
        User user2 = getUserOrThrow(user2Id);

        Integer count = jdbc.queryForObject(CHECK_USER_QUERY, Integer.class, userId, user2Id);

        if (count == null || count == 0) {
            jdbc.update(INSERT_QUERY, userId, user2Id);
            log.info("User {} added user {} to their friends", user.getLogin(), user2.getLogin());
        } else {
            log.info("User {} is already friends with user {}", user.getLogin(), user2.getLogin());
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        final String DELETE_FRIEND_QUERY = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        if (Objects.equals(userId, friendId)) throw new ValidationException("You can't delete yourself from friends");

        log.info("Attempting to remove friend with id {} from user with id {}", friendId, userId);
        User user = getUserOrThrow(userId);
        User user2 = getUserOrThrow(friendId);

        Integer count = jdbc.queryForObject(CHECK_USER_QUERY, Integer.class, userId, friendId);

        if (Objects.nonNull(count) && count > 0) {
            jdbc.update((DELETE_FRIEND_QUERY), userId, friendId);
            log.info("User {} removed user {} from their friends", user.getLogin(), user2.getLogin());
        } else {
            log.info("No friendship found between user {} and user {}", user.getLogin(), user2.getLogin());
        }
    }

    @Override
    public Collection<User> getUserFriends(Long id) {
        return List.of();
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long user2Id) {
        return List.of();
    }
}
