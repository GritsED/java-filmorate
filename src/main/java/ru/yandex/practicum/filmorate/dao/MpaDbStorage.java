package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbc;
    private final MpaRowMapper mpaRowMapper;

    @Override
    public Mpa findMpa(Integer id) {
        log.debug("Received request to find MPA with ID {}", id);
        final String sqlQuery = """
                SELECT *
                FROM mpa
                """;
        try {
            log.debug("Successfully found MPA with ID {}", id);
            return jdbc.queryForObject(sqlQuery, mpaRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.debug("MPA with ID {} not found", id);
            throw new NotFoundException("Mpa with id " + id + " not found");
        }
    }

    @Override
    public Collection<Mpa> findAll() {
        log.debug("Received request to find all genres");
        final String sqlQuery = """
                SELECT *
                FROM mpa
                WHERE id = ?
                """;
        List<Mpa> mpas = jdbc.query(sqlQuery, mpaRowMapper);
        log.debug("Successfully retrieved {} MPAs", mpas.size());
        return mpas;
    }
}