package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.film.FilmDirectorStorage;

@Repository
@RequiredArgsConstructor
public class FilmDirectorDbStorage implements FilmDirectorStorage {
    private static final String CHECK_USER_QUERY = """
            SELECT COUNT(*) FROM filmDirector
            WHERE film_id = ? AND director_id = ?
            """;
    private static final String INSERT_FILM_DIRECTOR_QUERY = """
            INSERT INTO filmDirector (film_id, director_id)
            VALUES (?, ?)
            """;
    private static final Logger log = LoggerFactory.getLogger(FilmDirectorDbStorage.class);

    private final JdbcTemplate jdbc;

    @Override
    public void addDirectorToFilm(Long filmId, Long directorId) {
        Integer count = jdbc.queryForObject(CHECK_USER_QUERY, Integer.class, filmId, directorId);

        if (count == null || count == 0) {
            jdbc.update(INSERT_FILM_DIRECTOR_QUERY, filmId, directorId);
        } else {
            log.info("Film with id {} already has a director with id {}", filmId, directorId);
        }
    }
}
