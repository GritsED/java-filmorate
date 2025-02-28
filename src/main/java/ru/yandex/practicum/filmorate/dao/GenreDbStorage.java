package ru.yandex.practicum.filmorate.dao;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private static final String GET_GENRE = """
            SELECT *
            FROM genres
            WHERE id = ?
            """;
    private static final String GET_ALL_GENRES = """
            SELECT *
            FROM genres
            """;
    private final JdbcTemplate jdbc;
    private final GenreRowMapper genreRowMapper;

    @Override
    public Genre findGenre(Integer id) {
        log.debug("Received request to find genre with ID {}", id);
        try {
            log.debug("Successfully found genre with ID {}", id);
            return jdbc.queryForObject(GET_GENRE, genreRowMapper, id);
        } catch (DataAccessException e) {
            log.debug("Genre with ID {} not found", id);
            throw new NotFoundException("Genre with id " + id + " not found");
        }
    }

    @Override
    public Collection<Genre> findAll() {
        log.debug("Received request to find all genres");
        List<Genre> genres = jdbc.query(GET_ALL_GENRES, genreRowMapper);
        log.debug("Successfully retrieved {} genres", genres.size());
        return genres;
    }
}
