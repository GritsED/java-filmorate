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
    private static final String INSERT_FILM_DIRECTOR_QUERY = """
            MERGE INTO filmDirector (film_id, director_id)
            VALUES (?, ?)
            """;
    private static final Logger log = LoggerFactory.getLogger(FilmDirectorDbStorage.class);

    private final JdbcTemplate jdbc;

    @Override
    public void addDirectorToFilm(Long filmId, Long directorId) {
        log.debug("Received request to add director with ID {} to film with ID {}", directorId, filmId);
        jdbc.update(INSERT_FILM_DIRECTOR_QUERY, filmId, directorId);
        log.debug("Successfully added director with ID {} to film with ID {}", directorId, filmId);
    }
}
