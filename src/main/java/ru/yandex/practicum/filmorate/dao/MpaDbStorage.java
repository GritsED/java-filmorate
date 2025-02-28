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
    private static final String GET_MPA = """
            SELECT *
            FROM mpa
            WHERE id = ?
            """;
    private static final String GET_ALL_MPA = """
            SELECT *
            FROM mpa
            """;
    private final JdbcTemplate jdbc;
    private final MpaRowMapper mpaRowMapper;

    @Override
    public Mpa findMpa(Integer id) {
        log.debug("Received request to find MPA with ID {}", id);
        try {
            log.debug("Successfully found MPA with ID {}", id);
            return jdbc.queryForObject(GET_MPA, mpaRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.debug("MPA with ID {} not found", id);
            throw new NotFoundException("Mpa with id " + id + " not found");
        }
    }

    @Override
    public Collection<Mpa> findAll() {
        log.debug("Received request to find all genres");
        List<Mpa> mpas = jdbc.query(GET_ALL_MPA, mpaRowMapper);
        log.debug("Successfully retrieved {} MPAs", mpas.size());
        return mpas;
    }
}