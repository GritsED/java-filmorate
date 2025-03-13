package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.film.FilmGenreStorage;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmGenreDbStorage implements FilmGenreStorage {
    private static final String INSERT_FILM_GENRE_QUERY = """
            MERGE INTO filmGenre (film_id, genre_id)
            VALUES (?, ?)
            """;

    private final JdbcTemplate jdbc;

    @Override
    public void addGenreToFilm(Long filmId, Integer genreId) {
        log.debug("Received request to add genre with ID {} to film with ID {}", genreId, filmId);
        jdbc.update(INSERT_FILM_GENRE_QUERY, filmId, genreId);
        log.debug("Successfully added genre with ID {} to film with ID {}", genreId, filmId);
    }
}
