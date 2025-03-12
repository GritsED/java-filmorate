package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.film.DirectorStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class
DirectorDbStorage implements DirectorStorage {
    private static final String INSERT_DIRECTOR = """
            INSERT INTO directors(name)
            VALUES (?)
            """;
    private static final String UPDATE_DIRECTOR = """
            UPDATE directors SET name = ?
            WHERE id = ?
            """;
    private static final String DELETE_DIRECTOR = """
            DELETE FROM directors
            WHERE id = ?
            """;
    private static final String GET_DIRECTOR = """
            SELECT *
            FROM directors
            WHERE id = ?
            """;
    private static final String GET_ALL_DIRECTORS = """
            SELECT *
            FROM directors
            """;
    private final JdbcTemplate jdbc;
    private final DirectorRowMapper directorRowMapper;

    @Override
    public Director create(Director director) {
        log.debug("Received request to create a new director: {}", director);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_DIRECTOR, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        director.setId(id);
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        log.debug("Received request to update director with ID: {}", director.getId());

        if (director.getId() == null) {
            log.warn("Failed to update director: ID is missing");
            throw new IllegalArgumentException("ID must be specified.");
        }
        int update = jdbc.update(
                UPDATE_DIRECTOR,
                director.getName(),
                director.getId()
        );

        if (update == 0) {
            log.warn("Director update failed: Director with ID {} not found", director.getId());
            throw new NotFoundException("Director with id  = " + director.getId() + " not found.");
        }
        log.debug("Film with ID {} successfully updated", director.getId());
        return director;
    }

    @Override
    public Optional<Director> findDirector(Long id) {
        log.debug("Received request to find director with ID {}", id);
        try {
            log.debug("Successfully found director with ID {}", id);
            return Optional.of(jdbc.queryForObject(GET_DIRECTOR, directorRowMapper, id));
        } catch (DataAccessException e) {
            log.debug("Director with ID {} not found", id);
            throw new NotFoundException("Director with id " + id + " not found");
        }
    }

    @Override
    public Collection<Director> findAll() {
        log.debug("Received request to find all directors");
        List<Director> directors = jdbc.query(GET_ALL_DIRECTORS, directorRowMapper);
        log.debug("Successfully retrieved {} directors", directors.size());
        return directors;
    }

    @Override
    public void removeDirector(Long id) {
        log.debug("Received request to remove director with ID: {}", id);

        int deletedRows = jdbc.update(DELETE_DIRECTOR, id);
        if (deletedRows == 0) {
            log.debug("Director with ID {} not found", id);
            throw new NotFoundException("Director with id  = " + id + " not found.");
        }
    }
}
