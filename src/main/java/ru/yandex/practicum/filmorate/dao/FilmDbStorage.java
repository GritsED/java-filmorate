package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Collection<Film> findAll() {
        final String sqlQuery = """
                SELECT *
                FROM films
                """;
        return jdbc.query(sqlQuery, filmRowMapper);
    }

    @Override
    public Optional<Film> findFilm(Long id) {
        final String sqlQuery = """
                SELECT *
                FROM films f
                JOIN mpa m ON m.id = f.mpa_id
                LEFT JOIN likes l ON l.film_id = f.id
                WHERE f.id = ?
                """;
        try {
            Optional<Film> film = Optional.ofNullable(jdbc.queryForObject(sqlQuery, filmRowMapper, id));
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Film with id " + id + " not found");
        }
    }

    @Override
    public Film create(Film film) {
        final String sqlQuery = """
                INSERT INTO films(name, description, releaseDate, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(id);
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        final String sqlQuery = """
                UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ?, mpa_id = ?
                WHERE id = ?
                """;
        if (newFilm.getId() == null) {
            throw new IllegalArgumentException("ID must be specified.");
        }
        int update = jdbc.update(
                sqlQuery,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getId()
        );
        if (update == 0) throw new NotFoundException("Film with id  = " + newFilm.getId() + " not found.");
        return newFilm;
    }

    @Override
    public void removeFilm(Long id) {
        final String sqlQuery = """
                DELETE FROM films
                WHERE id = ?
                """;
        int deletedRows = jdbc.update(sqlQuery, id);
        if (deletedRows == 0) {
            throw new NotFoundException("Film with id  = " + id + " not found.");
        }
    }

    @Override
    public Collection<Film> getTopFilms(Long count) {
        return null;
    }
}
