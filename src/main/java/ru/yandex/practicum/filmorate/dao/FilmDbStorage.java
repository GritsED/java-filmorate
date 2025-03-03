package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;
import ru.yandex.practicum.filmorate.storage.film.LikeStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private static final String GET_ALL_FILMS = """
            SELECT f.*, m.id AS mpa_id, m.rate AS mpa_name
            FROM films f
            JOIN mpa m ON m.id = f.mpa_id
            """;
    private static final String GET_FILM_BY_ID = """
            SELECT *
            FROM films f
            JOIN mpa m ON m.id = f.mpa_id
            WHERE f.id = ?
            """;
    private static final String INSERT_FILM = """
            INSERT INTO films(name, description, releaseDate, duration, mpa_id)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_FILM = """
            UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ?, mpa_id = ?
            WHERE id = ?
            """;
    private static final String DELETE_FILM = """
            DELETE FROM films
            WHERE id = ?
            """;
    private static final String GET_TOP_FILMS = """
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
    private static final String GET_LIKES = """
            SELECT user_id
            FROM likes
            WHERE film_id = ?
            """;
    private static final String GET_GENRE = """
            SELECT g.id, g.name
            FROM filmGenre fg
            JOIN genres g ON g.id = fg.genre_id
            WHERE fg.film_id = ?
            """;
    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final FilmGenreStorage filmGenreStorage;
    private final LikeStorage likeStorage;
    private final GenreStorage genreStorage;

    @Override
    public Collection<Film> findAll() {
        log.debug("Received request to retrieve all films");
        List<Film> films = jdbc.query(GET_ALL_FILMS, filmRowMapper);
        likeStorage.getFilmsLikes(films);
        genreStorage.getFilmsGenres(films);
        log.debug("Returning list of films");
        return films;
    }

    @Override
    public Optional<Film> findFilm(Long id) {
        log.debug("Received request to find film with ID {}", id);
        try {
            Film film = jdbc.queryForObject(GET_FILM_BY_ID, filmRowMapper, id);
            Set<Long> likes = new HashSet<>(jdbc.query(GET_LIKES,
                    (rs, rowNum) -> rs.getLong("user_id"), id));
            film.setLikes(likes);
            Set<Genre> genres = new LinkedHashSet<>(jdbc.query(GET_GENRE, genreRowMapper, film.getId()));
            film.setGenres(genres);

            log.debug("Returning film details for ID {}", id);
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Film with ID {} not found", id);
            throw new NotFoundException("Film with id " + id + " not found");
        }
    }

    @Override
    public Film create(Film film) {
        log.debug("Received request to create a new film: {}", film);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
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
        log.debug("Film successfully added with ID {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        log.debug("Received request to update film with ID: {}", newFilm.getId());

        if (newFilm.getId() == null) {
            log.warn("Failed to update film: ID is missing");
            throw new IllegalArgumentException("ID must be specified.");
        }
        int update = jdbc.update(
                UPDATE_FILM,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getId()
        );
        if (update == 0) {
            log.warn("Film update failed: Film with ID {} not found", newFilm.getId());
            throw new NotFoundException("Film with id  = " + newFilm.getId() + " not found.");
        }
        log.debug("Film with ID {} successfully updated", newFilm.getId());
        return newFilm;
    }

    @Override
    public void removeFilm(Long id) {
        log.debug("Received request to remove film with ID: {}", id);

        int deletedRows = jdbc.update(DELETE_FILM, id);
        if (deletedRows == 0) {
            log.debug("Film with ID {} not found", id);
            throw new NotFoundException("Film with id  = " + id + " not found.");
        }
    }

    @Override
    public Collection<Film> getTopFilms(Long count) {
        log.debug("Received request to get top {} films", count);
        List<Film> films = jdbc.query(GET_TOP_FILMS, filmRowMapper, count);
        log.debug("Returning top films list with {} entries", films.size());
        likeStorage.getFilmsLikes(films);
        genreStorage.getFilmsGenres(films);
        log.debug("Returning list of top films");
        return films;
    }
}
