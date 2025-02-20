package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Repository
public class FriendDbStorage implements FriendshipStorage {
    private static final String CHECK_USER_QUERY = """
            SELECT COUNT(*) FROM friends
            WHERE user_id = ? AND friend_id = ?
            """;
    private final JdbcTemplate jdbc;
    private final UserStorage userStorage;
    private final UserRowMapper userRowMapper;

    public FriendDbStorage(JdbcTemplate jdbc,
                           @Qualifier("userDbStorage") UserStorage userStorage,
                           UserRowMapper userRowMapper) {
        this.jdbc = jdbc;
        this.userStorage = userStorage;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public void addFriend(Long userId, Long user2Id) {
        final String INSERT_QUERY = """
                INSERT INTO friends(user_id, friend_id)
                VALUES (?, ?)
                """;
        if (Objects.equals(userId, user2Id)) throw new ValidationException("You can't add yourself as a friend");

        log.info("Attempting to add friend with id {} from user with id {}", user2Id, userId);
        User user = userStorage.findUser(userId);
        User user2 = userStorage.findUser(user2Id);

        Integer count = jdbc.queryForObject(CHECK_USER_QUERY, Integer.class, userId, user2Id);

        if (count == null || count == 0) {
            jdbc.update(INSERT_QUERY, userId, user2Id);
            user.addFriend(user2Id);
            log.info("User {} added user {} to their friends", user.getLogin(), user2.getLogin());
        } else {
            log.info("User {} is already friends with user {}", user.getLogin(), user2.getLogin());
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        final String DELETE_FRIEND_QUERY = """
                 DELETE FROM friends
                 WHERE user_id = ? AND friend_id = ?
                """;
        if (Objects.equals(userId, friendId)) throw new ValidationException("You can't delete yourself from friends");

        log.info("Attempting to remove friend with id {} from user with id {}", friendId, userId);
        User user = userStorage.findUser(userId);
        User user2 = userStorage.findUser(friendId);

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
        final String GET_USER_FRIENDS_QUERY = """
                SELECT  u.*
                FROM friends f
                JOIN users u ON u.id = f.friend_id
                WHERE f.user_id = ?
                """;

        userStorage.findUser(id);

        Set<User> friends = new HashSet<>(jdbc.query(GET_USER_FRIENDS_QUERY, userRowMapper, id));

        if (friends.isEmpty()) {
            log.info("User {} has no friends", id);
        }

        log.info("User with id {} has {} friends.", id, friends.size());
        return friends;
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long user2Id) {
        final String GET_COMMON_FRIENDS_QUERY = """
                SELECT u.*
                FROM users u
                JOIN friends f ON u.id = f.friend_id
                JOIN friends f2 ON u.id = f2.friend_id
                WHERE f.user_id = ? AND f2.user_id = ?
                """;

        log.info("Received request to find common friends: userId={}, user2Id={}", userId, user2Id);
        log.info("Attempting to find common friends for user {} and user {}", userId, user2Id);
        userStorage.findUser(userId);
        userStorage.findUser(user2Id);

        Set<User> commonFriends = new HashSet<>(
                jdbc.query(GET_COMMON_FRIENDS_QUERY, userRowMapper, userId, user2Id));

        if (commonFriends.isEmpty()) {
            log.info("No common friends found between user {} and user {}", userId, user2Id);
            return Collections.emptyList();
        }

        log.info("Found {} common friends between user {} and user {}", commonFriends.size(), userId, user2Id);
        return commonFriends;
    }
}
