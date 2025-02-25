package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.MpaStorage;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private static final String GET_ALL_MPA = """
            SELECT *
            FROM mpa
            """;
    private static final String GET_MPA_BY_ID = """
            SELECT *
            FROM mpa
            WHERE id = ?
            """;

    private final JdbcTemplate jdbc;
    private final MpaRowMapper mpaRowMapper;

    @Override
    public Mpa findMpa(Integer id) {
        try {
            return jdbc.queryForObject(GET_MPA_BY_ID, mpaRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Mpa with id " + id + " not found");
        }
    }

    @Override
    public Collection<Mpa> findAll() {
        return jdbc.query(GET_ALL_MPA, mpaRowMapper);
    }
}