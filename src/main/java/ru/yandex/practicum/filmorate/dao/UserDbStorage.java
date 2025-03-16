package ru.yandex.practicum.filmorate.dao;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
@Primary
public class UserDbStorage implements UserStorage {
    private static final String GET_USERS = """
            SELECT *
            FROM users
            """;
    private static final String GET_USER_BY_ID = """
            SELECT id, email, login, name, birthday
            FROM users WHERE id = ?
            """;
    private static final String INSERT_USER = """
            INSERT INTO users(email, login, name, birthday)
            VALUES (?, ?, ?, ?)
            """;
    private static final String UPDATE_USER = """
            UPDATE users SET email = ?, login = ?, name = ?, birthday = ?
            WHERE id = ?
            """;
    private static final String DELETE_USER = """
            DELETE FROM users
            WHERE id = ?
            """;
    private static final String GET_USER_FRIENDS = """
            SELECT friend_id
            FROM friends
            WHERE user_id = ?
            """;
    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    @Override
    public Collection<User> findAll() {
        log.debug("Received request to find all users");
        List<User> users = jdbc.query(GET_USERS, userRowMapper);
        users.forEach(this::loadUserFriends);
        log.debug("Successfully retrieved {} users", users.size());
        return users;
    }

    @Override
    public User findUser(Long id) {
        log.debug("Received request to find user with ID {}", id);
        try {
            User user = jdbc.queryForObject(GET_USER_BY_ID, userRowMapper, id);
            loadUserFriends(user);
            log.debug("Successfully retrieved user with ID {}", id);
            return user;
        } catch (EmptyResultDataAccessException ignored) {
            log.warn("User with ID {} not found", id);
            throw new NotFoundException("User with id " + id + " not found");
        }
    }

    @Override
    public User create(User user) {
        log.debug("Received request to create a new user: {}", user);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_USER, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setString(4, user.getBirthday().toString());
            return ps;
        }, keyHolder);

        return Optional.ofNullable(keyHolder.getKey().longValue())
                .map(id -> {
                    user.setId(id);
                    loadUserFriends(user);
                    log.debug("Successfully created user with ID {}", user.getId());
                    return user;
                })
                .orElseThrow(() -> new RuntimeException("Failed to save data"));
    }

    @Override
    public User updateUser(User newUser) {
        log.debug("Received request to update user with ID: {}", newUser.getId());
        if (newUser.getId() == null) {
            log.warn("Failed to update user: ID is missing");
            throw new IllegalArgumentException("ID must be specified.");
        }
        int update = jdbc.update(
                UPDATE_USER,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday(),
                newUser.getId()
        );
        if (update == 0) {
            log.warn("Film update failed: Film with ID {} not found", newUser.getId());
            throw new NotFoundException("User with id  = " + newUser.getId() + " not found.");
        }
        log.debug("User with ID {} successfully updated", newUser.getId());
        return newUser;
    }

    @Override
    public void removeUser(Long id) {
        log.debug("Received request to remove user with ID: {}", id);
        int deletedRows = jdbc.update(DELETE_USER, id);
        if (deletedRows == 0) {
            log.debug("User with ID {} not found", id);
            throw new NotFoundException("User with id  = " + id + " not found.");
        }
    }

    private void loadUserFriends(User user) {
        Set<Long> friendId = new HashSet<>(jdbc.query(GET_USER_FRIENDS,
                (rs, rowNum) -> rs.getLong("friend_id"), user.getId()));
        user.setFriends(friendId);
    }
}