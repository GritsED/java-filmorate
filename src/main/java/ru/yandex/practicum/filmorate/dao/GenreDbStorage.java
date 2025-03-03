package ru.yandex.practicum.filmorate.dao;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.GenreStorage;

import java.util.*;

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
    private static final String GET_FILM_GENRES = """
            SELECT *
            FROM filmGenre fg
            JOIN genres g ON fg.genre_id = g.id
            WHERE film_id IN (?)
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

    @Override
    public void getFilmsGenres(List<Film> films) {
        List<Long> filmsId = films.stream().map(Film::getId).toList();
        String placeholder = String.join(", ", Collections.nCopies(filmsId.size(), "?"));
        Map<Long, Set<Genre>> filmGenresMap = jdbc.query(GET_FILM_GENRES.replace("?", placeholder),
                rs -> {
                    Map<Long, Set<Genre>> map = new HashMap<>();
                    while (rs.next()) {
                        Long filmId = rs.getLong("film_id");
                        Genre genreId = new Genre(rs.getInt("genre_id"), rs.getString("name"));
                        map.computeIfAbsent(filmId, v -> new LinkedHashSet<>()).add(genreId);
                    }
                    return map;
                },
                filmsId.toArray());
        for (Film film : films) {
            Set<Genre> genres = filmGenresMap.get(film.getId());
            film.setGenres(Objects.requireNonNullElseGet(genres, LinkedHashSet::new));
        }
    }
}
