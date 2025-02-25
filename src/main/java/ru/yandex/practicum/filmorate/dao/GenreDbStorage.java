package ru.yandex.practicum.filmorate.dao;


import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private static final String GET_ALL_GENRE = """
            SELECT *
            FROM genres
            """;
    private static final String GET_GENRE_BY_ID = """
            SELECT *
            FROM genres
            WHERE id = ?
            """;

    private final JdbcTemplate jdbc;
    private final GenreRowMapper genreRowMapper;

    @Override
    public Genre findGenre(Integer id) {
        try {
            return jdbc.queryForObject(GET_GENRE_BY_ID, genreRowMapper, id);
        } catch (DataAccessException e) {
            throw new NotFoundException("Genre with id " + id + " not found");
        }
    }

    @Override
    public Collection<Genre> findAll() {
        return jdbc.query(GET_ALL_GENRE, genreRowMapper);
    }
}
