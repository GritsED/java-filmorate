package ru.yandex.practicum.filmorate.dao;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    @Override
    public Collection<User> findAll() {
        log.debug("Received request to find all users");
        final String sqlQuery = """
            SELECT *
            FROM users
            """;
        List<User> users = jdbc.query(sqlQuery, userRowMapper);
        log.debug("Successfully retrieved {} users", users.size());
        return users;
    }

    @Override
    public User findUser(Long id) {
        log.debug("Received request to find user with ID {}", id);
        final String sqlQuery = """
            SELECT id, email, login, name, birthday
            FROM users WHERE id = ?
            """;
        try {
            User user = jdbc.queryForObject(sqlQuery, userRowMapper, id);
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
        final String sqlQuery = """
            INSERT INTO users(email, login, name, birthday)
            VALUES (?, ?, ?, ?)
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setString(4, user.getBirthday().toString());
            return ps;
        }, keyHolder);

        return Optional.ofNullable(keyHolder.getKey().longValue())
                .map(id -> {
                    user.setId(id);
                    log.debug("Successfully created user with ID {}", user.getId());
                    return user;
                })
                .orElseThrow(() -> new RuntimeException("Failed to save data"));
    }

    @Override
    public User updateUser(User newUser) {
        log.debug("Received request to update user with ID: {}", newUser.getId());
        final String sqlQuery = """
            UPDATE users SET email = ?, login = ?, name = ?, birthday = ?
            WHERE id = ?
            """;
        if (newUser.getId() == null) {
            log.warn("Failed to update user: ID is missing");
            throw new IllegalArgumentException("ID must be specified.");
        }
        int update = jdbc.update(
                sqlQuery,
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
        final String sqlQuery = """
            DELETE FROM users
            WHERE id = ?
            """;
        int deletedRows = jdbc.update(sqlQuery, id);
        if (deletedRows == 0) {
            log.debug("User with ID {} not found", id);
            throw new NotFoundException("User with id  = " + id + " not found.");
        }
    }
}