package ru.yandex.practicum.filmorate.dao;


import lombok.RequiredArgsConstructor;
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
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {
    private static final String INSERT_QUERY = """
            INSERT INTO users(email, login, name, birthday)
            VALUES (?, ?, ?, ?)
            """;
    private static final String FIND_ALL_QUERY = """
            SELECT *
            FROM users
            """;
    private static final String FIND_BY_ID_QUERY = """
            SELECT id, email, login, name, birthday
            FROM users WHERE id = ?
            """;
    private static final String UPDATE_QUERY = """
            UPDATE users SET email = ?, login = ?, name = ?, birthday = ?
            WHERE id = ?
            """;
    private static final String DELETE_BY_ID_QUERY = """
            DELETE FROM users
            WHERE id = ?
            """;

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    @Override
    public Collection<User> findAll() {
        return jdbc.query(FIND_ALL_QUERY, userRowMapper);
    }

    @Override
    public User findUser(Long id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID_QUERY, userRowMapper, id);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NotFoundException("User with id " + id + " not found");
        }
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setString(4, user.getBirthday().toString());
            return ps;
        }, keyHolder);

        return Optional.ofNullable(keyHolder.getKey().longValue())
                .map(id -> {
                    user.setId(id);
                    return user;
                })
                .orElseThrow(() -> new RuntimeException("Failed to save data"));
    }

    @Override
    public User updateUser(User newUser) {
        if (newUser.getId() == null) {
            throw new IllegalArgumentException("ID must be specified.");
        }
        int update = jdbc.update(
                UPDATE_QUERY,
                newUser.getEmail(),
                newUser.getLogin(),
                newUser.getName(),
                newUser.getBirthday(),
                newUser.getId()
        );
        if (update == 0) throw new NotFoundException("User with id  = " + newUser.getId() + " not found.");
        return newUser;
    }

    @Override
    public void removeUser(Long id) {
        int deletedRows = jdbc.update(DELETE_BY_ID_QUERY, id);
        if (deletedRows == 0) {
            throw new NotFoundException("User with id  = " + id + " not found.");
        }
    }
}