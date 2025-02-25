package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final FilmGenreStorage filmGenreStorage;

    @Override
    public Collection<Film> findAll() {
        final String sqlQuery = """
                SELECT f.*, m.id AS mpa_id, m.rate AS mpa_name
                FROM films f
                JOIN mpa m ON m.id = f.mpa_id
                """;

        List<Film> films = jdbc.query(sqlQuery, filmRowMapper);
        films.forEach(this::loadFilmData);
        return films;
    }

    @Override
    public Optional<Film> findFilm(Long id) {
        final String sqlQuery = """
                SELECT *
                FROM films f
                JOIN mpa m ON m.id = f.mpa_id
                WHERE f.id = ?
                """;
        try {
            Film film = jdbc.queryForObject(sqlQuery, filmRowMapper, id);
            loadFilmData(film);
            return Optional.of(film);
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
        film.getGenres().forEach(genre -> filmGenreStorage.addGenreToFilm(film.getId(), genre.getId()));
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
        final String sqlQuery = """
                SELECT f.*, m.id AS mpa_id, m.rate, COUNT(l.user_id) AS likes_count, g.id genre
                FROM films f
                JOIN mpa m ON f.mpa_id = m.id
                LEFT JOIN likes l ON f.id = l.film_id
                LEFT JOIN filmGenre fg ON fg.film_id = f.id
                LEFT JOIN genres g ON g.id = fg.genre_id
                GROUP BY f.id, m.id, m.rate, g.id
                ORDER BY likes_count DESC
                LIMIT ?
                """;
        List<Film> films = jdbc.query(sqlQuery, filmRowMapper, count);
        films.forEach(this::loadFilmData);
        return films;
    }

    private void loadFilmData(Film film) {
        String likeQuery = "SELECT user_id FROM likes WHERE film_id = ?";
        Set<Long> likes = new HashSet<>(jdbc.query(likeQuery, (rs, rowNum) -> rs.getLong("user_id"), film.getId()));
        film.setLikes(likes);

        String genreQuery = """
                    SELECT g.id, g.name
                    FROM filmGenre fg
                    JOIN genres g ON g.id = fg.genre_id
                    WHERE fg.film_id = ?
                """;
        Set<Genre> genres = new LinkedHashSet<>(jdbc.query(genreQuery, genreRowMapper, film.getId()));
        film.setGenres(genres);
    }
}
